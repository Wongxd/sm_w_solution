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
import kotlin.math.abs


class GravitySensorHelper(
    getRotationDegree: ((Float) -> Unit)? = null
) {

    private val TAG = "GravitySensorHelper"

    //<editor-fold desc="传感器">
    //方位 z
    private val AZUMUTH: Int = 0

    //水平上下 y
    private val PITCH = 1

    //水平左右 x
    private val ROLL = 2


    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null

    private var count = 0
    private val sensorEventListener by lazy {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return

                /**
                 *  方向传感器提供三个数据，分别为azimuth、pitch和roll。
                 *
                 *  azimuth：方位，返回水平时磁北极和Y轴的夹角，范围为0°至360°。
                 *          0°=北，90°=东，180°=南，270°=西。
                 *
                 *  pitch：x轴和水平面的夹角，范围为-180°至180°。
                 *        当z轴向y轴转动时，角度为正值。
                 *
                 *  roll：y轴和水平面的夹角，由于历史原因，范围为-90°至90°。
                 *        当x轴向z轴移动时，角度为正值。
                 */
                val lowPass = event.values.lowPass()

                val angle = abs(lowPass[PITCH])
                if (count % 5 == 0) {
                    //镜头角度
                    getRotationDegree?.invoke(angle)
                }
                count++
//                Log.d(TAG, "$angle,$count")
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }
        }
    }

    private fun FloatArray.lowPass(): FloatArray {
        val ALPHA = 0.25f
        val input = this
        val output = FloatArray(input.size)
        for (i in input.indices) {
            output[i] = output[i] + ALPHA * (input[i] - output[i])
        }
        return output
    }

    fun init(ctx: Context) {
        sensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    fun onSensorActive() {
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        // 给传感器注册监听：
        sensorManager?.registerListener(
            sensorEventListener,
            sensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun onSensorDeActive() {
        // 取消方向传感器的监听
        sensorManager?.unregisterListener(sensorEventListener)
    }

    //</editor-fold>

}

@Composable
fun GravitySensorCompose(
    getRotationDegree: ((Float) -> Unit)? = null
) {
    val ctx = LocalContext.current

    val sensorHelper = remember {
        GravitySensorHelper(getRotationDegree = getRotationDegree).apply { init(ctx) }
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