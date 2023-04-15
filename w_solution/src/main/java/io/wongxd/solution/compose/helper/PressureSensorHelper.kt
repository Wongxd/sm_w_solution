package io.wongxd.solution.compose.helper

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver


class PressureSensorHelper(
    private val getInfo: ((Pair<Boolean, Float>) -> Unit)? = null
) {

    //<editor-fold desc="传感器">
    private var sensorManager: SensorManager? = null
    private var pressureSensor: Sensor? = null

    private val sensorEventListener by lazy {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return
                val sPV = event.values[0]

                getInfo?.invoke(true to sPV)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }
        }
    }

    fun init(ctx: Context) {
        sensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    fun onSensorActive() {
        pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)

        // 给传感器注册监听：
        sensorManager?.registerListener(
            sensorEventListener,
            pressureSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        getInfo?.invoke(false to 0f)
    }

    fun onSensorDeActive() {
        // 取消传感器的监听
        sensorManager?.unregisterListener(sensorEventListener)
    }

    //</editor-fold>

}

@Composable
fun PressureSensorCompose(getInfo: ((Pair<Boolean, Float>) -> Unit)? = null) {
    val ctx = LocalContext.current

    val sensorHelper = remember {
        PressureSensorHelper(getInfo = getInfo).apply { init(ctx) }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, sensorHelper) {
        // Make it follow the current lifecycle
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    sensorHelper.onSensorActive()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    sensorHelper.onSensorDeActive()
                }
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

}