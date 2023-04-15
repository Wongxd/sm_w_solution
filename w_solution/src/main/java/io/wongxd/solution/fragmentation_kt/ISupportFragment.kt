
package io.wongxd.solution.fragmentation_kt

import android.os.Bundle
import androidx.annotation.IntDef

interface ISupportFragment {
    @IntDef(STANDARD, SINGLETOP, SINGLETASK)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LaunchMode

    val supportDelegate: SupportFragmentDelegate

    fun extraTransaction(): ExtraTransaction?

    fun post(runnable: Runnable)

    fun lazyInit()

    fun setFragmentResult(resultCode: Int, bundle: Bundle?)

    fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?)

    fun onNewBundle(args: Bundle?)

    fun putNewBundle(newBundle: Bundle?)

    fun onBackPressedSupport(): Boolean

    fun onVisible()

    fun onInvisible()

    companion object {
        // LaunchMode
        const val STANDARD = 0
        const val SINGLETOP = 1
        const val SINGLETASK = 2

        // ResultCode
        const val RESULT_CANCELED = 0
        const val RESULT_OK = -1
    }
}