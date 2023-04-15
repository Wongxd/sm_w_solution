package io.wongxd.solution.camerax

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.wongxd.solution.logger.Logger
import io.wongxd.solution.permission.core.PermissionRequired
import io.wongxd.solution.permission.core.rememberPermissionState

@Composable
fun CameraXCompose(
    cameraXHolder: CameraXHolder,
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
                cameraXHolder,
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
fun CameraNoPerCompose(msg: String = "请求相机权限", onclick: (() -> Unit)? = null) {
    val permission = android.Manifest.permission.CAMERA
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->

            })


    Box(
        modifier = Modifier
            .zIndex(99f)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = {
            onclick?.invoke() ?: launcher.launch(permission)
        }) {
            Text(msg)
        }
    }
}

@Composable
private fun RealCameraCompose(
    cameraXHolder: CameraXHolder,
    openBack: Boolean = true
) {

    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFeature = remember {
        ProcessCameraProvider.getInstance(ctx)
    }

    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context)
            val preview = Preview.Builder().build()
            val selector = CameraSelector.Builder()
                .requireLensFacing(if (openBack) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT)
                .build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            try {
                cameraXHolder.camera = cameraProviderFeature.get()
                    .bindToLifecycle(lifecycleOwner, selector, preview)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleObserver = remember {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {

                }
                Lifecycle.Event.ON_STOP -> {

                }
                Lifecycle.Event.ON_DESTROY -> {

                }
                else -> {}
            }
        }
    }
    DisposableEffect(key1 = lifecycle, key2 = lifecycleObserver, effect = {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    })

}

