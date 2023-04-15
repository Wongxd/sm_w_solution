package io.wongxd.solution.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.wongxd.solution.compose.composeTheme.black
import io.wongxd.solution.compose.composeTheme.textStyle

@Composable
internal fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        buttons = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider()
                Text(
                    text = if (isPermanentlyDeclined) {
                        "去授权"
                    } else {
                        "OK"
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.textStyle.sp14.black(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isPermanentlyDeclined) {
                                onGoToAppSettingsClick()
                            } else {
                                onOkClick()
                            }
                        }
                        .padding(16.dp)
                )
                Divider()
                Text(
                    text = "取消",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.textStyle.sp14.black(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss.invoke()
                        }
                        .padding(16.dp)
                )
            }
        },
        title = {
            Text(text = "需要权限", style = MaterialTheme.textStyle.sp16.black())
        },
        text = {
            Text(
                text = permissionTextProvider.getDescription(
                    isPermanentlyDeclined = isPermanentlyDeclined
                ),
                style = MaterialTheme.textStyle.sp16.black()
            )
        },
        modifier = modifier.background(Color.White)
    )
}

sealed interface PermissionTextProvider {

    val youCanGoToSettingToGrantIt: String
        get() = "你可以到设置中去授予"

    fun getDescription(isPermanentlyDeclined: Boolean): String
}

object CameraPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "相机权限被禁止,$youCanGoToSettingToGrantIt"
        } else {
            "需要相机权限以进行下一步"
        }
    }
}

object ReadWriteFilePermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "文件权限被禁止,$youCanGoToSettingToGrantIt"
        } else {
            "需要文件权限以进行下一步"
        }
    }
}

object RecordAudioPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "录音权限被禁止,$youCanGoToSettingToGrantIt"
        } else {
            "需要录音权限以进行下一步"
        }
    }
}

object GpsPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "Gps权限被禁止,$youCanGoToSettingToGrantIt"
        } else {
            "需要Gps权限以进行下一步"
        }
    }
}

object SensorPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "传感器权限被禁止,$youCanGoToSettingToGrantIt"
        } else {
            "需要传感器权限以进行下一步"
        }
    }
}