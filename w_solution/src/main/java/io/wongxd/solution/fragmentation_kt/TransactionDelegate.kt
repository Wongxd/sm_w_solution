
package io.wongxd.solution.fragmentation_kt

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import io.wongxd.solution.fragmentation_kt.Fragmentation.Companion.default
import io.wongxd.solution.fragmentation_kt.queue.Action
import io.wongxd.solution.fragmentation_kt.queue.ActionQueue
import io.wongxd.solution.fragmentation_kt.record.ResultRecord

/**
 * Controller
 */
class TransactionDelegate(private val mSupport: ISupportActivity) {
    private val mHandler: Handler
    var mActionQueue: ActionQueue
    fun post(runnable: Runnable) {
        mActionQueue.enqueue(object : Action() {
            override fun run() {
                runnable.run()
            }
        })
    }

    fun loadRootTransaction(
        fm: FragmentManager, containerId: Int,
        to: ISupportFragment?
    ) {
        to ?: return
        enqueue(fm, object : Action(ACTION_LOAD) {
            override fun run() {
                bindContainerId(containerId, to)
                var toFragmentTag = to.javaClass.name
                val transactionRecord = to.supportDelegate.mTransactionRecord
                if (transactionRecord != null) {
                    if (transactionRecord.tag != null) {
                        toFragmentTag = transactionRecord.tag!!
                    }
                }
                to.supportDelegate.isCanPop = false
                start(fm, null, to, toFragmentTag, TYPE_REPLACE)
            }
        })
    }

    fun loadMultipleRootTransaction(
        fm: FragmentManager, containerId: Int, showPosition: Int,
        vararg tos: ISupportFragment
    ) {
        enqueue(fm, object : Action(ACTION_LOAD) {
            override fun run() {
                val ft = fm.beginTransaction()
                for (i in 0 until tos.size) {
                    val to = tos[i] as Fragment
                    (to as ISupportFragment).supportDelegate.isCanPop = false
                    (to as ISupportFragment).supportDelegate.isStartByFragmentation = true
                    bindContainerId(containerId, tos[i])
                    val toName = to.javaClass.name
                    ft.add(containerId, to, toName)
                    if (i != showPosition) {
                        ft.hide(to)
                        ft.setMaxLifecycle(to, Lifecycle.State.STARTED)
                    } else {
                        ft.setMaxLifecycle(to, Lifecycle.State.RESUMED)
                    }
                }
                supportCommit(fm, ft)
            }
        })
    }

    fun remove(fm: FragmentManager, toFragment: ISupportFragment?) {
        enqueue(fm, object : Action(ACTION_POP) {
            override fun run() {
                val ft = fm.beginTransaction()
                ft.remove((toFragment as Fragment?)!!)
                supportCommit(fm, ft)
            }
        })
    }

    fun remove(fm: FragmentManager, toFragment: ISupportFragment?, targetFragmentExit: Int) {
        enqueue(fm, object : Action(ACTION_POP) {
            override fun run() {
                val ft = fm.beginTransaction()
                ft.setCustomAnimations(0, targetFragmentExit, 0, 0)
                ft.remove((toFragment as Fragment?)!!)
                supportCommit(fm, ft)
            }
        })
    }

    /**
     * Dispatch the start transaction.
     */
    fun dispatchStartTransaction(
        fm: FragmentManager, from: ISupportFragment?,
        to: ISupportFragment?, requestCode: Int, launchMode: Int, type: Int
    ) {
        enqueue(
            fm,
            object :
                Action(if (launchMode == ISupportFragment.SINGLETASK) ACTION_POP else ACTION_NORMAL) {
                override fun run() {
                    to ?: return
                    doDispatchStartTransaction(fm, from, to, requestCode, launchMode, type)
                }
            })
    }

    /**
     * Show showFragment then hide hideFragment
     */
    fun showHideFragment(
        fm: FragmentManager, showFragment: ISupportFragment,
        hideFragment: ISupportFragment?
    ) {
        enqueue(fm, object : Action() {
            override fun run() {
                doShowHideFragment(fm, showFragment, hideFragment)
            }
        })
    }

    /**
     * Start the target Fragment and pop itself
     */
    fun dispatchStartWithPopTransaction(
        fm: FragmentManager,
        from: ISupportFragment?,
        to: ISupportFragment?
    ) {
        to ?: return
        //此处需要设置 ACTION_POP， 动画效果更自然
        enqueue(fm, object : Action(ACTION_POP) {
            override fun run() {
                if (fm.isStateSaved) return
                val top = getTopFragmentForStart(from, fm)
                    ?: throw NullPointerException(
                        "There is no Fragment in the FragmentManager, " +
                                "maybe you need to call loadRootFragment() first!"
                    )
                val containerId = top.supportDelegate.mContainerId
                bindContainerId(containerId, to)
                val toFragmentTag = to.javaClass.name
                val fromFragment = getTopFragmentForStart(from, fm)
                start(fm, fromFragment, to, toFragmentTag, TYPE_ADD)
            }
        })
        enqueue(fm, object : Action() {
            override fun run() {
                val ft = fm.beginTransaction()
                    .remove((from as Fragment?)!!)
                supportCommit(fm, ft)
            }
        })
    }

    fun dispatchStartWithPopToTransaction(
        fm: FragmentManager, from: ISupportFragment?,
        to: ISupportFragment?, fragmentTag: String?, includeTargetFragment: Boolean
    ) {
        to ?: return
        val willPopFragments =
            SupportHelper.getWillPopFragments(fm, fragmentTag, includeTargetFragment)
        //此处需要设置 ACTION_POP， 动画效果更自然
        enqueue(fm, object : Action(ACTION_POP) {
            override fun run() {
                if (fm.isStateSaved) return
                val top = getTopFragmentForStart(from, fm)
                    ?: throw NullPointerException(
                        "There is no Fragment in the FragmentManager, " +
                                "maybe you need to call loadRootFragment() first!"
                    )
                val containerId = top.supportDelegate.mContainerId
                bindContainerId(containerId, to)
                if (willPopFragments.size <= 0) return
                val toFragmentTag = to.javaClass.name
                val fromFragment = getTopFragmentForStart(from, fm)
                start(fm, fromFragment, to, toFragmentTag, TYPE_ADD)
            }
        })
        if (willPopFragments.size > 0) {
            enqueue(fm, object : Action() {
                override fun run() {
                    safePopTo(fm, willPopFragments)
                }
            })
        }
    }

    /**
     * Pop
     */
    fun pop(fm: FragmentManager) {
        if (fm.isStateSaved) return
        val top = SupportHelper.getTopFragment(fm) ?: return
        enqueue(fm, object : Action(ACTION_POP) {
            override fun run() {
                val ft = fm.beginTransaction()
                val preFragment = SupportHelper.getPreFragment(top as Fragment)
                val record = top.supportDelegate.mTransactionRecord
                if (preFragment == null) return
                if (record != null) {
                    if (record.currentFragmentPopEnter != Int.MIN_VALUE) {
                        preFragment.supportDelegate.hasEnterAnimation = true
                        ft.setCustomAnimations(
                            record.currentFragmentPopEnter,
                            record.targetFragmentExit,
                            0,
                            0
                        )
                    }
                } else {
                    if (mSupport.fragmentAnimator.currentFragmentPopEnter > 0) {
                        preFragment.supportDelegate.hasEnterAnimation = true
                        ft.setCustomAnimations(
                            mSupport.fragmentAnimator.currentFragmentPopEnter,
                            mSupport.fragmentAnimator.targetFragmentExit,
                            0,
                            0
                        )
                    } else if (default!!.currentFragmentPopEnter > 0) {
                        preFragment.supportDelegate.hasEnterAnimation = true
                        ft.setCustomAnimations(
                            default!!.currentFragmentPopEnter, default!!.targetFragmentExit,
                            0, 0
                        )
                    }
                }
                ft.remove((top as Fragment))
                if (preFragment is Fragment) {
                    ft.show((preFragment as Fragment))
                    ft.setMaxLifecycle((preFragment as Fragment), Lifecycle.State.RESUMED)
                }
                supportCommit(fm, ft)
            }
        })
    }

    /**
     * popQuiet
     */
    fun popQuiet(fm: FragmentManager) {
        val top = SupportHelper.getTopFragment(fm) ?: return
        enqueue(fm, object : Action(ACTION_POP) {
            override fun run() {
                val ft = fm.beginTransaction()
                val preFragment = SupportHelper.getPreFragment(top as Fragment) ?: return
                ft.remove((top as Fragment))
                if (preFragment is Fragment) {
                    ft.show((preFragment as Fragment))
                    ft.setMaxLifecycle((preFragment as Fragment), Lifecycle.State.RESUMED)
                }
                supportCommit(fm, ft)
            }
        })
    }

    /**
     * Pop the last fragment transition from the manager's fragment pop stack.
     *
     * @param targetFragmentTag     Tag
     * @param includeTargetFragment Whether it includes targetFragment
     */
    fun popTo(
        targetFragmentTag: String?, includeTargetFragment: Boolean,
        afterPopTransactionRunnable: Runnable?, fm: FragmentManager
    ) {
        enqueue(fm, object : Action(ACTION_POP) {
            override fun run() {
                if (fm.isStateSaved) return
                doPopTo(targetFragmentTag, includeTargetFragment, fm)
                afterPopTransactionRunnable?.run()
            }
        })
    }

    /**
     * Dispatch the pop-event. Priority of the top of the stack of Fragment
     */
    fun dispatchBackPressedEvent(activeFragment: ISupportFragment?): Boolean {
        if (activeFragment != null) {
            val result = activeFragment.onBackPressedSupport()
            if (result) {
                return true
            }
            if (activeFragment.supportDelegate.isStartByFragmentation
                && activeFragment.supportDelegate.isCanPop
            ) {
                activeFragment.supportDelegate.pop()
                return true
            }
            val parentFragment = (activeFragment as Fragment).parentFragment
            return dispatchBackPressedEvent(parentFragment as ISupportFragment?)
        }
        return false
    }

    fun handleResultRecord(from: Fragment) {
        try {
            val args = from.arguments ?: return
            val resultRecord: ResultRecord = args.getParcelable(FRAGMENTATION_ARG_RESULT_RECORD)
                ?: return
            val targetFragment = from
                .parentFragmentManager
                .getFragment(
                    from.requireArguments(),
                    FRAGMENTATION_STATE_SAVE_RESULT
                ) as ISupportFragment?
                ?: return
            targetFragment.onFragmentResult(
                resultRecord.requestCode,
                resultRecord.resultCode,
                resultRecord.resultBundle
            )
        } catch (ignored: IllegalStateException) {
            // Fragment no longer exists
        }
    }

    private fun enqueue(fm: FragmentManager?, action: Action) {
        if (fm == null) {
            Log.w(TAG, "FragmentManager is null, skip the action!")
            return
        }
        mActionQueue.enqueue(action)
    }

    private fun doDispatchStartTransaction(
        fm: FragmentManager, oriFrom: ISupportFragment?,
        to: ISupportFragment, requestCode: Int, launchMode: Int, type: Int
    ) {
        var from: ISupportFragment? = oriFrom
        checkNotNull(to)
        if ((type == TYPE_ADD_RESULT || type == TYPE_ADD_RESULT_WITHOUT_HIDE) && from != null) {
            if (!(from as Fragment).isAdded) {
                Log.w(
                    TAG, (from as Fragment).javaClass.simpleName + " has not been attached yet! " +
                            "startForResult() converted to start()"
                )
            } else {
                saveRequestCode(fm, from as Fragment, to as Fragment, requestCode)
            }
        }
        from = getTopFragmentForStart(from, fm)
        val containerId = getArguments(to as Fragment).getInt(FRAGMENTATION_ARG_CONTAINER, 0)
        if (from == null && containerId == 0) {
            Log.e(
                TAG,
                "There is no Fragment in the FragmentManager, maybe you need to call loadRootFragment()!"
            )
            return
        }
        if (from != null && containerId == 0) {
            bindContainerId(from.supportDelegate.mContainerId, to)
        }

        // process ExtraTransaction
        var toFragmentTag = to.javaClass.name
        val transactionRecord = to.supportDelegate.mTransactionRecord
        if (transactionRecord != null) {
            if (transactionRecord.tag != null) {
                toFragmentTag = transactionRecord.tag!!
            }
        }
        if (handleLaunchMode(fm, from, to, toFragmentTag, launchMode)) return
        start(fm, from, to, toFragmentTag, type)
    }

    private fun getTopFragmentForStart(
        from: ISupportFragment?,
        fm: FragmentManager
    ): ISupportFragment? {
        return if (from == null) {
            SupportHelper.getTopFragment(fm)
        } else {
            SupportHelper.getTopFragment(fm, from.supportDelegate.mContainerId)
        }
    }

    private fun start(
        fm: FragmentManager, from: ISupportFragment?, to: ISupportFragment, toFragmentTag: String?,
        type: Int
    ) {
        to.supportDelegate.isStartByFragmentation = true
        val ft = fm.beginTransaction()
        val addMode =
            type == TYPE_ADD || type == TYPE_ADD_RESULT || type == TYPE_ADD_WITHOUT_HIDE || type == TYPE_ADD_RESULT_WITHOUT_HIDE
        val fromF = from as Fragment?
        val toF = to as Fragment
        val args = getArguments(toF)
        if (from == null) {
            to.supportDelegate.isCanPop = false
            ft.replace(args.getInt(FRAGMENTATION_ARG_CONTAINER), toF, toFragmentTag)
            ft.setMaxLifecycle(toF, Lifecycle.State.RESUMED)
        } else {
            if (addMode) {
                val record = to.supportDelegate.mTransactionRecord
                if (record != null) {
                    if (record.targetFragmentEnter != Int.MIN_VALUE) {
                        to.supportDelegate.hasEnterAnimation = true
                        ft.setCustomAnimations(
                            record.targetFragmentEnter, record.currentFragmentPopExit,
                            0, 0
                        )
                    }
                } else {
                    if (mSupport.fragmentAnimator!!.targetFragmentEnter > 0) {
                        to.supportDelegate.hasEnterAnimation = true
                        ft.setCustomAnimations(
                            mSupport.fragmentAnimator!!.targetFragmentEnter,
                            mSupport.fragmentAnimator!!.currentFragmentPopExit,
                            0,
                            0
                        )
                    } else if (default!!.targetFragmentEnter > 0) {
                        to.supportDelegate.hasEnterAnimation = true
                        ft.setCustomAnimations(
                            default!!.targetFragmentEnter, default!!.currentFragmentPopExit,
                            0, 0
                        )
                    }
                }
                ft.add(from.supportDelegate.mContainerId, toF, toFragmentTag)
                ft.setMaxLifecycle(toF, Lifecycle.State.RESUMED)
                if (type != TYPE_ADD_WITHOUT_HIDE && type != TYPE_ADD_RESULT_WITHOUT_HIDE) {
                    ft.hide(fromF!!)
                    ft.setMaxLifecycle(fromF, Lifecycle.State.STARTED)
                }
            } else {
                to.supportDelegate.isCanPop = false
                ft.replace(from.supportDelegate.mContainerId, toF, toFragmentTag)
                ft.setMaxLifecycle(toF, Lifecycle.State.RESUMED)
            }
        }
        supportCommit(fm, ft)
    }

    private fun doShowHideFragment(
        fm: FragmentManager,
        showFragment: ISupportFragment,
        hideFragment: ISupportFragment?
    ) {
        if (showFragment === hideFragment) return
        val ft = fm.beginTransaction().show((showFragment as Fragment))
        ft.setMaxLifecycle((showFragment as Fragment), Lifecycle.State.RESUMED)
        if (hideFragment == null) {
            val fragmentList = SupportHelper.getActiveFragments(fm)
            for (fragment in fragmentList) {
                if (fragment != null && fragment !== showFragment) {
                    ft.hide(fragment)
                    ft.setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                }
            }
        } else {
            ft.hide((hideFragment as Fragment?)!!)
            ft.setMaxLifecycle((hideFragment as Fragment?)!!, Lifecycle.State.STARTED)
        }
        supportCommit(fm, ft)
    }

    private fun bindContainerId(containerId: Int, to: ISupportFragment) {
        val args = getArguments(to as Fragment)
        args.putInt(FRAGMENTATION_ARG_CONTAINER, containerId)
    }

    private fun getArguments(fragment: Fragment): Bundle {
        var bundle = fragment.arguments
        if (bundle == null) {
            bundle = Bundle()
            fragment.arguments = bundle
        }
        return bundle
    }

    private fun supportCommit(fm: FragmentManager, transaction: FragmentTransaction) {
        transaction.commitNowAllowingStateLoss()
    }

    private fun handleLaunchMode(
        fm: FragmentManager, topFragment: ISupportFragment?,
        to: ISupportFragment, toFragmentTag: String?, launchMode: Int
    ): Boolean {
        if (topFragment == null) return false
        val stackToFragment = SupportHelper.findStackFragment(to.javaClass, toFragmentTag, fm)
            ?: return false
        if (launchMode == ISupportFragment.SINGLETOP) {
            if (to === topFragment || to.javaClass.name == topFragment.javaClass.name) {
                handleNewBundle(to, stackToFragment)
                return true
            }
        } else if (launchMode == ISupportFragment.SINGLETASK) {
            doPopTo(toFragmentTag, false, fm)
            mHandler.post { handleNewBundle(to, stackToFragment) }
            return true
        }
        return false
    }

    private fun handleNewBundle(toFragment: ISupportFragment, stackToFragment: ISupportFragment) {
        val argsNewBundle = toFragment.supportDelegate.mNewBundle
        val args = getArguments(toFragment as Fragment)
        if (args.containsKey(FRAGMENTATION_ARG_CONTAINER)) {
            args.remove(FRAGMENTATION_ARG_CONTAINER)
        }
        if (argsNewBundle != null) {
            args.putAll(argsNewBundle)
        }
        stackToFragment.onNewBundle(args)
    }

    /**
     * save requestCode
     */
    private fun saveRequestCode(
        fm: FragmentManager,
        from: Fragment,
        to: Fragment,
        requestCode: Int
    ) {
        val bundle = getArguments(to)
        val resultRecord = ResultRecord()
        resultRecord.requestCode = requestCode
        bundle.putParcelable(FRAGMENTATION_ARG_RESULT_RECORD, resultRecord)
        fm.putFragment(bundle, FRAGMENTATION_STATE_SAVE_RESULT, from)
    }

    private fun doPopTo(
        targetFragmentTag: String?,
        includeTargetFragment: Boolean,
        fm: FragmentManager
    ) {
        val targetFragment = fm.findFragmentByTag(targetFragmentTag)
        if (targetFragment == null) {
            Log.e(
                TAG,
                "Pop failure! Can't find FragmentTag:$targetFragmentTag in the FragmentManager's Stack."
            )
            return
        }
        var preFragment: Fragment? = null
        if (includeTargetFragment) {
            preFragment = SupportHelper.getPreFragment(targetFragment) as Fragment?
            if (preFragment == null) {
                Log.e(TAG, "Pop failure! Can't find targetFragment in the FragmentManager's Stack.")
                return
            }
        }
        val willPopFragments =
            SupportHelper.getWillPopFragments(fm, targetFragmentTag, includeTargetFragment)
        if (willPopFragments.size <= 0) return
        val top = willPopFragments[0]
        val ft = fm.beginTransaction()
        willPopFragments.removeAt(0)
        for (fragment in willPopFragments) {
            ft.remove(fragment)
        }
        val record = (top as ISupportFragment).supportDelegate.mTransactionRecord
        if (record != null) {
            if (record.currentFragmentPopEnter != Int.MIN_VALUE) {
                (targetFragment as ISupportFragment).supportDelegate.hasEnterAnimation = true
                ft.setCustomAnimations(
                    record.currentFragmentPopEnter,
                    record.targetFragmentExit,
                    0,
                    0
                )
            }
        } else {
            if (mSupport.fragmentAnimator.currentFragmentPopEnter > 0) {
                (targetFragment as ISupportFragment).supportDelegate.hasEnterAnimation = true
                ft.setCustomAnimations(
                    mSupport.fragmentAnimator.currentFragmentPopEnter,
                    mSupport.fragmentAnimator.targetFragmentExit,
                    0,
                    0
                )
            } else if (default?.currentFragmentPopEnter ?: 0 > 0) {
                (targetFragment as ISupportFragment).supportDelegate.hasEnterAnimation = true
                ft.setCustomAnimations(
                    default!!.currentFragmentPopEnter, default!!.targetFragmentExit,
                    0, 0
                )
            }
        }
        ft.remove(top)
        if (includeTargetFragment) {
            ft.show(preFragment!!)
            ft.setMaxLifecycle(preFragment, Lifecycle.State.RESUMED)
        } else {
            ft.show(targetFragment)
            ft.setMaxLifecycle(targetFragment, Lifecycle.State.RESUMED)
        }
        supportCommit(fm, ft)
    }

    private fun safePopTo(fm: FragmentManager, willPopFragments: List<Fragment>) {
        val ft = fm.beginTransaction()
        for (fragment in willPopFragments) {
            ft.remove(fragment)
        }
        supportCommit(fm, ft)
    }

    companion object {
        private const val TAG = "Fragmentation"
        const val FRAGMENTATION_ARG_RESULT_RECORD = "fragment_arg_result_record"
        const val FRAGMENTATION_ARG_CONTAINER = "fragmentation_arg_container"
        private const val FRAGMENTATION_STATE_SAVE_RESULT = "fragmentation_state_save_result"
        const val TYPE_ADD = 0
        const val TYPE_ADD_RESULT = 1
        const val TYPE_ADD_WITHOUT_HIDE = 2
        const val TYPE_ADD_RESULT_WITHOUT_HIDE = 3
        const val TYPE_REPLACE = 4
        private fun <T> checkNotNull(value: T?) {
            if (value == null) {
                throw NullPointerException("toFragment == null")
            }
        }
    }

    init {
        mHandler = Handler(Looper.getMainLooper())
        mActionQueue = ActionQueue(mHandler)
    }
}