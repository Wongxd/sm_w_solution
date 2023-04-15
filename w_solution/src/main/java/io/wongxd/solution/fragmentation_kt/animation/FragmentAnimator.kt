
package io.wongxd.solution.fragmentation_kt.animation

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.AnimRes

/**
 * Fragment动画实体类
 */
open class FragmentAnimator : Parcelable {
    @AnimRes
    var targetFragmentEnter = 0
        protected set

    @AnimRes
    var currentFragmentPopExit = 0
        protected set

    @AnimRes
    var currentFragmentPopEnter = 0
        protected set

    @AnimRes
    var targetFragmentExit = 0
        protected set

    constructor() {}
    constructor(
        targetFragmentEnter: Int,
        currentFragmentPopExit: Int,
        currentFragmentPopEnter: Int,
        targetFragmentExit: Int
    ) {
        this.targetFragmentEnter = targetFragmentEnter
        this.currentFragmentPopExit = currentFragmentPopExit
        this.currentFragmentPopEnter = currentFragmentPopEnter
        this.targetFragmentExit = targetFragmentExit
    }

    protected constructor(`in`: Parcel) {
        targetFragmentEnter = `in`.readInt()
        currentFragmentPopExit = `in`.readInt()
        currentFragmentPopEnter = `in`.readInt()
        targetFragmentExit = `in`.readInt()
    }

    fun setTargetFragmentEnter(targetFragmentEnter: Int): FragmentAnimator {
        this.targetFragmentEnter = targetFragmentEnter
        return this
    }

    fun setCurrentFragmentPopExit(currentFragmentPopExit: Int): FragmentAnimator {
        this.currentFragmentPopExit = currentFragmentPopExit
        return this
    }

    fun setCurrentFragmentPopEnter(currentFragmentPopEnter: Int): FragmentAnimator {
        this.currentFragmentPopEnter = currentFragmentPopEnter
        return this
    }

    fun setTargetFragmentExit(targetFragmentExit: Int): FragmentAnimator {
        this.targetFragmentExit = targetFragmentExit
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(targetFragmentEnter)
        dest.writeInt(currentFragmentPopExit)
        dest.writeInt(currentFragmentPopEnter)
        dest.writeInt(targetFragmentExit)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<FragmentAnimator?> =
            object : Parcelable.Creator<FragmentAnimator?> {
                override fun createFromParcel(parcel: Parcel): FragmentAnimator {
                    return FragmentAnimator(parcel)
                }

                override fun newArray(size: Int): Array<FragmentAnimator?> {
                    return arrayOfNulls(size)
                }
            }
    }
}