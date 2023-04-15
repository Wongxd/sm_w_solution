package io.wongxd.solution.camera

import android.view.SurfaceView
import io.wongxd.solution.camera.core.CameraUtils

class CameraOldHolder {
    var surfaceView: SurfaceView? = null
//    private var surfaceHolder: SurfaceHolder? = null

    var cameraUtils: CameraUtils? = null

//    fun initCameraAndPreview(surfaceView: SurfaceView) {
//        try {
//            this.surfaceView = surfaceView
//            this.surfaceView?.visibility = View.VISIBLE
//            surfaceHolder = surfaceView.holder
//            CameraManager.get().openDriver(surfaceHolder)
//            surfaceHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    fun startPreview() {
//        surfaceView?.let {
//            initCameraAndPreview(it)
//        }
        cameraUtils?.startPreview()
    }

    fun stopPreview() {
//        surfaceView?.visibility = View.INVISIBLE
//        CameraManager.get().stopPreview()
//        CameraManager.get().closeDriver()
        cameraUtils?.stopPreview()
    }

    fun getCameraUtil() = cameraUtils

    fun getSurfaceView() = surfaceView


}