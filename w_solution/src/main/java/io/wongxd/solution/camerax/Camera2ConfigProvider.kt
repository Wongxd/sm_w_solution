package io.wongxd.solution.camerax

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

interface Camera2ConfigProvider : CameraXConfig.Provider {

    @SuppressLint("UnsafeOptInUsageError")
    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.ERROR)
            .build();
    }

}