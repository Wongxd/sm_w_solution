package io.wongxd.demo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.wongxd.solution.camera.CameraCompose
import io.wongxd.solution.camerax.CameraXCompose
import io.wongxd.solution.camerax.CameraXHolder
import io.wongxd.solution.compose.composeTheme.*

class AtyMain : AppCompatActivity() {

    private val cameraXHolder by lazy { CameraXHolder() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeHolder.themeAdapterAuto = true
        setContent {
            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppThemeHolder.colorAssets.white),
                    contentAlignment = Alignment.Center
                ) {

                    CameraXCompose(cameraXHolder = cameraXHolder, openBack = false)

                    Text(text = "你好", style = AppThemeHolder.textStyle.sp20.black().bold())
                }
            }
        }

    }
}