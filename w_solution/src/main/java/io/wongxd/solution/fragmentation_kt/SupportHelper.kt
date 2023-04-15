
package io.wongxd.solution.fragmentation_kt

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.util.*

object SupportHelper {
    private const val SHOW_SPACE = 200L

    /**
     * 显示软键盘
     */
    @JvmStatic
    fun showSoftInput(view: View?) {
        if (view == null || view.context == null) return
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.requestFocus()
        view.postDelayed(
            Runnable { imm.showSoftInput(view, InputMethodManager.SHOW_FORCED) },
            SHOW_SPACE
        )
    }

    /**
     * 隐藏软键盘
     */
    @JvmStatic
    fun hideSoftInput(view: View?) {
        if (view == null || view.context == null) return
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 获得栈顶
     * supportDelegate.isStartByFragmentation 为 true
     * 的SupportFragment
     */
    fun getTopFragment(fragmentManager: FragmentManager): ISupportFragment? {
        return getTopFragment(fragmentManager, 0)
    }

    fun getTopFragment(fragmentManager: FragmentManager, containerId: Int): ISupportFragment? {
        val fragmentList = getActiveFragments(fragmentManager)
        for (i in fragmentList.indices.reversed()) {
            val fragment = fragmentList[i]
            if (fragment is ISupportFragment && fragment.supportDelegate.isStartByFragmentation) {
                val iFragment = fragment as ISupportFragment
                if (containerId == 0) return iFragment
                if (containerId == iFragment.supportDelegate.mContainerId) {
                    return iFragment
                }
            }
        }
        return null
    }

    /**
     * 获取目标Fragment的前一个
     * supportDelegate.isStartByFragmentation 为 true
     * 的SupportFragment
     *
     * @param fragment 目标Fragment
     */
    fun getPreFragment(fragment: Fragment): ISupportFragment? {
        val fragmentManager = fragment.parentFragmentManager
        val fragmentList = getActiveFragments(fragmentManager)
        val index = fragmentList.indexOf(fragment)
        for (i in index - 1 downTo 0) {
            val preFragment = fragmentList[i]
            if (preFragment is ISupportFragment && preFragment.supportDelegate.isStartByFragmentation) {
                return preFragment
            }
        }
        return null
    }

    /**
     * Same as fragmentManager.findFragmentByTag(fragmentClass.getName());
     * find Fragment from FragmentStack
     */
    fun <T : ISupportFragment?> findFragment(
        fragmentManager: FragmentManager,
        fragmentClass: Class<T>?
    ): T? {
        return findStackFragment(fragmentClass, null, fragmentManager)
    }

    /**
     * Same as fragmentManager.findFragmentByTag(fragmentTag);
     *
     *
     * find Fragment from FragmentStack
     */
    fun <T : ISupportFragment?> findFragment(
        fragmentManager: FragmentManager,
        fragmentTag: String?
    ): T? {
        return findStackFragment(null, fragmentTag, fragmentManager)
    }

    /**
     * 从栈顶开始，寻找FragmentManager以及其所有子栈,
     * 直到找到状态为show & userVisible
     * 且supportDelegate.isStartByFragmentation 为 true
     * 的SupportFragment
     */
    fun getActiveFragment(fragmentManager: FragmentManager): ISupportFragment? {
        return getActiveFragment(fragmentManager, null)
    }

    fun <T : ISupportFragment?> findStackFragment(
        fragmentClass: Class<T>?,
        toFragmentTag: String?,
        fragmentManager: FragmentManager
    ): T? {
        var fragment: Fragment? = null
        if (toFragmentTag == null) {
            val fragmentList = getActiveFragments(fragmentManager)
            val sizeChildFrgList = fragmentList.size
            for (i in sizeChildFrgList - 1 downTo 0) {
                val brotherFragment = fragmentList[i]
                if (brotherFragment is ISupportFragment && brotherFragment.javaClass.name == fragmentClass!!.name) {
                    fragment = brotherFragment
                    break
                }
            }
        } else {
            fragment = fragmentManager.findFragmentByTag(toFragmentTag)
            if (fragment == null) return null
        }
        return fragment as T?
    }

    private fun getActiveFragment(
        fragmentManager: FragmentManager,
        parentFragment: ISupportFragment?
    ): ISupportFragment? {
        val fragmentList = getActiveFragments(fragmentManager)
        for (i in fragmentList.indices.reversed()) {
            val fragment = fragmentList[i]
            if (fragment is ISupportFragment) {
                if (fragment.isResumed && !fragment.isHidden && fragment.supportDelegate.isStartByFragmentation) {
                    return getActiveFragment(
                        fragment.childFragmentManager,
                        fragment as ISupportFragment
                    )
                }
            }
        }
        return parentFragment
    }

    fun getWillPopFragments(
        fm: FragmentManager,
        targetTag: String?,
        includeTarget: Boolean
    ): MutableList<Fragment> {
        val target = fm.findFragmentByTag(targetTag)
        val willPopFragments: MutableList<Fragment> = ArrayList()
        val fragmentList = getActiveFragments(fm)
        val size = fragmentList.size
        var startIndex = -1
        for (i in size - 1 downTo 0) {
            if (target === fragmentList[i]) {
                if (includeTarget) {
                    startIndex = i
                } else if (i + 1 < size) {
                    startIndex = i + 1
                }
                break
            }
        }
        if (startIndex == -1) return willPopFragments
        for (i in size - 1 downTo startIndex) {
            val fragment = fragmentList[i]
            if (fragment.view != null) {
                willPopFragments.add(fragment)
            }
        }
        return willPopFragments
    }

    @JvmStatic
    fun getActiveFragments(fragmentManager: FragmentManager): List<Fragment> {
        return fragmentManager.fragments
    }
}