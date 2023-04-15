
package io.wongxd.solution.fragmentation_kt

import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.wongxd.solution.fragmentation_kt.ISupportFragment.LaunchMode
import io.wongxd.solution.fragmentation_kt.record.TransactionRecord

abstract class ExtraTransaction {
    /**
     * @param tag Optional tag name for the fragment, to later retrieve the
     * fragment with [SupportHelper.findFragment]
     * , pop(String)
     * or FragmentManager.findFragmentByTag(String).
     */
    abstract fun setTag(tag: String?): ExtraTransaction

    /**
     * Set specific animation resources to run for the fragments that are
     * entering and exiting in this transaction. The `currentFragmentPopEnter`
     * and `targetFragmentExit` animations will be played for targetFragmentEnter/currentFragmentPopExit
     * operations specifically when popping the back stack.
     */
    abstract fun setCustomAnimations(
        @AnimatorRes @AnimRes targetFragmentEnter: Int,
        @AnimatorRes @AnimRes currentFragmentPopExit: Int,
        @AnimatorRes @AnimRes currentFragmentPopEnter: Int,
        @AnimatorRes @AnimRes targetFragmentExit: Int
    ): ExtraTransaction

    abstract fun loadRootFragment(containerId: Int, toFragment: ISupportFragment)
    abstract fun loadChildRootFragment(containerId: Int, toFragment: ISupportFragment)
    abstract fun start(toFragment: ISupportFragment)
    abstract fun startChild(toFragment: ISupportFragment)
    abstract fun startNotHideSelf(toFragment: ISupportFragment)
    abstract fun startChildNotHideSelf(toFragment: ISupportFragment)
    abstract fun startNotHideSelf(toFragment: ISupportFragment, @LaunchMode launchMode: Int)
    abstract fun startChildNotHideSelf(toFragment: ISupportFragment, @LaunchMode launchMode: Int)
    abstract fun start(toFragment: ISupportFragment, @LaunchMode launchMode: Int)
    abstract fun startChild(toFragment: ISupportFragment, @LaunchMode launchMode: Int)
    abstract fun startForResult(toFragment: ISupportFragment, requestCode: Int)
    abstract fun startChildForResult(toFragment: ISupportFragment, requestCode: Int)
    abstract fun startForResultNotHideSelf(toFragment: ISupportFragment, requestCode: Int)
    abstract fun startChildForResultNotHideSelf(toFragment: ISupportFragment, requestCode: Int)
    abstract fun startWithPop(toFragment: ISupportFragment)
    abstract fun startChildWithPop(toFragment: ISupportFragment)
    abstract fun startWithPopTo(
        toFragment: ISupportFragment,
        targetFragmentTag: String?,
        includeTargetFragment: Boolean
    )

    abstract fun startChildWithPopTo(
        toFragment: ISupportFragment,
        targetFragmentTag: String?,
        includeTargetFragment: Boolean
    )

    abstract fun replace(toFragment: ISupportFragment)
    abstract fun replaceChild(toFragment: ISupportFragment)
    abstract fun remove(toFragment: ISupportFragment?)
    abstract fun removeWithAnimation(toFragment: ISupportFragment?, targetFragmentExit: Int)
    abstract fun removeChild(toFragment: ISupportFragment?)
    abstract fun removeChildWithAnimation(toFragment: ISupportFragment?, targetFragmentExit: Int)

    /**
     * 使用setTag()自定义Tag时，使用下面popTo()／popToChild()出栈
     *
     * @param targetFragmentTag     通过setTag()设置的tag
     * @param includeTargetFragment 是否包含目标(Tag为targetFragmentTag)Fragment
     */
    abstract fun popTo(targetFragmentTag: String?, includeTargetFragment: Boolean)
    abstract fun popToChild(targetFragmentTag: String?, includeTargetFragment: Boolean)
    abstract fun popTo(
        targetFragmentTag: String?,
        includeTargetFragment: Boolean,
        afterPopTransactionRunnable: Runnable?
    )

    abstract fun popToChild(
        targetFragmentTag: String?,
        includeTargetFragment: Boolean,
        afterPopTransactionRunnable: Runnable?
    )

    /**
     * Impl
     */
    internal class ExtraTransactionImpl<T : ISupportFragment>(
        private val mActivity: FragmentActivity,
        private val mSupportF: T?,
        transactionDelegate: TransactionDelegate,
        fromActivity: Boolean
    ) : ExtraTransaction() {
        private val mFragment: Fragment?
        private val mTransactionDelegate: TransactionDelegate
        private val mFromActivity: Boolean
        private val mRecord: TransactionRecord
        override fun start(toFragment: ISupportFragment) {
            start(toFragment, ISupportFragment.STANDARD)
        }

        override fun startChild(toFragment: ISupportFragment) {
            startChild(toFragment, ISupportFragment.STANDARD)
        }

        override fun replace(toFragment: ISupportFragment) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                fragmentManager, mSupportF,
                toFragment, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_REPLACE
            )
        }

        override fun replaceChild(toFragment: ISupportFragment) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                childFragmentManager, mSupportF,
                toFragment, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_REPLACE
            )
        }

        override fun setTag(tag: String?): ExtraTransaction {
            mRecord.tag = tag
            return this
        }

        override fun setCustomAnimations(
            targetFragmentEnter: Int,
            currentFragmentPopExit: Int,
            currentFragmentPopEnter: Int,
            targetFragmentExit: Int
        ): ExtraTransaction {
            mRecord.targetFragmentEnter = targetFragmentEnter
            mRecord.currentFragmentPopExit = currentFragmentPopExit
            mRecord.currentFragmentPopEnter = currentFragmentPopEnter
            mRecord.targetFragmentExit = targetFragmentExit
            return this
        }

        override fun loadRootFragment(containerId: Int, toFragment: ISupportFragment) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.loadRootTransaction(fragmentManager, containerId, toFragment)
        }

        override fun loadChildRootFragment(containerId: Int, toFragment: ISupportFragment) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.loadRootTransaction(childFragmentManager, containerId, toFragment)
        }

        override fun remove(toFragment: ISupportFragment?) {
            mTransactionDelegate.remove(fragmentManager, toFragment)
        }

        override fun removeWithAnimation(toFragment: ISupportFragment?, targetFragmentExit: Int) {
            mTransactionDelegate.remove(fragmentManager, toFragment, targetFragmentExit)
        }

        override fun removeChild(toFragment: ISupportFragment?) {
            mTransactionDelegate.remove(childFragmentManager, toFragment)
        }

        override fun removeChildWithAnimation(
            toFragment: ISupportFragment?,
            targetFragmentExit: Int
        ) {
            mTransactionDelegate.remove(childFragmentManager, toFragment, targetFragmentExit)
        }

        override fun popTo(targetFragmentTag: String?, includeTargetFragment: Boolean) {
            popTo(targetFragmentTag, includeTargetFragment, null)
        }

        override fun popTo(
            targetFragmentTag: String?, includeTargetFragment: Boolean,
            afterPopTransactionRunnable: Runnable?
        ) {
            mTransactionDelegate.popTo(
                targetFragmentTag, includeTargetFragment,
                afterPopTransactionRunnable, fragmentManager
            )
        }

        override fun popToChild(targetFragmentTag: String?, includeTargetFragment: Boolean) {
            popToChild(targetFragmentTag, includeTargetFragment, null)
        }

        override fun popToChild(
            targetFragmentTag: String?, includeTargetFragment: Boolean,
            afterPopTransactionRunnable: Runnable?
        ) {
            mTransactionDelegate.popTo(
                targetFragmentTag, includeTargetFragment,
                afterPopTransactionRunnable, childFragmentManager
            )
        }

        override fun startNotHideSelf(toFragment: ISupportFragment) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                fragmentManager, mSupportF,
                toFragment, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_WITHOUT_HIDE
            )
        }

        override fun startChildNotHideSelf(toFragment: ISupportFragment) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                childFragmentManager, mSupportF,
                toFragment, 0, ISupportFragment.STANDARD, TransactionDelegate.TYPE_ADD_WITHOUT_HIDE
            )
        }

        override fun startNotHideSelf(toFragment: ISupportFragment, @LaunchMode launchMode: Int) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                fragmentManager, mSupportF,
                toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD_WITHOUT_HIDE
            )
        }

        override fun startChildNotHideSelf(
            toFragment: ISupportFragment,
            @LaunchMode launchMode: Int
        ) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                childFragmentManager, mSupportF,
                toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD_WITHOUT_HIDE
            )
        }

        override fun start(toFragment: ISupportFragment, @LaunchMode launchMode: Int) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                fragmentManager, mSupportF,
                toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD
            )
        }

        override fun startChild(toFragment: ISupportFragment, @LaunchMode launchMode: Int) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                childFragmentManager, mSupportF,
                toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD
            )
        }

        override fun startForResult(toFragment: ISupportFragment, requestCode: Int) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                fragmentManager,
                mSupportF,
                toFragment,
                requestCode,
                ISupportFragment.STANDARD,
                TransactionDelegate.TYPE_ADD_RESULT
            )
        }

        override fun startChildForResult(toFragment: ISupportFragment, requestCode: Int) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                childFragmentManager,
                mSupportF,
                toFragment,
                requestCode,
                ISupportFragment.STANDARD,
                TransactionDelegate.TYPE_ADD_RESULT
            )
        }

        override fun startForResultNotHideSelf(toFragment: ISupportFragment, requestCode: Int) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                fragmentManager,
                mSupportF,
                toFragment,
                requestCode,
                ISupportFragment.STANDARD,
                TransactionDelegate.TYPE_ADD_RESULT_WITHOUT_HIDE
            )
        }

        override fun startChildForResultNotHideSelf(
            toFragment: ISupportFragment,
            requestCode: Int
        ) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartTransaction(
                childFragmentManager,
                mSupportF,
                toFragment,
                requestCode,
                ISupportFragment.STANDARD,
                TransactionDelegate.TYPE_ADD_RESULT_WITHOUT_HIDE
            )
        }

        override fun startWithPop(toFragment: ISupportFragment) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartWithPopTransaction(
                fragmentManager,
                mSupportF,
                toFragment
            )
        }

        override fun startChildWithPop(toFragment: ISupportFragment) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartWithPopTransaction(
                childFragmentManager,
                mSupportF,
                toFragment
            )
        }

        override fun startWithPopTo(
            toFragment: ISupportFragment,
            targetFragmentTag: String?,
            includeTargetFragment: Boolean
        ) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartWithPopToTransaction(
                fragmentManager, mSupportF,
                toFragment, targetFragmentTag, includeTargetFragment
            )
        }

        override fun startChildWithPopTo(
            toFragment: ISupportFragment,
            targetFragmentTag: String?,
            includeTargetFragment: Boolean
        ) {
            toFragment.supportDelegate.mTransactionRecord = mRecord
            mTransactionDelegate.dispatchStartWithPopToTransaction(
                childFragmentManager, mSupportF,
                toFragment, targetFragmentTag, includeTargetFragment
            )
        }

        private val fragmentManager: FragmentManager
            get() = mFragment?.parentFragmentManager ?: mActivity.supportFragmentManager
        private val childFragmentManager: FragmentManager
            get() = mFragment?.childFragmentManager!!

        init {
            mFragment = mSupportF as Fragment
            mTransactionDelegate = transactionDelegate
            mFromActivity = fromActivity
            mRecord = TransactionRecord()
        }
    }
}