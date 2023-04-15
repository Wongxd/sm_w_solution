
package io.wongxd.solution.fragmentation_kt

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import io.wongxd.solution.fragmentation_kt.ISupportFragment.LaunchMode

/**
 * Base class for activities that use the support-based
 * [ISupportFragment] and
 * [Fragment] APIs.
 */
internal abstract class SupportFragment : Fragment(), ISupportFragment {
    override val supportDelegate by lazy { SupportFragmentDelegate(this) }
    private var _mActivity: SupportActivity? = null
    private var isLoaded = false
    open val autoSetBackgroundColor = true

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (nextAnim > 0) {
            val anim = AnimationUtils.loadAnimation(_mActivity, nextAnim)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    if (enter) {
                        onEnterAnimationEnd()
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            return anim
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag，添加SharedElement动画，操作非回退栈Fragment
     */
    override fun extraTransaction(): ExtraTransaction? {
        return supportDelegate.extraTransaction()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        supportDelegate.onAttach(context)
        _mActivity = supportDelegate.activity as SupportActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportDelegate.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        supportDelegate.onViewCreated(savedInstanceState, autoSetBackgroundColor)
    }

    override fun onResume() {
        super.onResume()
        if (!supportDelegate.hasEnterAnimation) {
            onRealResume()
        }
    }

    override fun onPause() {
        super.onPause()
        supportDelegate.isVisible = false
        onInvisible()
    }

    private fun onEnterAnimationEnd() {
        onRealResume()
        supportDelegate.hasEnterAnimation = false
    }

    private fun onRealResume() {
        if (!isLoaded && !isHidden) {
            lazyInit()
            isLoaded = true
        }
        if (!isHidden) {
            supportDelegate.isVisible = true
            onVisible()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        supportDelegate.onDestroy()
        isLoaded = false
        super.onDestroy()
    }

    /**
     * Causes the Runnable r to be added to the action queue.
     *
     *
     * The runnable will be run after all the previous action has been run.
     *
     *
     * 前面的事务全部执行后 执行该Action
     */
    override fun post(runnable: Runnable) {
        supportDelegate.post(runnable)
    }

    /**
     * 按返回键触发,前提是SupportActivity的onBackPressed()方法能被调用
     *
     * @return false则继续向上传递, true则消费掉该事件
     */
    override fun onBackPressedSupport(): Boolean {
        return supportDelegate.onBackPressedSupport()
    }

    /**
     * 类似 [Activity.setResult]
     *
     *
     * Similar to [Activity.setResult]
     *
     * @see .startForResult
     */
    override fun setFragmentResult(resultCode: Int, bundle: Bundle?) {
        supportDelegate.setFragmentResult(resultCode, bundle)
    }

    /**
     * 类似Activity.onActivityResult(int, int, Intent)
     *
     * @see .startForResult
     */
    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {}

    /**
     * 在start(TargetFragment,LaunchMode)时,启动模式为SingleTask/SingleTop, 回调TargetFragment的该方法
     * 类似 Activity.onNewIntent(Intent)
     *
     * @param args putNewBundle(Bundle newBundle)
     * @see .start
     */
    override fun onNewBundle(args: Bundle?) {}

    /**
     * 添加NewBundle,用于启动模式为SingleTask/SingleTop时
     *
     * @see .start
     */
    override fun putNewBundle(newBundle: Bundle?) {
        supportDelegate.putNewBundle(newBundle)
    }

    /**
     * 隐藏软键盘
     */
    protected fun hideSoftInput() {
        supportDelegate.hideSoftInput()
    }

    /**
     * 显示软键盘
     */
    protected fun showSoftInput(view: View?) {
        supportDelegate.showSoftInput(view)
    }

    /**
     * 加载根Fragment, 即Fragment内的第一个子Fragment
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
     */
    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?) {
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
     * 使用该方法时，要确保同级栈内无多余的Fragment(只有通过loadMultipleRootFragment()载入的Fragment)
     *
     *
     * 建议使用更明确的[.showHideFragment]
     *
     * @param showFragment 需要show的Fragment
     */
    fun showHideFragment(showFragment: ISupportFragment?) {
        supportDelegate.showHideFragment(showFragment)
    }

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     */
    fun showHideFragment(showFragment: ISupportFragment?, hideFragment: ISupportFragment?) {
        supportDelegate.showHideFragment(showFragment, hideFragment)
    }

    fun start(toFragment: ISupportFragment?) {
        supportDelegate.start(toFragment)
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    fun start(toFragment: ISupportFragment?, @LaunchMode launchMode: Int) {
        supportDelegate.start(toFragment, launchMode)
    }

    /**
     * Launch an fragment for which you would like a result when it popped.
     */
    fun startForResult(toFragment: ISupportFragment?, requestCode: Int) {
        supportDelegate.startForResult(toFragment, requestCode)
    }

    /**
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: ISupportFragment?) {
        supportDelegate.startWithPop(toFragment)
    }

    /**
     * @see .popTo
     * @see .start
     */
    fun startWithPopTo(
        toFragment: ISupportFragment,
        targetFragmentClass: Class<*>,
        includeTargetFragment: Boolean
    ) {
        supportDelegate.startWithPopTo(toFragment, targetFragmentClass, includeTargetFragment)
    }

    fun replaceFragment(toFragment: ISupportFragment?) {
        supportDelegate.replaceFragment(toFragment)
    }

    fun pop() {
        supportDelegate.pop()
    }

    fun popQuiet() {
        supportDelegate.popQuiet()
    }

    /**
     * Pop the last fragment transition from the manager's fragment
     * back stack.
     *
     *
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

    fun startChild(toFragment: ISupportFragment?) {
        supportDelegate.startChild(toFragment)
    }

    fun startChild(toFragment: ISupportFragment?, @LaunchMode launchMode: Int) {
        supportDelegate.startChild(toFragment, launchMode)
    }

    fun startChildForResult(toFragment: ISupportFragment?, requestCode: Int) {
        supportDelegate.startChildForResult(toFragment, requestCode)
    }

    fun startChildWithPop(toFragment: ISupportFragment?) {
        supportDelegate.startChildWithPop(toFragment)
    }

    fun replaceChildFragment(toFragment: ISupportFragment?) {
        supportDelegate.replaceChildFragment(toFragment)
    }

    /**
     * Pop the child fragment.
     */
    fun popChild() {
        supportDelegate.popChild()
    }

    fun popToChild(targetFragmentClass: Class<*>, includeTargetFragment: Boolean) {
        supportDelegate.popToChild(targetFragmentClass, includeTargetFragment)
    }

    fun popToChild(
        targetFragmentClass: Class<*>,
        includeTargetFragment: Boolean,
        afterPopTransactionRunnable: Runnable?
    ) {
        supportDelegate.popToChild(
            targetFragmentClass,
            includeTargetFragment,
            afterPopTransactionRunnable
        )
    }

    /**
     * 得到位于栈顶Fragment
     */
    val topFragment: ISupportFragment?
        get() = SupportHelper.getTopFragment(parentFragmentManager)

    /**
     * 得到位于子栈顶Fragment
     */
    val topChildFragment: ISupportFragment?
        get() = SupportHelper.getTopFragment(childFragmentManager)

    /**
     * @return 位于当前Fragment的前一个Fragment
     */
    val preFragment: ISupportFragment?
        get() = SupportHelper.getPreFragment(this)

    /**
     * 获取栈内的fragment对象
     */
    fun <T : ISupportFragment?> findFragment(fragmentClass: Class<T>?): T? {
        return SupportHelper.findFragment(parentFragmentManager, fragmentClass)
    }

    /**
     * 获取子栈内的fragment对象
     */
    fun <T : ISupportFragment?> findChildFragment(fragmentClass: Class<T>?): T? {
        return SupportHelper.findFragment(childFragmentManager, fragmentClass)
    }
}