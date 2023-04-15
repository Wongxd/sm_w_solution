
package io.wongxd.solution.fragmentation_kt

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.wongxd.solution.fragmentation_kt.ExtraTransaction.ExtraTransactionImpl
import io.wongxd.solution.fragmentation_kt.ISupportFragment.LaunchMode
import io.wongxd.solution.fragmentation_kt.SupportHelper.getTopFragment
import io.wongxd.solution.fragmentation_kt.SupportHelper.hideSoftInput
import io.wongxd.solution.fragmentation_kt.record.ResultRecord
import io.wongxd.solution.fragmentation_kt.record.TransactionRecord

class SupportFragmentDelegate(support: ISupportFragment?) {
    var mContainerId = 0
    private var mTransactionDelegate: TransactionDelegate? = null
    var mTransactionRecord: TransactionRecord? = null
    var mNewBundle: Bundle? = null
    private val mSupportF: ISupportFragment
    private val mFragment: Fragment
    var activity: FragmentActivity? = null
        protected set
    private var mSupport: ISupportActivity? = null
    var isVisible = false
    var hasEnterAnimation = false
    var isCanPop = true
    var isStartByFragmentation = false

    @ColorRes
    var defaultBackgroundColor = 0

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag，添加SharedElement动画，操作非回退栈Fragment
     */
    fun extraTransaction(): ExtraTransaction {
        if (mTransactionDelegate == null) throw RuntimeException(mFragment.javaClass.simpleName + " not attach!")
        return ExtraTransactionImpl(
            (mSupport as FragmentActivity?)!!,
            mSupportF, mTransactionDelegate!!, false
        )
    }

    fun onAttach(context: Context) {
        if (context is ISupportActivity) {
            mSupport = context
            activity = context as FragmentActivity
            mTransactionDelegate = mSupport?.supportDelegate?.transactionDelegate
        } else {
            throw RuntimeException(context.javaClass.simpleName + " must impl ISupportActivity!")
        }
    }

    fun onCreate(savedInstanceState: Bundle?) {
        val bundle = mFragment.arguments
        if (bundle != null) {
            mContainerId = bundle.getInt(TransactionDelegate.FRAGMENTATION_ARG_CONTAINER)
        }
    }

    fun onViewCreated(savedInstanceState: Bundle?, autoSetBackgroundColor: Boolean) {
        if (autoSetBackgroundColor) {
            val view = mFragment.view
            view?.let { setBackground(it) }
        }
    }

    fun setBackground(view: View) {
        if (view.background != null) {
            return
        }
        var realBackground = defaultBackgroundColor
        if (realBackground == 0) {
            realBackground = mSupport?.supportDelegate?.defaultFragmentBackground ?: 0
        }
        if (realBackground == 0) {
            realBackground = windowBackground
        }
        view.setBackgroundResource(realBackground)
    }

    private val windowBackground: Int
        get() {
            val a = activity?.theme?.obtainStyledAttributes(
                intArrayOf(
                    android.R.attr.windowBackground
                )
            )
            val background = a?.getResourceId(0, 0)
            a?.recycle()
            return background ?: 0
        }

    fun onDestroy() {
        mTransactionDelegate?.handleResultRecord(mFragment)
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
    fun post(runnable: Runnable) {
        mTransactionDelegate?.post(runnable)
    }

    /**
     * 类似 [Activity.setResult]
     *
     *
     * Similar to [Activity.setResult]
     *
     * @see .startForResult
     */
    fun setFragmentResult(resultCode: Int, bundle: Bundle?) {
        val args = mFragment.arguments
        if (args == null || !args.containsKey(TransactionDelegate.FRAGMENTATION_ARG_RESULT_RECORD)) {
            return
        }
        val resultRecord: ResultRecord? =
            args.getParcelable(TransactionDelegate.FRAGMENTATION_ARG_RESULT_RECORD)
        if (resultRecord != null) {
            resultRecord.resultCode = resultCode
            resultRecord.resultBundle = bundle
        }
    }

    /**
     * 添加NewBundle,用于启动模式为SingleTask/SingleTop时
     *
     * @see .start
     */
    fun putNewBundle(newBundle: Bundle?) {
        mNewBundle = newBundle
    }

    /**
     * Back Event
     *
     * @return false则继续向上传递, true则消费掉该事件
     */
    fun onBackPressedSupport(): Boolean {
        return false
    }

    /**
     * 隐藏软键盘
     */
    fun hideSoftInput() {
        val activity = mFragment.activity ?: return
        val view = activity.window.decorView
        hideSoftInput(view)
    }

    /**
     * 显示软键盘
     */
    fun showSoftInput(view: View?) {
        SupportHelper.showSoftInput(view)
    }

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     */
    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?) {
        mTransactionDelegate?.loadRootTransaction(
            childFragmentManager,
            containerId, toFragment
        )
    }

    /**
     * 加载多个同级根Fragment
     */
    fun loadMultipleRootFragment(
        containerId: Int, showPosition: Int,
        vararg toFragments: ISupportFragment
    ) {
        mTransactionDelegate?.loadMultipleRootTransaction(
            childFragmentManager,
            containerId, showPosition, *toFragments
        )
    }
    /**
     * show一个Fragment,hide一个Fragment
     */
    /**
     * show一个Fragment,hide其他同栈所有Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment(只有通过loadMultipleRootFragment()载入的Fragment)
     *
     *
     * 建议使用更明确的[.showHideFragment]
     */
    @JvmOverloads
    fun showHideFragment(showFragment: ISupportFragment?, hideFragment: ISupportFragment? = null) {
        showFragment ?: return
        mTransactionDelegate?.showHideFragment(childFragmentManager, showFragment, hideFragment)
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    @JvmOverloads
    fun start(
        toFragment: ISupportFragment?,
        @LaunchMode launchMode: Int = ISupportFragment.STANDARD
    ) {
        mTransactionDelegate?.dispatchStartTransaction(
            mFragment.parentFragmentManager, mSupportF,
            toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD
        )
    }

    /**
     * Launch an fragment for which you would like a result when it poped.
     */
    fun startForResult(toFragment: ISupportFragment?, requestCode: Int) {
        mTransactionDelegate?.dispatchStartTransaction(
            mFragment.parentFragmentManager,
            mSupportF,
            toFragment,
            requestCode,
            ISupportFragment.STANDARD,
            TransactionDelegate.TYPE_ADD_RESULT
        )
    }

    /**
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: ISupportFragment?) {
        toFragment ?: return
        mTransactionDelegate?.dispatchStartWithPopTransaction(
            mFragment.parentFragmentManager,
            mSupportF,
            toFragment
        )
    }

    fun startWithPopTo(
        toFragment: ISupportFragment?, targetFragmentClass: Class<*>,
        includeTargetFragment: Boolean
    ) {
        mTransactionDelegate?.dispatchStartWithPopToTransaction(
            mFragment.parentFragmentManager, mSupportF,
            toFragment, targetFragmentClass.name, includeTargetFragment
        )
    }

    fun replaceFragment(toFragment: ISupportFragment?) {
        mTransactionDelegate?.dispatchStartTransaction(
            mFragment.parentFragmentManager, mSupportF,
            toFragment, 0, ISupportFragment.STANDARD,
            TransactionDelegate.TYPE_REPLACE
        )
    }

    @JvmOverloads
    fun startChild(
        toFragment: ISupportFragment?,
        @LaunchMode launchMode: Int = ISupportFragment.STANDARD
    ) {
        mTransactionDelegate?.dispatchStartTransaction(
            childFragmentManager, childTopFragment,
            toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD
        )
    }

    fun startChildForResult(toFragment: ISupportFragment?, requestCode: Int) {
        mTransactionDelegate?.dispatchStartTransaction(
            childFragmentManager,
            childTopFragment,
            toFragment,
            requestCode,
            ISupportFragment.STANDARD,
            TransactionDelegate.TYPE_ADD_RESULT
        )
    }

    fun startChildWithPop(toFragment: ISupportFragment?) {
        mTransactionDelegate?.dispatchStartWithPopTransaction(
            childFragmentManager,
            childTopFragment,
            toFragment
        )
    }

    fun replaceChildFragment(toFragment: ISupportFragment?) {
        mTransactionDelegate?.dispatchStartTransaction(
            childFragmentManager, childTopFragment,
            toFragment, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_REPLACE
        )
    }

    fun pop() {
        mTransactionDelegate?.pop(mFragment.parentFragmentManager)
    }

    fun popQuiet() {
        mTransactionDelegate?.popQuiet(mFragment.parentFragmentManager)
    }

    /**
     * Pop the child fragment.
     */
    fun popChild() {
        mTransactionDelegate?.pop(childFragmentManager)
    }
    /**
     * If you want to begin another FragmentTransaction immediately after popTo(), use this method.
     * 如果你想在出栈后, 立刻进行FragmentTransaction操作，请使用该方法
     */
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
    @JvmOverloads
    fun popTo(
        targetFragmentClass: Class<*>, includeTargetFragment: Boolean,
        afterPopTransactionRunnable: Runnable? = null
    ) {
        mTransactionDelegate?.popTo(
            targetFragmentClass.name, includeTargetFragment,
            afterPopTransactionRunnable, mFragment.parentFragmentManager
        )
    }

    @JvmOverloads
    fun popToChild(
        targetFragmentClass: Class<*>, includeTargetFragment: Boolean,
        afterPopTransactionRunnable: Runnable? = null
    ) {
        mTransactionDelegate?.popTo(
            targetFragmentClass.name, includeTargetFragment,
            afterPopTransactionRunnable, childFragmentManager
        )
    }

    private val childFragmentManager: FragmentManager
        get() = mFragment.childFragmentManager
    private val childTopFragment: ISupportFragment?
        get() = getTopFragment(childFragmentManager)

    init {
        if (support !is Fragment) throw RuntimeException("Must extends Fragment")
        mSupportF = support
        mFragment = support
    }
}