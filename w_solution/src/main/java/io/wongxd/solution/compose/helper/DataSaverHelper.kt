package io.wongxd.solution.compose.helper

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import kotlin.reflect.KProperty

/**
 * Some config that you can set:
 * 1. DEBUG: whether to output some debug info
 * 2. LIST_SEPARATOR: the separator used to convert a list into string, '#@#' by default (**don't use ',' which will occurs in json itself** )
 */
object DataSaverConfig : DataSaverScope {
    var DEBUG = true
        private set
    const val LIST_SEPARATOR = "#@#"

    fun setDebug(flag: Boolean) {
        DEBUG = flag
    }

}


//<editor-fold desc="作用域">

@DslMarker
annotation class DataSaverScopeMarker

/**
 * 特殊作用域的方法
 */
@DataSaverScopeMarker
interface DataSaverScope {

}

//</editor-fold>


//<editor-fold desc="类型注册">

val DataSaverScope.typeSaveConverters: MutableMap<Class<*>, (Any?) -> String>
        by lazy(LazyThreadSafetyMode.PUBLICATION) { mutableMapOf() }

val DataSaverScope.typeRestoreConverters: MutableMap<Class<*>, (String) -> Any?>
        by lazy(LazyThreadSafetyMode.PUBLICATION) { mutableMapOf() }

/**
 * Use this function to convert your entity class into basic data type to store.
 * Check the example of this repository to see how to use it.
 *
 * @param save Function1<T, Any>? save your entity bean into [String]
 * @param restore Function1<Any, T>? restore your entity bean from the saved [String] value
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any?> DataSaverScope.registerTypeConverters(
    noinline save: ((T) -> String)? = null,
    noinline restore: ((String) -> T)? = null
) {
    save?.let { typeSaveConverters[T::class.java] = it as ((Any?) -> String) }
    restore?.let { typeRestoreConverters[T::class.java] = it }
}

private fun DataSaverScope.convertListToString(list: List<*>): String {
    val sb = StringBuilder("[")
    for (each in list) {
        when (each) {
            is List<*> -> sb.append(convertListToString(each))
            null -> error("unable to save data: some part of list is null! ")
            else -> run {
                val typeConverter = typeSaveConverters[each::class.java]
                typeConverter ?: unsupportedType(each)
                sb.append("${typeConverter(each)}${DataSaverConfig.LIST_SEPARATOR}")
            }
        }
    }
    if (sb.length > DataSaverConfig.LIST_SEPARATOR.length + 2) {
        sb.delete(
            sb.length - DataSaverConfig.LIST_SEPARATOR.length,
            sb.length
        )
    }
    sb.append("]")
    return sb.toString()
}

inline fun <reified T> DataSaverScope.convertStringToList(str: String): List<T> {
    if (str.length < 2) error("Invalid text($str), it should be like [a${DataSaverConfig.LIST_SEPARATOR}b${DataSaverConfig.LIST_SEPARATOR}c] instead.")
    val restoreConverter = typeRestoreConverters[T::class.java]
    restoreConverter
        ?: error("Unable to restore $str: ${T::class.java} is not supported, please call [registerTypeConverters] at first!")
    val s = str.substring(1, str.length - 1)
    return try {
        val arr = s.split(DataSaverConfig.LIST_SEPARATOR)
        arr.map {
            restoreConverter(it) as T
        }
    } catch (e: Exception) {
        Log.e("DataConverter", "error while parsing $str to list")
        e.printStackTrace()
        emptyList()
    }
}

private fun unsupportedType(data: Any?): Nothing =
    error("Unable to save data: type of $data (class: ${if (data == null) "null" else data::class.java} is not supported, please call [DataSaverScope.registerTypeConverters] at first!")

//</editor-fold>


//<editor-fold desc="保存策略">

/**
 * Controls whether and when to do data persistence. Includes [IMMEDIATELY], [DISPOSED] and [NEVER] by default.
 *
 * 控制是否做、什么时候做数据持久化
 */
open class SavePolicy {
    /**
     * Default mode, do data persistence every time you assign a new value to the state.
     *
     * 默认模式，每次给state的value赋新值时就做持久化
     */
    object IMMEDIATELY : SavePolicy()

    /**
     * do data persistence when the Composable enters `onDispose`. NOTE: USE THIS MODE CAREFULLY, BECAUSE SOMETIME
     * `onDispose` WILL NOT BE CALLED
     *
     * Composable `onDispose` 时做数据持久化，适合数据变动比较频繁、且此Composable会进入onDispose的情况。
     * **慎用此模式，因为有些情况下onDispose不会被回调**
     */
    object DISPOSED : SavePolicy()

    /**
     * NEVER do data persistence automatically. Please call `state.saveData()` manually.
     *
     * 不会自动做持久化操作，请自行调用`state.saveData()`。
     *
     * Example: `onClick = { state.saveData() }`
     */
    object NEVER : SavePolicy()
}

//</editor-fold>


//<editor-fold desc="持久化代理">

/**
 * The interface is used to save/read data. We provide the basic implementation using Preference, DataStore and MMKV.
 *
 * If you want to write your own, you need to implement `saveData` and `readData`. Besides, a suspend function `saveDataAsync` is optional(which is equal to `saveData` by default)
 */
interface DataSaverInterface {
    fun <T> saveData(key: String, data: T)
    fun <T> readData(key: String, default: T): T
    suspend fun <T> saveDataAsync(key: String, data: T) = saveData(key, data)
    fun remove(key: String)
}

/**
 * Default implementation using [SharedPreferences] to save data
 */
class DataSaverPreferences(private val preference: SharedPreferences) : DataSaverInterface {
    constructor(context: Context) : this(
        context.getSharedPreferences(
            context.packageName,
            Context.MODE_PRIVATE
        )
    )

    override fun <T> saveData(key: String, data: T) = with(preference.edit()) {
        when (data) {
            null -> {
                this@DataSaverPreferences.remove(key)
                return@with
            }
            is Long -> putLong(key, data)
            is Int -> putInt(key, data)
            is String -> putString(key, data)
            is Boolean -> putBoolean(key, data)
            is Float -> putFloat(key, data)
            else -> throw IllegalArgumentException("Unable to save $data, this type(${data!!::class.java}) cannot be saved using SharedPreferences, call [registerTypeConverters] to support it.")
        }.apply()
    }

    override fun <T> readData(key: String, default: T): T = with(preference) {
        val res: Any = when (default) {
            is Long -> getLong(key, default)
            is String -> this.getString(key, default)!!
            is Int -> getInt(key, default)
            is Boolean -> getBoolean(key, default)
            is Float -> getFloat(key, default)
            else -> {
                val restore = DataSaverConfig.typeRestoreConverters[default!!::class.java]
                throw IllegalArgumentException("Unable to read $default, restore:$restore, this type(${default!!::class.java}) cannot be get from Preferences, call [registerTypeConverters] to support it.")
            }
        }
        return res as T
    }

    override fun remove(key: String) {
        preference.edit().remove(key).apply()
    }
}

/**
 * You can call `LocalDataSaver.current` inside a [androidx.compose.runtime.Composable] to
 * get the instance you've provided. You can call `readData` and `saveData` then.
 */
var LocalDataSaver: ProvidableCompositionLocal<DataSaverInterface> = staticCompositionLocalOf {
    error("No instance of DataSaveInterface is provided, please call `CompositionLocalProvider(LocalDataSaver provides dataSaverPreferences){}` first.")
}

//</editor-fold>


//<editor-fold desc="State">

/**
 * A state which holds the value.  It implements the [MutableState] interface, so you can use
 * it just like a normal state. see [mutableDataSaverStateOf]
 *
 * When assign a new value to it, it will do data persistence according to [SavePolicy], which is [SavePolicy.IMMEDIATELY]
 * by default. If you want to save data manually, you can call [saveData].
 *
 * You can call `val (value, setValue) = state` to get its `set` function.
 *
 * @param T the class of data
 * @param dataSaverInterface the interface to read/save data, see [DataSaverInterface]
 * @param key persistence key
 * @param initialValue NOTE: YOU SHOULD READ THE SAVED VALUE AND PASSED IT AS THIS PARAMETER BY YOURSELF(see: [mutableDataSaverStateOf])
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async Boolean whether to save data asynchronously
 */
class DataSaverMutableState<T>(
    private val dataSaverInterface: DataSaverInterface,
    private val key: String,
    private val initialValue: T,
    private val savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    private val async: Boolean = false
) : MutableState<T> {
    private val state = mutableStateOf(initialValue)

    override var value: T
        get() = state.value
        set(value) {
            doSetValue(value)
        }

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        doSetValue(value)
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = state.value

    /**
     * This function will convert and save current data.
     * If `async` is true, it will `launch` a coroutine
     * to do that.
     */
    fun saveData() {
        if (value == null) {
            dataSaverInterface.remove(key)
            return
        }
        val value = value!!
        if (async) {
            scope.launch {
                val typeConverter = DataSaverConfig.typeSaveConverters[value::class.java]
                if (typeConverter != null) {
                    val convertedData = typeConverter(value)
                    log("saveConvertedData(async: $async): $key -> $value(as $convertedData)")
                    dataSaverInterface.saveDataAsync(key, convertedData)
                } else {
                    log("saveData(async: $async): $key -> $value")
                    dataSaverInterface.saveDataAsync(key, value)
                }
            }
        } else {
            val typeConverter = DataSaverConfig.typeSaveConverters[value::class.java]
            if (typeConverter != null) {
                val convertedData = typeConverter(value)
                log("saveConvertedData(async: $async): $key -> $value(as $convertedData)")
                dataSaverInterface.saveData(key, convertedData)
            } else {
                log("saveData(async: $async): $key -> $value")
                dataSaverInterface.saveData(key, value)
            }
        }
    }

    /**
     * remove the key and set the value to `replacement`
     * @param replacement List<T> new value of the state, `initialValue` by default
     */
    fun remove(replacement: T = initialValue) {
        dataSaverInterface.remove(key)
        state.value = replacement
    }

    fun valueChangedSinceInit() = state.value != initialValue

    private fun doSetValue(value: T) {
        val oldValue = this.state.value
        this.state.value = value
        if (oldValue != value && savePolicy == SavePolicy.IMMEDIATELY)
            saveData()
    }

    private fun log(msg: String) {
        if (DataSaverConfig.DEBUG) Log.d(TAG, msg)
    }

    companion object {
        const val TAG = "DataSaverState"

        private val scope by lazy(LazyThreadSafetyMode.PUBLICATION) {
            CoroutineScope(Dispatchers.IO)
        }
    }

    override operator fun component1() = state.value

    override operator fun component2(): (T) -> Unit = {
        doSetValue(it)
    }
}

/**
 * This function READ AND CONVERT the saved data and return a remembered [DataSaverMutableState].
 * Check the example in `README.md` to see how to use it.
 * ================================
 *
 * 此函数 **读取并转换** 已保存的数据，返回remember后的 [DataSaverMutableState]
 *
 * @param key String
 * @param initialValue T default value if it is initialized the first time
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async  whether to save data asynchronously
 * @return DataSaverMutableState<T>
 *
 * @see DataSaverMutableState
 */
@Composable
inline fun <reified T> rememberDataSaverState(
    key: String,
    initialValue: T,
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    async: Boolean = true
): DataSaverMutableState<T> {
    val saverInterface = LocalDataSaver.current
    var state: DataSaverMutableState<T>? = null
    DisposableEffect(key, savePolicy) {
        onDispose {
            if (DataSaverConfig.DEBUG)
                Log.d("rememberDataSaver", "rememberDataSaverState: onDisposed!")
            if (savePolicy == SavePolicy.DISPOSED && state != null && state!!.valueChangedSinceInit()) {
                state!!.saveData()
            }
        }
    }
    return remember(saverInterface, key, async) {
        mutableDataSaverStateOf(saverInterface, key, initialValue, savePolicy, async).also {
            state = it
        }
    }
}

/**
 * This function READ AND CONVERT the saved data and return a [DataSaverMutableState].
 * Check the example in `README.md` to see how to use it.
 *
 * 此函数 **读取并转换** 已保存的数据，返回 [DataSaverMutableState]
 *
 * @param key String
 * @param initialValue T default value if it is initialized the first time
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async  whether to save data asynchronously
 * @return DataSaverMutableState<T>
 *
 * @see DataSaverMutableState
 */
inline fun <reified T> mutableDataSaverStateOf(
    dataSaverInterface: DataSaverInterface,
    key: String,
    initialValue: T,
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    async: Boolean = true
): DataSaverMutableState<T> {
    val data = try {
        dataSaverInterface.readData(key, initialValue)
    } catch (e: Exception) {
        val restore = DataSaverConfig.typeRestoreConverters[T::class.java]
        restore ?: throw e
        val jsonData = dataSaverInterface.readData(key, "")
        if (jsonData == "") initialValue
        else restore(jsonData) as T
    }
    return DataSaverMutableState(dataSaverInterface, key, data, savePolicy, async)
}

//</editor-fold>


//<editor-fold desc="SateList">

/**
 * A state which holds a list as value. It implements the [MutableState] interface, so you can use
 * it just like a normal state.
 *
 * When assign a new value to it, it will do data persistence according to [SavePolicy], which is IMMEDIATELY
 * by default. If you want to save data manually, you can call [saveData].
 *
 * You can call `val (value, setValue) = state` to get its `set` function.
 *
 * @param T the class of each element in the list
 * @param dataSaverInterface the interface to read/save data, see [DataSaverInterface]
 * @param key persistence key
 * @param initialValue NOTE: YOU SHOULD READ THE SAVED VALUE AND PASSED IT AS THIS PARAMETER BY YOURSELF(see: [rememberDataSaverListState])
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async Boolean whether to save data asynchronously
 */
class DataSaverMutableListState<T>(
    private val dataSaverInterface: DataSaverInterface,
    private val key: String,
    private val initialValue: List<T> = emptyList(),
    private val savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    private val async: Boolean = false,
) : MutableState<List<T>> {
    private val list = mutableStateOf(initialValue)

    override var value: List<T>
        get() = list.value
        set(value) {
            doSetValue(value)
        }

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: List<T>) {
        doSetValue(value)
    }

    operator fun getValue(thisObj: Any?, property: KProperty<*>): List<T> = list.value

    fun saveData() {
        val value = value
        if (async) {
            scope.launch {
                dataSaverInterface.saveData(
                    key,
                    DataSaverConfig.convertListToString(value).also {
                        log("saveConvertedData(async: $async): $key -> $value(as $it)")
                    })
            }
        } else {
            dataSaverInterface.saveData(
                key,
                DataSaverConfig.convertListToString(value).also {
                    log("saveConvertedData(async: $async): $key -> $value(as $it)")
                })
        }
    }

    fun valueChangedSinceInit() = list.value.deepEquals(initialValue.toList())

    /**
     * remove the key and set the value to `replacement`
     * @param replacement List<T> new value of the state, `initialValue` by default
     */
    fun remove(replacement: List<T> = initialValue) {
        dataSaverInterface.remove(key)
        list.value = replacement
    }

    private fun doSetValue(value: List<T>) {
        val oldValue = this.list
        this.list.value = value
        if (oldValue != value && savePolicy == SavePolicy.IMMEDIATELY)
            saveData()
    }

    private fun log(msg: String) {
        if (DataSaverConfig.DEBUG) Log.d(TAG, msg)
    }

    companion object {

        const val TAG = "DataSaverState"
        private val scope by lazy(LazyThreadSafetyMode.PUBLICATION) {
            CoroutineScope(Dispatchers.IO)
        }
    }

    override fun component1(): List<T> = value

    override fun component2(): (List<T>) -> Unit = ::doSetValue
}

/**
 * This function READ AND CONVERT the saved data and return a remembered [DataSaverMutableListState].
 * Check the example in `README.md` to see how to use it.
 * -------------------------
 * 此函数 **读取并转换** 已保存的数据，返回remember后的State
 *
 * @param key String
 * @param initialValue T default value if it is initialized the first time
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async  whether to save data asynchronously
 * @return DataSaverMutableListState<T>
 */
@Composable
inline fun <reified T : Any> rememberDataSaverListState(
    key: String,
    initialValue: List<T>,
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    async: Boolean = true
): DataSaverMutableListState<T> {
    val saverInterface = LocalDataSaver.current
    var state: DataSaverMutableListState<T>? = null
    DisposableEffect(key, savePolicy) {
        onDispose {
            if (DataSaverConfig.DEBUG) Log.d(
                "rememberDataSaver",
                "rememberDataSaverState: onDisposed!"
            )
            if (savePolicy == SavePolicy.DISPOSED && state != null && state!!.valueChangedSinceInit()) {
                state!!.saveData()
            }
        }
    }
    return remember(saverInterface, key, async) {
        mutableDataSaverListStateOf(
            saverInterface,
            key,
            initialValue,
            savePolicy,
            async
        ).also { state = it }
    }
}

/**
 * This function READ AND CONVERT the saved data and return a [DataSaverMutableListState].
 * Check the example in `README.md` to see how to use it.
 * -------------------------
 * 此函数 **读取并转换** 已保存的数据，返回 [DataSaverMutableListState]
 *
 * @param key String
 * @param initialValue T default value if no data persistence has been done
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async  whether to save data asynchronously
 * @return DataSaverMutableListState<T>
 */
inline fun <reified T> mutableDataSaverListStateOf(
    dataSaverInterface: DataSaverInterface,
    key: String,
    initialValue: List<T> = emptyList(),
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    async: Boolean = false,
): DataSaverMutableListState<T> {
    val data = try {
        dataSaverInterface.readData(key, initialValue)
    } catch (e: Exception) {
        val restore = DataSaverConfig.typeRestoreConverters[T::class.java]
        restore ?: throw e
        val strData = dataSaverInterface.readData(key, "")
        if (strData == "") initialValue
        else DataSaverConfig.convertStringToList<T>(strData)
    }
    return DataSaverMutableListState(dataSaverInterface, key, data, savePolicy, async)
}

internal fun <T> List<T>.deepEquals(other: List<T>): Boolean {
    if (size != other.size) return false
    for (i in indices) {
        if (this[i] != other[i]) return false
    }
    return true
}

//</editor-fold>