package io.wongxd.solution.baseView

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import io.wongxd.solution.fragmentation_kt.*
import io.wongxd.solution.fragmentation_kt.animation.FragmentAnimator
import io.wongxd.solution.util.AppManager
import io.wongxd.solution.util.statusBar
import io.wongxd.solution.util.statusPadding
import java.lang.ref.WeakReference
import java.lang.reflect.Method


//防止虚拟建遮挡tab
class AndroidWorkaround private constructor(private val mChildOfContent: View) {
    private var usableHeightPrevious = 0
    private val frameLayoutParams: ViewGroup.LayoutParams

    init {
        mChildOfContent.viewTreeObserver.addOnGlobalLayoutListener { possiblyResizeChildOfContent() }
        frameLayoutParams = mChildOfContent.layoutParams
    }

    private fun possiblyResizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        if (usableHeightNow != usableHeightPrevious) {
            frameLayoutParams.height = usableHeightNow
            mChildOfContent.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    private fun computeUsableHeight(): Int {
        val r = Rect()
        mChildOfContent.getWindowVisibleDisplayFrame(r)
        return r.bottom
    }

    companion object {
        fun assistActivity(content: View) {
            AndroidWorkaround(content)
        }

        fun checkDeviceHasNavigationBar(context: Context): Boolean {
            var hasNavigationBar = false
            val rs: Resources = context.getResources()
            val id: Int = rs.getIdentifier("config_showNavigationBar", "bool", "android")
            if (id > 0) {
                hasNavigationBar = rs.getBoolean(id)
            }
            try {
                val systemPropertiesClass = Class.forName("android.os.SystemProperties")
                val m: Method = systemPropertiesClass.getMethod("get", String::class.java)
                val navBarOverride = m.invoke(systemPropertiesClass, "qemu.hw.mainkeys") as String
                if ("1" == navBarOverride) {
                    hasNavigationBar = false
                } else if ("0" == navBarOverride) {
                    hasNavigationBar = true
                }
            } catch (e: java.lang.Exception) {
            }
            return hasNavigationBar
        }
    }
}

abstract class BaseRootActivityLast : BaseRootActivity2() {
    companion object {
        internal var activityCount = 0
        var afterForeground2Back: (() -> Unit)? = null
        var afterBack2Foreground: (() -> Unit)? = null
    }

    // <editor-fold desc="状态栏">

    open val needSetStatusBarByBase = false

    open val setStatusBarLightMode = true

    open val insert2StatusBar = false

    open val fakeStatusBar = false

    open val statusBarColorRes = android.R.color.white

    open val statusBarColor: Int? = null

    open fun dealStatusBar() {
        try {
            if (needSetStatusBarByBase) {
                statusBar
                    .color(
                        if (insert2StatusBar) Color.TRANSPARENT
                        else statusBarColor ?: resources.getColor(statusBarColorRes)
                    )
                    .light(setStatusBarLightMode)
                    .insert2StatusBar(insert2StatusBar)
                    .userFakeStatusBar(fakeStatusBar)
                    .apply()
            }
        } catch (e: Exception) {
        }
    }

    open fun addStatusBarTopPadding(view: View) {
        view.statusPadding()
    }

    override fun onResume() {
        super.onResume()
        dealStatusBar()
    }

    // </editor-fold>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCount++

        //适配华为手机虚拟键遮挡tab的问题,需要在setContentView()方法后面执行
        if (AndroidWorkaround.checkDeviceHasNavigationBar(this)) {
            AndroidWorkaround.assistActivity(findViewById(android.R.id.content))
        }
    }

    override fun onDestroy() {
        activityCount--
        super.onDestroy()
    }
}

abstract class BaseRootActivity2 : BaseRootActivity1(), ISupportActivity {

    override val supportDelegate by lazy { SupportActivityDelegate(this) }

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag, 操作非回退栈Fragment
     */
    override fun extraTransaction(): ExtraTransaction? {
        return supportDelegate.extraTransaction()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportDelegate.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportDelegate.onPostCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        supportDelegate.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        supportDelegate.onDestroy()
    }

    /**
     * Get all fragments animation.
     *
     * @return FragmentAnimator
     */
    /**
     * Set all fragments animation.
     * 设置Fragment内的全局动画
     */
    override var fragmentAnimator: FragmentAnimator
        get() = supportDelegate.fragmentAnimator
        set(fragmentAnimator) {
            supportDelegate.fragmentAnimator = fragmentAnimator ?: FragmentAnimator()
        }

    /**
     * Note：super.dispatchTouchEvent(ev);
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 不建议复写该方法,请使用 [.onBackPressedSupport] 代替
     */
    final override fun onBackPressed() {
        supportDelegate.onBackPressed()
    }

    /**
     * 该方法回调时机为,Activity回退栈内Fragment的数量 小于等于1 时,默认finish Activity
     * 请尽量复写该方法,避免复写onBackPress(),以保证SupportFragment内的onBackPressedSupport()回退事件正常执行
     */
    override fun onBackPressedSupport() {
        supportDelegate.onBackPressedSupport()
    }

    override fun post(runnable: Runnable) {
        supportDelegate.post(runnable)
    }

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
     */
    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment) {
        supportDelegate.loadRootFragment(containerId, toFragment)
    }

    /**
     * 加载多个同级根Fragment
     */
    fun loadMultipleRootFragment(
        containerId: Int,
        showPosition: Int,
        vararg toFragments: ISupportFragment
    ) {
        supportDelegate.loadMultipleRootFragment(containerId, showPosition, *toFragments)
    }

    /**
     * show一个Fragment,hide其他同栈所有Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment,(只有通过loadMultipleRootFragment()载入的Fragment)
     *
     *
     * 建议使用更明确的[.showHideFragment]
     *
     * @param showFragment 需要show的Fragment
     */
    fun showHideFragment(showFragment: ISupportFragment) {
        supportDelegate.showHideFragment(showFragment)
    }

    /**
     * show一个Fragment,hide一个Fragment
     */
    fun showHideFragment(showFragment: ISupportFragment, hideFragment: ISupportFragment?) {
        supportDelegate.showHideFragment(showFragment, hideFragment)
    }

    /**
     * It is recommended to use [SupportFragment.start].
     */
    fun start(toFragment: ISupportFragment) {
        supportDelegate.start(toFragment)
    }

    /**
     * It is recommended to use [SupportFragment.start].
     *
     * @param launchMode Similar to Activity's LaunchMode.
     */
    fun start(toFragment: ISupportFragment, @ISupportFragment.LaunchMode launchMode: Int) {
        supportDelegate.start(toFragment, launchMode)
    }

    /**
     * It is recommended to use [SupportFragment.startForResult].
     * Launch an fragment for which you would like a result when it popped.
     */
    fun startForResult(toFragment: ISupportFragment, requestCode: Int) {
        supportDelegate.startForResult(toFragment, requestCode)
    }

    /**
     * It is recommended to use [SupportFragment.startWithPop].
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: ISupportFragment) {
        supportDelegate.startWithPop(toFragment)
    }

    /**
     * It is recommended to use [SupportFragment.startWithPopTo].
     *
     * @see .popTo
     * @see .start
     */
    fun startWithPopTo(
        toFragment: ISupportFragment,
        targetFragmentClass: Class<*>?,
        includeTargetFragment: Boolean
    ) {
        supportDelegate.startWithPopTo(toFragment, targetFragmentClass, includeTargetFragment)
    }

    /**
     * It is recommended to use [SupportFragment.replaceFragment].
     */
    fun replaceFragment(toFragment: ISupportFragment) {
        supportDelegate.replaceFragment(toFragment)
    }

    /**
     * Pop the fragment.
     */
    fun pop() {
        supportDelegate.pop()
    }

    /**
     * 出栈到目标fragment
     *
     * @param targetFragmentClass   目标fragment
     * @param includeTargetFragment 是否包含该fragment
     */
    fun popTo(targetFragmentClass: Class<*>, includeTargetFragment: Boolean) {
        supportDelegate.popTo(targetFragmentClass, includeTargetFragment)
    }

    /**
     * If you want to begin another FragmentTransaction immediately after popTo(), use this method.
     * 如果你想在出栈后, 立刻进行FragmentTransaction操作，请使用该方法
     */
    fun popTo(
        targetFragmentClass: Class<*>,
        includeTargetFragment: Boolean,
        afterPopTransactionRunnable: Runnable?
    ) {
        supportDelegate.popTo(
            targetFragmentClass,
            includeTargetFragment,
            afterPopTransactionRunnable
        )
    }

    /**
     * 得到位于栈顶Fragment
     */
    val topFragment: ISupportFragment?
        get() = SupportHelper.getTopFragment(supportFragmentManager)

    /**
     * 获取栈内的fragment对象
     */
    fun <T : ISupportFragment?> findFragment(fragmentClass: Class<T>?): T? {
        return SupportHelper.findFragment(supportFragmentManager, fragmentClass)
    }

    /**
     * 当Fragment根布局 没有 设定background属性时,
     * Fragmentation默认使用Theme的android:windowbackground作为Fragment的背景,
     * 可以通过该方法改变其内所有Fragment的默认背景。
     */
    fun setDefaultFragmentBackground(@DrawableRes backgroundRes: Int) {
        supportDelegate.defaultFragmentBackground = backgroundRes
    }
}

abstract class BaseRootActivity1 : AppCompatActivity() {
    private var weakReference: WeakReference<Activity>? = null

    private var isActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weakReference = WeakReference(this)
        AppManager.activityStack.add(weakReference)
    }

    override fun onRestart() {
        super.onRestart()
        if (!isActive) {
            BaseRootActivityLast.afterBack2Foreground?.invoke()
            isActive = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (BaseRootActivityLast.activityCount == 0) {
            BaseRootActivityLast.afterForeground2Back?.invoke()
            isActive = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppManager.activityStack.remove(weakReference)
    }

}