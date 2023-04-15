package io.wongxd.solution.camera

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.wongxd.solution.camera.core.CameraSurfaceView
import io.wongxd.solution.camera.core.CameraUtils
import io.wongxd.solution.camerax.CameraNoPerCompose
import io.wongxd.solution.compose.common.noRippleClickable
import io.wongxd.solution.logger.Logger
import io.wongxd.solution.permission.core.PermissionRequired
import io.wongxd.solution.permission.core.rememberPermissionState

@Composable
fun CameraCompose(
    cameraOldHolder: CameraOldHolder,
    orientation: Int = -1,
    openBack: Boolean = true,
    afterCameraGet: () -> Unit = {}
) {

    val permission = android.Manifest.permission.CAMERA
    // 定义 Permission State
    val permissionState = rememberPermissionState(permission)
    PermissionRequired(
        permissionState = permissionState,
        permissionNotAvailableContent = {
            // 权限获取失败 Deny & don’t ask again
            Logger.d("相机权限:permissionNotAvailableContent")
            CameraNoPerCompose(
                msg = "去'设置-权限'中手动授予相机权限",
                onclick = {
                    permissionState.jump2Setting()
                })
        }, permissionNotGrantedContent = {
            // 尚未获取权限时 Deny
            Logger.d("相机权限:permissionNotGrantedContent")
            CameraNoPerCompose(
                msg = "请求相机权限",
                onclick = {
                    permissionState.launchPermissionRequest()
                })
        }, content = {
            // 权限获取成功 Granted
            Logger.d("相机权限:content")
            RealCameraCompose(
                cameraOldHolder = cameraOldHolder,
                orientation = orientation,
                openBack = openBack
            )
            afterCameraGet.invoke()
        }
    )


    LaunchedEffect(key1 = Unit, block = {
        permissionState.launchPermissionRequest()
    })

}

@Composable
private fun RealCameraCompose(
    cameraOldHolder: CameraOldHolder,
    orientation: Int = -1,
    openBack: Boolean = true
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .noRippleClickable {
            cameraOldHolder.cameraUtils?.autoFocus()
        }) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
//                    val surfaceView = SurfaceView(ctx)
//                    surfaceView.background = ColorDrawable(Color.RED)
//                    cameraOldHolder.initCameraAndPreview(surfaceView)
//                    surfaceView
                val cameraUtils = CameraUtils()
                if (orientation == -1)
                    cameraUtils.calculateCameraPreviewOrientation(ctx as Activity)
                else
                    cameraUtils.previewOrientation = orientation
                val tmp = CameraSurfaceView(ctx, cameraUtils, openBack)
                cameraOldHolder.cameraUtils = cameraUtils
                tmp
            },
            update = {

            }
        )

    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleObserver = remember {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    cameraOldHolder.startPreview()
                    cameraOldHolder.cameraUtils?.autoFocus()
                    cameraOldHolder.cameraUtils?.setZoomIn()
                }
                Lifecycle.Event.ON_STOP -> {
                    cameraOldHolder.stopPreview()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    cameraOldHolder.stopPreview()
                    cameraOldHolder.cameraUtils?.releaseCamera()
                }
                else -> {}
            }
        }
    }
    DisposableEffect(key1 = lifecycle, key2 = lifecycleObserver, effect = {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            cameraOldHolder.cameraUtils?.resetZoom()
            cameraOldHolder.stopPreview()
            cameraOldHolder.cameraUtils?.releaseCamera()
        }
    })

}

