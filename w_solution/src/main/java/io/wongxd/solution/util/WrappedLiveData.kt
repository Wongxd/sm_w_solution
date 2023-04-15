package io.wongxd.solution.util

import androidx.annotation.MainThread
import androidx.lifecycle.*

open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    private var map = HashMap<ViewModelStore, Boolean>()

    /**
     * 整个事件只需要唯一观察者消费
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 每个观察者仅能消费一次事件
     * 根据同观察者判断事件是否消费
     * 如果该观察者已消费数据，则返回null
     * 否则标记已消费并返回数据
     */
    fun getContentIfNotHandled(viewModelStore: ViewModelStore): T? {
        return if (map.contains(viewModelStore)) {
            null
        } else {
            map[viewModelStore] = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

//为 LiveData<Event<T>>提供类型别名，使用 EventLiveData<T> 即可
typealias EventMutableLiveData<T> = MutableLiveData<Event<T>>

typealias EventLiveData<T> = LiveData<Event<T>>

/**
 * 事件可被多个观察者消费，且每个观察者 [viewModelStore] 仅能消费一次
 *
 */
@MainThread
inline fun <T> EventLiveData<T>.observeSingleEvent(
    owner: LifecycleOwner,
    viewModelStore: ViewModelStore,
    crossinline onChanged: (T) -> Unit
): Observer<Event<T>> {
    val wrappedObserver = Observer<Event<T>> { t ->
        //数据没有被使用过则发送给调用者，否则不处理
        t.getContentIfNotHandled(viewModelStore)?.let { data ->
            onChanged.invoke(data)
        }
    }
    observe(owner, wrappedObserver)
    return wrappedObserver
}

/**
 * 整个事件只能被唯一观察者消费
 */
@MainThread
inline fun <T> EventLiveData<T>.observeSingleEvent(
    owner: LifecycleOwner,
    crossinline onChanged: (T) -> Unit
): Observer<Event<T>> {
    val wrappedObserver = Observer<Event<T>> { t ->
        //数据没有被使用过则发送给调用者，否则不处理
        t.getContentIfNotHandled()?.let { data ->
            onChanged.invoke(data)
        }
    }
    observe(owner, wrappedObserver)
    return wrappedObserver
}

/**
 * 不考虑粘性问题，和 UI 数据一样，每次都通知观察者
 */
@MainThread
inline fun <T> EventLiveData<T>.observeEvent(
    owner: LifecycleOwner,
    crossinline onChanged: (T) -> Unit
): Observer<Event<T>> {
    val wrappedObserver = Observer<Event<T>> { t ->
        onChanged.invoke(t.peekContent())
    }
    observe(owner, wrappedObserver)
    return wrappedObserver
}

fun <T> EventMutableLiveData<T>.postEventValue(value: T) {
    postValue(Event(value))
}

fun <T> EventMutableLiveData<T>.setEventValue(value: T) {
    setValue(Event(value))
}