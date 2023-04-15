package io.wongxd.solution.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.*


private const val COLOR_TRANSPARENT = 0

//<editor-fold desc="状态栏颜色">

/** 设置状态栏颜色 */
private fun Activity.statusBarColor(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window?.statusBarColor = color
    }
}

/** 设置状态栏颜色 */
private fun Activity.statusBarColorRes(@ColorRes colorRes: Int) =
    statusBarColor(ContextCompat.getColor(this, colorRes))

//</editor-fold>

// <editor-fold desc="透明状态栏">
/**
 * 半透明状态栏
 * 会导致键盘遮挡输入框, 为根布局设置[View.setFitsSystemWindows]为true可以解决
 *
 * @param translucent 是否显示透明状态栏
 * @param darkMode 是否显示暗色状态栏文字颜色
 */
@JvmOverloads
private fun Activity.translucent(
    translucent: Boolean = true,
    darkMode: Boolean? = null
) {
    if (Build.VERSION.SDK_INT >= 19) {
        if (translucent) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }
    if (darkMode != null) {
        darkMode(darkMode)
    }
}

/**
 * 使用视图的背景色作为状态栏颜色
 */
@JvmOverloads
private fun Activity.immersive(view: View, darkMode: Boolean? = null) {
    val background = view.background
    if (background is ColorDrawable) {
        immersive(background.color, darkMode)
    }
}

/**
 * 设置透明状态栏或者状态栏颜色, 会导致状态栏覆盖界面
 *
 * 如果不指定状态栏颜色则会应用透明状态栏(全屏属性), 会导致键盘遮挡输入框
 *
 * @param color 状态栏颜色, 不指定则为透明状态栏
 * @param darkMode 是否显示暗色状态栏文字颜色
 */
@SuppressLint("ObsoleteSdkInt")
@JvmOverloads
private fun Activity.immersive(
    @ColorInt color: Int = COLOR_TRANSPARENT,
    darkMode: Boolean? = null
) {
    when {
        Build.VERSION.SDK_INT >= 21 -> {
            when (color) {
                COLOR_TRANSPARENT -> {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    var systemUiVisibility = window.decorView.systemUiVisibility
                    systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    window.decorView.systemUiVisibility = systemUiVisibility
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = color
                }
                else -> {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    var systemUiVisibility = window.decorView.systemUiVisibility
                    systemUiVisibility =
                        systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    window.decorView.systemUiVisibility = systemUiVisibility
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = color
                }
            }
        }
        Build.VERSION.SDK_INT >= 19 -> {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            if (color != COLOR_TRANSPARENT) {
                setTranslucentView(window.decorView as ViewGroup, color)
            }
        }
    }
    if (darkMode != null) {
        darkMode(darkMode)
    }
}

/**
 * 获取颜色资源值来设置状态栏
 */
@JvmOverloads
private fun Activity.immersiveRes(@ColorRes color: Int, darkMode: Boolean? = null) =
    immersive(resources.getColor(color), darkMode)

// </editor-fold>

//<editor-fold desc="暗色模式">


/**
 * 开关状态栏暗色模式, 并不会透明状态栏, 只是单纯的状态栏文字变暗色调.
 *
 * @param darkMode 状态栏文字是否为暗色
 */
@JvmOverloads
private fun Activity.darkMode(darkMode: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var systemUiVisibility = window.decorView.systemUiVisibility
        systemUiVisibility = if (darkMode) {
            systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        window.decorView.systemUiVisibility = systemUiVisibility
    }
}

@JvmOverloads
private fun Activity.light(light: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (light) {
            //设置statusBar字体颜色
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            //恢复默认statusBar文字颜色
            window.decorView.systemUiVisibility = View.VISIBLE
        }
    }

}

//</editor-fold>

//<editor-fold desc="侵入布局">

/**
 * 通过设置全屏，设置状态栏透明
 *
 */
private fun Activity.fullScreen2makeStatusTrans() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        val activity = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
            val window: Window = activity.window
            val decorView: View = window.decorView
            //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
            val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            decorView.systemUiVisibility = option
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            //导航栏颜色也可以正常设置
//                window.setNavigationBarColor(Color.TRANSPARENT);
        } else {
            val window: Window = activity.window
            val attributes: WindowManager.LayoutParams = window.attributes
            val flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            val flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            attributes.flags = attributes.flags or flagTranslucentStatus
            //                attributes.flags |= flagTranslucentNavigation;
            window.attributes = attributes
        }
    }
}


/**
 * desc:状态栏设置 设置布局侵入到状态栏
 */
private fun Activity.setContentIntoStatusBar(@ColorInt color: Int = Color.TRANSPARENT) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val window: Window = window
        val decorView = window.decorView
        decorView.setOnApplyWindowInsetsListener { v, insets ->
            val defaultInsets: WindowInsets = v.onApplyWindowInsets(insets)
            defaultInsets.replaceSystemWindowInsets(
                defaultInsets.systemWindowInsetLeft,
                0,
                defaultInsets.systemWindowInsetRight,
                defaultInsets.systemWindowInsetBottom
            )
        }
        ViewCompat.requestApplyInsets(decorView)
        //将状态栏设成透明，如不想透明可设置其他颜色
        window.statusBarColor = color
    }
}

private fun Activity.setContentIntoStatusBarRes(@ColorRes colorRes: Int = android.R.color.transparent) {
    setContentIntoStatusBar(ContextCompat.getColor(this, colorRes))
}

/**
 * desc:状态栏设置 设置布局不侵入到状态栏
 */
private fun Activity.setContentNotIntoStatusBar(@ColorInt color: Int = Color.TRANSPARENT) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val decorView = window.decorView
        decorView.setOnApplyWindowInsetsListener { v, insets ->
            val defaultInsets: WindowInsets = v.onApplyWindowInsets(insets)
            defaultInsets.replaceSystemWindowInsets(
                defaultInsets.systemWindowInsetLeft,
                defaultInsets.systemWindowInsetTop,
                defaultInsets.systemWindowInsetRight,
                defaultInsets.systemWindowInsetBottom
            )
        }
        ViewCompat.requestApplyInsets(decorView)
        //将状态栏设成透明，如不想透明可设置其他颜色
        window.statusBarColor = color
    }
}

private fun Activity.setContentNotIntoStatusBarRes(colorRes: Int = android.R.color.transparent) {
    setContentNotIntoStatusBar(ContextCompat.getColor(this, colorRes))
}

//</editor-fold>

// <editor-fold desc="间距">

/**
 * 增加View的上内边距, 增加高度为状态栏高度, 防止视图和状态栏重叠
 * 如果是RelativeLayout设置padding值会导致centerInParent等属性无法正常显示
 *
 * @param remove true: paddingTop = 状态栏高度
 *               false: paddingTop = 0
 */
@JvmOverloads
fun View.statusPadding(remove: Boolean = false) {
    if (this is RelativeLayout) {
        throw UnsupportedOperationException("Unsupported set statusPadding for RelativeLayout")
    }
    if (Build.VERSION.SDK_INT >= 19) {
        val statusBarHeight = context.statusBarHeight
        val lp = layoutParams
        if (lp != null && lp.height > 0) {
            lp.height += statusBarHeight //增高
        }
        if (remove) {
            if (paddingTop < statusBarHeight) return
            setPadding(
                paddingLeft, paddingTop - statusBarHeight,
                paddingRight, paddingBottom
            )
        } else {
            if (paddingTop >= statusBarHeight) return
            setPadding(
                paddingLeft, paddingTop + statusBarHeight,
                paddingRight, paddingBottom
            )
        }
    }
}

@JvmOverloads
fun View.navigationBarPadding(remove: Boolean = false) {
    if (this is RelativeLayout) {
        throw UnsupportedOperationException("Unsupported set statusPadding for RelativeLayout")
    }
    if (Build.VERSION.SDK_INT >= 19) {
        val navigationBarHeight = context.navigationBarHeight
        val lp = layoutParams
        if (lp != null && lp.height > 0) {
            lp.height += navigationBarHeight //增高
        }
        if (remove) {
            if (paddingBottom < navigationBarHeight) return
            setPadding(
                paddingLeft, paddingTop,
                paddingRight, paddingBottom - navigationBarHeight
            )
        } else {
            if (paddingBottom >= navigationBarHeight) return
            setPadding(
                paddingLeft, paddingTop,
                paddingRight, paddingBottom + navigationBarHeight
            )
        }
    }
}

//</editor-fold>

// <editor-fold desc="假状态栏">

/**
 * 创建假的透明栏
 */
private fun Context.setTranslucentView(container: ViewGroup, color: Int) {
    if (Build.VERSION.SDK_INT >= 19) {
        var simulateStatusBar: View? = container.findViewById(android.R.id.custom)
        if (simulateStatusBar == null && color != 0) {
            simulateStatusBar = View(container.context)
            simulateStatusBar.id = android.R.id.custom
            val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)
            container.addView(simulateStatusBar, lp)
        }
        simulateStatusBar?.setBackgroundColor(color)
    }
}

private class FakeStatusBar(context: Context) : View(context)

private fun Activity.fakeStatusBar(): View {
    //条件状态栏透明，要不然不会起作用
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

    //去除Android 5.0以上状态栏半透明的效果
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    //获取根布局
    val systemContent: ViewGroup = findViewById(android.R.id.content)
    val userContent = systemContent.getChildAt(0) as ViewGroup
    userContent.fitsSystemWindows = false

    var mStatusBarView: FakeStatusBar? = null
    if (userContent.getChildAt(0) is FakeStatusBar) {
        mStatusBarView = userContent.getChildAt(0) as FakeStatusBar
    }
    if (mStatusBarView == null) {
        mStatusBarView = FakeStatusBar(this)
        val screenWidth: Int = resources.displayMetrics.widthPixels
        val statusBarHeight: Int = statusBarHeight
        val params = ViewGroup.LayoutParams(screenWidth, statusBarHeight)
        mStatusBarView.layoutParams = params
        mStatusBarView.requestLayout()

        //添加
        userContent.addView(mStatusBarView, 0)
    }
    return mStatusBarView
}

private fun Activity.setFakeStatusBarVisible(show: Boolean) {
    //获取根布局
    val systemContent: ViewGroup = findViewById(android.R.id.content)
    val userContent = systemContent.getChildAt(0) as ViewGroup

    var mStatusBarView: FakeStatusBar? = null
    if (userContent.getChildAt(0) is FakeStatusBar) {
        mStatusBarView = userContent.getChildAt(0) as FakeStatusBar
    }

    mStatusBarView?.let {
        it.visibility = if (show) View.VISIBLE else View.GONE
    }
}

private fun Activity.getFakeStatusBar(): FakeStatusBar? {
    //获取根布局
    val systemContent: ViewGroup = findViewById(android.R.id.content)
    val userContent = systemContent.getChildAt(0) as ViewGroup

    var mStatusBarView: FakeStatusBar? = null
    if (userContent.getChildAt(0) is FakeStatusBar) {
        mStatusBarView = userContent.getChildAt(0) as FakeStatusBar
    }

    return mStatusBarView
}

//</editor-fold>

//<editor-fold desc="ActionBar">

/**
 * 设置ActionBar的背景颜色
 */
fun AppCompatActivity.setActionBarBackground(@ColorInt color: Int) {
    supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
}

fun AppCompatActivity.setActionBarBackgroundRes(@ColorRes color: Int) {
    supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(color)))
}

/**
 * 设置ActionBar的背景颜色为透明
 */
fun AppCompatActivity.setActionBarTransparent() {
    supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
}
//</editor-fold>

// <editor-fold desc="辅助">

/**
 * 显示或隐藏导航栏, 系统开启可以隐藏, 系统未开启不能开启
 *
 * @param enabled 是否显示导航栏
 */
@JvmOverloads
fun Activity.setNavigationBar(enabled: Boolean = true) {
    if (Build.VERSION.SDK_INT in 12..18) {
        if (enabled) {
            window.decorView.systemUiVisibility = View.VISIBLE
        } else {
            window.decorView.systemUiVisibility = View.GONE
        }
    } else if (Build.VERSION.SDK_INT >= 19) {
        val systemUiVisibility = window.decorView.systemUiVisibility
        if (enabled) {
            window.decorView.systemUiVisibility =
                systemUiVisibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            window.decorView.systemUiVisibility = systemUiVisibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }
}

/**
 * 设置是否全屏
 *
 * @param enabled 是否全屏显示
 */
@JvmOverloads
fun Activity.setFullscreen(enabled: Boolean = true) {
    val systemUiVisibility = window.decorView.systemUiVisibility
    window.decorView.systemUiVisibility = if (enabled) {
        systemUiVisibility or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    } else {
        systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE and View.SYSTEM_UI_FLAG_FULLSCREEN.inv()
    }
}


/**
 * 沉浸模式（全屏模式）
 * 设置全屏的方法
 * 参数window在activity或AppCompatActivity都带有
 */
fun Activity.immersionFull() {
    hideSystemBars(window)
    useSpecialScreen(window)
}

/**
 * 隐藏状态栏，显示内容上移到状态栏
 */
private fun hideSystemBars(window: Window) {
    //占满全屏，activity绘制将状态栏也加入绘制范围。
    //如此即使使用BEHAVIOR_SHOW_BARS_BY_SWIPE或BEHAVIOR_SHOW_BARS_BY_TOUCH
    //也不会因为状态栏的显示而导致activity的绘制区域出现变形
    //使用刘海屏也需要这一句进行全屏处理
    WindowCompat.setDecorFitsSystemWindows(window, false)
    //隐藏状态栏和导航栏 以及交互
    WindowInsetsControllerCompat(window, window.decorView).let {
        //隐藏状态栏和导航栏
        //用于WindowInsetsCompat.Type.systemBars()隐藏两个系统栏
        //用于WindowInsetsCompat.Type.statusBars()仅隐藏状态栏
        //用于WindowInsetsCompat.Type.navigationBars()仅隐藏导航栏
        it.hide(WindowInsetsCompat.Type.systemBars())
        //交互效果
        //BEHAVIOR_SHOW_BARS_BY_SWIPE 下拉状态栏操作可能会导致activity画面变形
        //BEHAVIOR_SHOW_BARS_BY_TOUCH 未测试到与BEHAVIOR_SHOW_BARS_BY_SWIPE的明显差异
        //BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE 下拉或上拉的屏幕交互操作会显示状态栏和导航栏
        it.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

/**
 * 扩展使用刘海屏
 */
private fun useSpecialScreen(window: Window) {
    //允许window 的内容可以上移到刘海屏状态栏
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val lp = window.attributes
        lp.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = lp
    }
}


/**
 * 是否有导航栏
 */
val Activity?.isNavigationBar: Boolean
    get() {
        this ?: return false
        val vp = window.decorView as? ViewGroup
        if (vp != null) {
            for (i in 0 until vp.childCount) {
                vp.getChildAt(i).context.packageName
                if (vp.getChildAt(i).id != -1 && "navigationBarBackground" ==
                    resources.getResourceEntryName(vp.getChildAt(i).id)
                ) return true
            }
        }
        return false
    }

/**
 * 如果当前设备存在导航栏返回导航栏高度, 否则0
 */
val Context?.navigationBarHeight: Int
    get() {
        this ?: return 0
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        var height = 0
        if (resourceId > 0) {
            height = resources.getDimensionPixelSize(resourceId)
        }
        return height
    }


/**
 * 状态栏高度
 */
val Context?.statusBarHeight: Int
    get() {
        this ?: return 0
        var result = 24
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
        result = if (resId > 0) {
            resources.getDimensionPixelSize(resId)
        } else {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                result.toFloat(), Resources.getSystem().displayMetrics
            ).toInt()
        }
        return result
    }
// </editor-fold>

//<editor-fold desc="使用">

val Activity.statusBar: StatusBarOption get() = StatusBarOption.with(this)

/**
 * A small library for configuring status bar color in Fragment.
 *
 * It can be useful in Navigation Component where we have only one Activity but many Fragments.
 *
 *
 *    class MyFragment: Fragment() {
 *
 *       override fun onCreate(savedInstanceState: Bundle?) {
 *             super.onCreate(savedInstanceState)
 *             // A simple extension
 *            statusBar.color(Color.WHITE).light(true).apply()
 *        }
 *    }
 */

//https://github.com/seanghay/statusbar
//val Fragment.statusBar: FgtStatusBar get() = FgtStatusBar.with(this)

val Fragment.statusBar: StatusBarOption get() = StatusBarOption.with(requireActivity())

class FgtStatusBar private constructor(private val fragment: Fragment) {

    private val fragmentStatusBarLO = FragmentStatusBarLO(fragment)

    init {
        fragment.lifecycle.addObserver(fragmentStatusBarLO)
    }

    private val statusBarConfig = StatusBarConfig()

    fun color(color: Int): FgtStatusBar {
        statusBarConfig.color = color
        return this
    }

    fun colorRes(colorRes: Int): FgtStatusBar {
        return color(ContextCompat.getColor(fragment.requireContext(), colorRes))
    }

    fun light(isLight: Boolean): FgtStatusBar {
        statusBarConfig.isLight = isLight
        return this
    }

    fun insert2StatusBar(insert2StatusBar: Boolean): FgtStatusBar {
        statusBarConfig.insert2StatusBar = insert2StatusBar
        return this
    }

    fun userFakeStatusBar(userFakeStatusBar: Boolean): FgtStatusBar {
        statusBarConfig.userFakeStatusBar = userFakeStatusBar
        return this
    }

    fun apply() {
        fragmentStatusBarLO.statusBarConfig = statusBarConfig
    }

    companion object {
        @JvmStatic
        fun with(fragment: Fragment): FgtStatusBar {
            return FgtStatusBar(fragment)
        }
    }
}

private data class StatusBarConfig(
    @ColorInt var color: Int = Color.TRANSPARENT,
    var isLight: Boolean = true,
    var insert2StatusBar: Boolean = false,
    var userFakeStatusBar: Boolean = false
)

private class FragmentStatusBarLO(private val fragment: Fragment) : LifecycleObserver {

    private val statusBarOption by lazy { StatusBarOption.with(fragment.requireActivity()) }

    var statusBarConfig: StatusBarConfig? = null

    private fun apply() {
        statusBarConfig?.let { config ->
            statusBarOption.color(config.color)
                .light(config.isLight)
                .userFakeStatusBar(config.userFakeStatusBar)
                .insert2StatusBar(config.insert2StatusBar)
                .apply()
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onStart() {
        Log.d(
            "statusBar",
            "statusBar#onResume:${fragment.javaClass.simpleName},config:${statusBarConfig},isWhite:${statusBarConfig?.color == Color.WHITE}"
        )
        apply()
    }
}

class StatusBarOption private constructor(private val aty: Activity) {

    private var config = StatusBarConfig()

    fun color(color: Int): StatusBarOption {
        config.color = color
        return this
    }

    fun colorRes(colorRes: Int): StatusBarOption {
        color(ContextCompat.getColor(aty, colorRes))
        return this
    }

    fun light(isLight: Boolean): StatusBarOption {
        config.isLight = isLight
        return this
    }

    fun userFakeStatusBar(userFakeStatusBar: Boolean): StatusBarOption {
        config.userFakeStatusBar = userFakeStatusBar
        return this
    }

    fun insert2StatusBar(insert2StatusBar: Boolean): StatusBarOption {
        config.insert2StatusBar = insert2StatusBar
        return this
    }

    fun apply() {
        StatusBarController(aty).set(config)
    }

    companion object {
        @JvmStatic
        fun with(aty: Activity): StatusBarOption {
            return StatusBarOption(aty)
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
private class StatusBarController(private val activity: Activity) {

    @get:ColorInt
    private var color: Int
        set(value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.window.statusBarColor = value
            }
        }
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.window.statusBarColor
            } else Color.BLACK
        }

    private var isLight: Boolean
        set(value) {
            if (value) {
                if (containsLightFlag()) return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.window.decorView.systemUiVisibility =
                        activity.window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            } else {

                if (!containsLightFlag()) {
                    return
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.window.decorView.systemUiVisibility =
                        activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
        }
        get() = containsLightFlag()

    private fun containsLightFlag(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR != 0
        } else {
            false
        }
    }

    private var insert2StatusBar: Boolean
        set(value) {

            val decorView = activity.window.decorView

            if (value) {
                decorView.setOnApplyWindowInsetsListener { v, insets ->
                    val defaultInsets: WindowInsets = v.onApplyWindowInsets(insets)
                    defaultInsets.replaceSystemWindowInsets(
                        defaultInsets.systemWindowInsetLeft,
                        0,
                        defaultInsets.systemWindowInsetRight,
                        defaultInsets.systemWindowInsetBottom
                    )
                }
            } else {
                decorView.setOnApplyWindowInsetsListener { v, insets ->
                    val defaultInsets: WindowInsets = v.onApplyWindowInsets(insets)
                    defaultInsets.replaceSystemWindowInsets(
                        defaultInsets.systemWindowInsetLeft,
                        defaultInsets.systemWindowInsetTop,
                        defaultInsets.systemWindowInsetRight,
                        defaultInsets.systemWindowInsetBottom
                    )
                }
            }

            ViewCompat.requestApplyInsets(decorView)
        }
        get() = containsInsert2StatusBarFlag()

    private fun containsInsert2StatusBarFlag(): Boolean {
        val decorView = activity.window.decorView
        val defaultInsets: WindowInsets? =
            ViewCompat.getRootWindowInsets(decorView)?.toWindowInsets()
        defaultInsets?.let {
            if (it.systemWindowInsetTop != 0)
                return true
        }
        return false
    }

    private var userFakeStatusBar: Boolean
        set(value) {
            if (value) {
                activity.fakeStatusBar().setBackgroundColor(color)
            }
        }
        get() {
            return activity.getFakeStatusBar() != null
        }

    private var hideFakeStatusBar: Boolean
        set(value) {
            activity.setFakeStatusBarVisible(value)
        }
        get() {
            val fakeStatusBar = activity.getFakeStatusBar()
            return fakeStatusBar != null && fakeStatusBar.visibility == View.VISIBLE
        }


    fun set(config: StatusBarConfig?) {
        config?.let { realConfig ->
            Log.d("statusBar", "statusBar:${realConfig}")

            color = realConfig.color
            userFakeStatusBar = realConfig.userFakeStatusBar
            hideFakeStatusBar = realConfig.insert2StatusBar
            isLight = realConfig.isLight
            insert2StatusBar = realConfig.insert2StatusBar

            //深色模式 特殊处理
            if ((activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                isLight = false
            }

        }
    }
}

//</editor-fold>