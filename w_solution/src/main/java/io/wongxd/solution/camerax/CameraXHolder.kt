package io.wongxd.solution.camerax

import androidx.camera.core.Camera
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CameraXHolder {

    var camera by mutableStateOf<Camera?>(null)

    fun setZoom(float: Float){
        camera?.cameraControl?.setLinearZoom(float)
    }

    fun getCurrentZoomLiveData() = camera?.cameraInfo?.zoomState

}