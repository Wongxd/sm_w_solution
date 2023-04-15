
package io.wongxd.solution.fragmentation_kt

import android.os.Bundle
import android.view.MotionEvent
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import io.wongxd.solution.fragmentation_kt.ISupportFragment.LaunchMode
import io.wongxd.solution.fragmentation_kt.animation.FragmentAnimator

/**
 * Base class for activities that use the support-based
 * [ISupportActivity] and
 * [AppCompatActivity] APIs.
 */
internal abstract class SupportActivity : AppCompatActivity(), ISupportActivity {

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
    fun start(toFragment: ISupportFragment, @LaunchMode launchMode: Int) {
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