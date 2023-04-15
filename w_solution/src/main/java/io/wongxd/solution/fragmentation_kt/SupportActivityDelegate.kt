
package io.wongxd.solution.fragmentation_kt

import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.wongxd.solution.fragmentation_kt.ExtraTransaction.ExtraTransactionImpl
import io.wongxd.solution.fragmentation_kt.ISupportFragment.LaunchMode
import io.wongxd.solution.fragmentation_kt.animation.FragmentAnimator
import io.wongxd.solution.fragmentation_kt.queue.Action

class SupportActivityDelegate(support: ISupportActivity) {
    private val mSupport: ISupportActivity
    private val mActivity: FragmentActivity
    private var mTransactionDelegate: TransactionDelegate? = null

    /**
     * 当Fragment根布局 没有设定background属性时,
     * Fragmentation默认使用Theme的android:windowbackground作为Fragment的背景,
     * 可以通过该方法改变Fragment背景。
     */
    var defaultFragmentBackground = 0
    var fragmentAnimator = FragmentAnimator()

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag，操作非回退栈Fragment
     */
    fun extraTransaction(): ExtraTransaction {
        return ExtraTransactionImpl(
            (mSupport as FragmentActivity),
            topFragment, transactionDelegate, true
        )
    }

    fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val animator: FragmentAnimator? =
                savedInstanceState.getParcelable(S_FRAGMENTATION_FRAGMENT_ANIMATOR)
            if (animator != null) {
                fragmentAnimator = animator
            }
        }
        mTransactionDelegate = transactionDelegate
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(S_FRAGMENTATION_FRAGMENT_ANIMATOR, fragmentAnimator)
    }

    fun onPostCreate(savedInstanceState: Bundle?) {
    }

    fun onDestroy() {
    }

    val transactionDelegate: TransactionDelegate
        get() {
            if (mTransactionDelegate == null) {
                mTransactionDelegate = TransactionDelegate(mSupport)
            }
            return mTransactionDelegate!!
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
     * 不建议复写该方法,请使用 [.onBackPressedSupport] 代替
     */
    fun onBackPressed() {
        mTransactionDelegate?.mActionQueue?.enqueue(object : Action(ACTION_BACK) {
            override fun run() {
                // 获取 activeFragment:
                // 即从栈顶开始 状态为show
                // 并且 fragment.supportDelegate.isStartByFragmentation 为true
                // 的那个Fragment
                val activeFragment = SupportHelper.getActiveFragment(
                    supportFragmentManager
                )
                if (mTransactionDelegate?.dispatchBackPressedEvent(activeFragment) == true)
                    return
                mSupport.onBackPressedSupport()
            }
        })
    }

    /**
     * 该方法回调时机为,Activity回退栈内Fragment的数量 小于等于1 时,默认finish Activity
     * 请尽量复写该方法,避免复写onBackPress(),以保证SupportFragment内的onBackPressedSupport()回退事件正常执行
     */
    fun onBackPressedSupport() {
        val list = SupportHelper.getActiveFragments(
            supportFragmentManager
        )
        var fragmentNum = 0
        for (f in list) {
            if (f is ISupportFragment
                && (f as ISupportFragment).supportDelegate.isCanPop
                && (f as ISupportFragment).supportDelegate.isStartByFragmentation
            ) {
                fragmentNum++
            }
        }
        if (fragmentNum > 0) {
            pop()
        } else {
            ActivityCompat.finishAfterTransition(mActivity)
        }
    }

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     */
    fun loadRootFragment(containerId: Int, toFragment: ISupportFragment?) {
        mTransactionDelegate?.loadRootTransaction(
            supportFragmentManager,
            containerId,
            toFragment
        )
    }

    /**
     * 加载多个同级根Fragment,类似Wechat, QQ主页的场景
     */
    fun loadMultipleRootFragment(
        containerId: Int,
        showPosition: Int,
        vararg toFragments: ISupportFragment
    ) {
        mTransactionDelegate?.loadMultipleRootTransaction(
            supportFragmentManager,
            containerId,
            showPosition,
            *toFragments
        )
    }
    /**
     * show一个Fragment,hide一个Fragment
     *
     * @param showFragment 需要show的Fragment
     * @param hideFragment 需要hide的Fragment
     */
    /**
     * 这个的使用必须要注意
     * show一个Fragment,hide其他同栈所有Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment,(只有通过loadMultipleRootFragment()载入的Fragment)
     *
     *
     *
     * 建议使用更明确的[.showHideFragment]
     *
     * @param showFragment 需要show的Fragment
     */
    @JvmOverloads
    fun showHideFragment(showFragment: ISupportFragment, hideFragment: ISupportFragment? = null) {
        mTransactionDelegate?.showHideFragment(supportFragmentManager, showFragment, hideFragment)
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    @JvmOverloads
    fun start(
        toFragment: ISupportFragment,
        @LaunchMode launchMode: Int = ISupportFragment.STANDARD
    ) {
        mTransactionDelegate?.dispatchStartTransaction(
            supportFragmentManager,
            topFragment, toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD
        )
    }

    /**
     * Launch an fragment for which you would like a result when it popped.
     */
    fun startForResult(toFragment: ISupportFragment, requestCode: Int) {
        mTransactionDelegate?.dispatchStartTransaction(
            supportFragmentManager,
            topFragment, toFragment, requestCode, ISupportFragment.STANDARD,
            TransactionDelegate.TYPE_ADD_RESULT
        )
    }

    /**
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: ISupportFragment) {
        mTransactionDelegate?.dispatchStartWithPopTransaction(
            supportFragmentManager,
            topFragment,
            toFragment
        )
    }

    fun startWithPopTo(
        toFragment: ISupportFragment,
        targetFragmentClass: Class<*>?,
        includeTargetFragment: Boolean
    ) {
        mTransactionDelegate?.dispatchStartWithPopToTransaction(
            supportFragmentManager,
            topFragment, toFragment, targetFragmentClass?.name, includeTargetFragment
        )
    }

    fun replaceFragment(toFragment: ISupportFragment) {
        mTransactionDelegate?.dispatchStartTransaction(
            supportFragmentManager,
            topFragment, toFragment, 0, ISupportFragment.STANDARD,
            TransactionDelegate.TYPE_REPLACE
        )
    }

    /**
     * Pop the child fragment.
     */
    fun pop() {
        mTransactionDelegate?.pop(supportFragmentManager)
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
        targetFragmentClass: Class<*>,
        includeTargetFragment: Boolean,
        afterPopTransactionRunnable: Runnable? = null
    ) {
        mTransactionDelegate?.popTo(
            targetFragmentClass.name, includeTargetFragment,
            afterPopTransactionRunnable, supportFragmentManager
        )
    }

    private val supportFragmentManager: FragmentManager
        get() = mActivity.supportFragmentManager

    private val topFragment: ISupportFragment?
        get() = SupportHelper.getTopFragment(supportFragmentManager)

    companion object {
        const val S_FRAGMENTATION_FRAGMENT_ANIMATOR = "s_fragmentation_fragment_animator"
    }

    init {
        if (support !is FragmentActivity) throw RuntimeException("Must extends FragmentActivity/AppCompatActivity")
        mSupport = support
        mActivity = support
    }
}