package io.wongxd.solution.baseView

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import io.wongxd.solution.R
import io.wongxd.solution.delegate.activityArgumentNullable
import io.wongxd.solution.delegate.set
import io.wongxd.solution.util.ext.setCustomDensity
import io.wongxd.solution.util.hideNavKey
import io.wongxd.solution.util.setFullScreen

class AtyContainer : BaseRootActivityLast() {

    companion object {
        private const val ATY_CONTAINER_FULLSCREEN_KEY = "aty_container_fullscreen_key"
        private const val ATY_CONTAINER_LANDSCAPE_KEY = "aty_container_landscape_key"
        private const val ATY_CONTAINER_FRAGMENT_KEY = "aty_container_fragment_key"
        private const val ATY_CONTAINER_FRAGMENT_ARGS_KEY = "aty_container_fragment_args_key"
        fun startFgt(
            ctx: Context,
            fgt: BaseRootFragmentLast,
            fullScreen: Boolean = false,
            landscape: Boolean = false
        ) {
            ctx.startActivity(Intent(ctx, AtyContainer::class.java).apply {
                this["fullScreen"] = fullScreen
                this["landscape"] = landscape
                this["initFgtClassName"] = fgt::class.java.name
                this.putExtra("fgtArgs", fgt.arguments)
            })
        }
    }

    private val fullScreen by activityArgumentNullable<Boolean>()
    private val landscape by activityArgumentNullable<Boolean>()
    private val initFgtClassName by activityArgumentNullable<String>()
    private var fgtClassName = ""
    private var fgtArgs: Bundle? = null

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(ATY_CONTAINER_FULLSCREEN_KEY, fullScreen ?: false)
        outState.putBoolean(ATY_CONTAINER_LANDSCAPE_KEY, landscape ?: false)
        outState.putString(ATY_CONTAINER_FRAGMENT_KEY, initFgtClassName ?: fgtClassName)
        outState.putBundle(ATY_CONTAINER_FRAGMENT_ARGS_KEY, fgtArgs)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fgtArgs = intent.getBundleExtra("fgtArgs")

        var fullScreenFinal = fullScreen
        var landscapeFinal = landscape
        savedInstanceState?.let { si ->
            fullScreenFinal = si.getBoolean(ATY_CONTAINER_FULLSCREEN_KEY)
            landscapeFinal = si.getBoolean(ATY_CONTAINER_LANDSCAPE_KEY)
            fgtClassName = si.getString(ATY_CONTAINER_FRAGMENT_KEY) ?: ""
            fgtArgs = si.getBundle(ATY_CONTAINER_FRAGMENT_ARGS_KEY)
        }

        if (fullScreenFinal == true) {
            setFullScreen()
        }

        if (landscapeFinal == true) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            setCustomDensity(812f)
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.aty_container)
        val fgtIns = Class.forName(initFgtClassName ?: fgtClassName).newInstance()
        (fgtIns is BaseRootFragmentLast).let {
            if (it) loadRootFragment(R.id.fgt_container, (fgtIns as BaseRootFragmentLast).apply {
                arguments = fgtArgs
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (fullScreen == true) {
            hideNavKey()
        }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(0, 0)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

}