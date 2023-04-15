
package io.wongxd.solution.fragmentation_kt

import android.view.MotionEvent
import io.wongxd.solution.fragmentation_kt.animation.FragmentAnimator

interface ISupportActivity {
    val supportDelegate: SupportActivityDelegate

    var fragmentAnimator: FragmentAnimator

    fun extraTransaction(): ExtraTransaction?

    fun post(runnable: Runnable)

    fun onBackPressed()

    fun onBackPressedSupport()

    fun dispatchTouchEvent(ev: MotionEvent?): Boolean

}