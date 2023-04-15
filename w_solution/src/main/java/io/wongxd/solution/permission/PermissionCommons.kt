package io.wongxd.solution.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.wongxd.solution.compose.common.noRippleClickable
import io.wongxd.solution.logger.Logger
import io.wongxd.solution.permission.PermissionCommons.openAppSettings
import io.wongxd.solution.permission.core.*

object PermissionCommons {
    private const val TAG = "PermissionCommons"

    fun showLog(log: String) {
        Logger.d(TAG, log)
    }

    val REQUIRED_RECORD_AUDIO_PERMISSIONS =
        mutableListOf(Manifest.permission.RECORD_AUDIO).toTypedArray()

    val REQUIRED_LOCATION_PERMISSIONS = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).toTypedArray()

    val REQUIRED_GPS_PERMISSIONS = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
    ).toTypedArray()

    val REQUIRED_WIFI_LOCATION_PERMISSIONS = mutableListOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ).toTypedArray()

    val REQUIRED_CAMERA_PERMISSIONS =
        mutableListOf(
            Manifest.permission.CAMERA,
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
//                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

    fun Context.startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    fun Activity.reqPermission(
        permissionsToRequest: Array<out String>,
        autoJump2Settings: Boolean = true
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(permissionsToRequest, 0)

            val jump2Setting =
                permissionsToRequest.none { per ->
                    //需要到设置中授予
                    ActivityCompat.shouldShowRequestPermissionRationale(this, per).not()
                }.not()
            if (jump2Setting && autoJump2Settings) {
                startAppSettings()
            }
        }
    }

    fun Context.isPermissionGranted(vararg per: String) =
        per.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

    fun checkPermissionGranted(ctx: Context, vararg per: String) = ctx.isPermissionGranted(*per)

    private fun Activity.openAppSettings() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).also(::startActivity)
    }

    @Composable
    fun ReqPermission(
        permissionsToRequest: Array<String>,
        reqWhenFirstEnter: Boolean = true,
        content: @Composable ()->Unit = {},
        permissionResult: (granted: Boolean) -> Unit
    ) {
        val showedDialogQueue = remember {
            mutableStateListOf<String>()
        }

        val visiblePermissionDialogQueue = remember {
            mutableStateListOf<String>()
        }

        fun dismissDialog() {
            visiblePermissionDialogQueue.removeFirst()
        }

        fun onPermissionResult(
            permission: String,
            isGranted: Boolean
        ) {
            if (!isGranted
                && !visiblePermissionDialogQueue.contains(permission)
                && !showedDialogQueue.contains(permission)
            ) {
                showedDialogQueue.add(permission)
                visiblePermissionDialogQueue.add(permission)
            }
        }

        // 定义 Permission State
        val rememberMultiplePermissionsState =
            rememberMultiplePermissionsState(permissionsToRequest.toList())
        PermissionsRequired(
            multiplePermissionsState = rememberMultiplePermissionsState,
            permissionsNotAvailableContent = {
                // 权限获取失败 Deny & don’t ask again
                permissionResult.invoke(false)

                permissionsToRequest.forEach { permission ->
                    onPermissionResult(
                        permission = permission,
                        isGranted = rememberMultiplePermissionsState.permissions.any { it.permission == permission && it.hasPermission }
                    )
                }

                NoPerCompose(
                    msg = "去'设置-权限'中手动授予必要权限",
                    onclick = {
                        rememberMultiplePermissionsState.jump2Setting()
                    }, permissionsToRequest = permissionsToRequest
                )


            }, permissionsNotGrantedContent = {
                // 尚未获取权限时 Deny
                permissionResult.invoke(false)

                NoPerCompose(
                    msg = "点击请求必要权限",
                    onclick = {
                        rememberMultiplePermissionsState.launchMultiplePermissionRequest()
                    }, permissionsToRequest = permissionsToRequest
                )
            }, content = {
                // 权限获取成功 Granted
                content()

                permissionResult.invoke(true)
            }
        )

        LaunchedEffect(key1 = Unit, block = {
            if (reqWhenFirstEnter) rememberMultiplePermissionsState.launchMultiplePermissionRequest()
        })


        val aty = LocalContext.current.findActivity()
        visiblePermissionDialogQueue
            .reversed()
            .forEach { permission ->
                PermissionDialog(
                    permissionTextProvider = when (permission) {
                        Manifest.permission.CAMERA -> {
                            CameraPermissionTextProvider
                        }
                        Manifest.permission.RECORD_AUDIO -> {
                            RecordAudioPermissionTextProvider
                        }
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                            ReadWriteFilePermissionTextProvider
                        }
                        Manifest.permission.BODY_SENSORS -> {
                            SensorPermissionTextProvider
                        }
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION -> {
                            GpsPermissionTextProvider
                        }
                        else -> return@forEach
                    },
                    isPermanentlyDeclined = ActivityCompat.shouldShowRequestPermissionRationale(
                        aty,
                        permission
                    ),
                    onDismiss = { dismissDialog() },
                    onOkClick = {
                        dismissDialog()
                        rememberMultiplePermissionsState.launchMultiplePermissionRequest()
                    },
                    onGoToAppSettingsClick = {
                        dismissDialog()
                        aty.openAppSettings()
                    }
                )
            }

    }

    @Composable
    private fun NoPerCompose(
        permissionsToRequest: Array<String>,
        msg: String = "请求相机权限",
        onclick: (() -> Unit)? = null
    ) {

        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts
                .RequestMultiplePermissions(),
                onResult = { result ->

                })

        Box(
            modifier = Modifier
                .zIndex(99f)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                onclick?.invoke() ?: launcher.launch(permissionsToRequest)
            }) {
                Text(msg)
            }
        }
    }


}