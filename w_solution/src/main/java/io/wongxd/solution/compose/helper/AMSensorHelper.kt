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
import kotlin.math.round


class AMSensorHelper(
    getAdjustInfo: ((Triple<Float, Float, Float>) -> Unit)? = null,
    getLevelInfo: ((Pair<Float, Float>) -> Unit)? = null,
    getComPassInfo: ((Pair<Float, String>) -> Unit)? = null,
    getMagnetic: ((Float) -> Unit)? = null,
    getRotationDegree: ((Float) -> Unit)? = null
) {

    private val TAG = "AMSensorHelper"

    //<editor-fold desc="传感器">
    //方位 z
    private val AZUMUTH: Int = 0

    //水平上下 y
    private val PITCH = 1

    //水平左右 x
    private val ROLL = 2


    private var sensorManager: SensorManager? = null
    private var accSensor: Sensor? = null
    private var magSensor: Sensor? = null

    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)

    // 旋转矩阵，用来保存磁场和加速度的数据
    private val rotationMatrix = FloatArray(9)

    // 模拟方向传感器的数据（原始数据为弧度）
    private val orientationAngles = FloatArray(3)

    private val sensorEventListener by lazy {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return

                // 获取手机触发event的传感器的类型
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        lowPass(event.values.clone(), accelerometerReading)
                        getRotationDegree?.invoke(getRotation())
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> lowPass(event.values.clone(), magnetometerReading)
                }

                //调用 getRotationMatrix 获得变换矩阵 rotationMatrix []
                SensorManager.getRotationMatrix(
                    rotationMatrix,
                    null,
                    accelerometerReading,
                    magnetometerReading
                )

                //经过SensorManager.getOrientation(R, orientationAngles) 得到的 orientationAngles 值为弧度
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                // x轴的偏转角度
                val degreeX = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                // y轴的偏转角度
                val degreeY = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
                // z轴的偏转角度
                val degreeZ = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

//                Logger.d(TAG, "x轴偏转角度 = $degreeX , y轴偏转角度 = $degreeY, z轴 = $degreeZ")

                getAdjustInfo?.invoke(Triple(degreeX, degreeY, degreeZ))

                //磁场
                getMagnetic?.invoke(magnetometerReading[0])

                //罗盘
                getComPassInfo?.invoke(getCompassInfo())


                //水平仪
                getLevelInfo?.invoke(getLevelInfo())
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }
        }
    }

    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        val ALPHA = 0.25f
        if (output == null) return input
        for (i in input.indices) {
            output[i] = output[i] + ALPHA * (input[i] - output[i])
        }
        return output
    }

    //https://www.cnblogs.com/happyxiaoyu02/archive/2012/10/10/6818973.html
    fun getRotation(): Float {
        val ax = accelerometerReading[0]
        val ay = accelerometerReading[1]

        val g = Math.sqrt((ax * ax + ay * ay).toDouble())
        var cos = ay / g

        if (cos > 1) {
            cos = 1.0
        } else if (cos < -1) {
            cos = -1.0
        }

        var rad = Math.acos(cos) //0-180

        if (ax < 0) {        //rad>180
            rad = 2 * Math.PI - rad
        }

        return Math.toDegrees(rad).toFloat()
    }

    private fun getLevelInfo(): Pair<Float, Float> {

        // 获取　沿着Z轴转过的角度
        val azimuth: Float = orientationAngles[0]

        // 获取　沿着X轴倾斜时　与Y轴的夹角
        val pitchAngle: Float = orientationAngles[1]

        // 获取　沿着Y轴的滚动时　与X轴的角度
        //此处与官方文档描述不一致，所在加了符号（https://developer.android.google.cn/reference/android/hardware/SensorManager.html#getOrientation(float[], float[])）
        val rollAngle: Float = -orientationAngles[2]

        //格式化
//                val horizontalDegrees = Math.toDegrees(rollAngle.toDouble())
//                val verticalDegrees = Math.toDegrees(pitchAngle.toDouble())

//        Logger.d(
//            "水平仪",
//            "  水平:${Math.toDegrees(rollAngle.toDouble())}  垂直:${Math.toDegrees(pitchAngle.toDouble())}"
//        )
        return rollAngle to pitchAngle
    }

    private fun getCompassInfo(): Pair<Float, String> {

        fun getDirection(angle: Double): String {
            var direction = ""

            if (angle >= 350 || angle <= 10)
                direction = "北"
            if (angle < 350 && angle > 280)
                direction = "西北"
            if (angle <= 280 && angle > 260)
                direction = "西"
            if (angle <= 260 && angle > 190)
                direction = "西南"
            if (angle <= 190 && angle > 170)
                direction = "南"
            if (angle <= 170 && angle > 100)
                direction = "东南"
            if (angle <= 100 && angle > 80)
                direction = "东"
            if (angle <= 80 && angle > 10)
                direction = "东北"

            return direction
        }

        //orientationAngles[0]  ：azimuth 方向角，但用（磁场+加速度）得到的数据范围是（-180～180）,也就是说，0表示正北，90表示正东，180/-180表示正南，-90表示正西。
        // 而直接通过方向感应器数据范围是（0～359）360/0表示正北，90表示正东，180表示正南，270表示正西。
        val oriDegrees = Math.toDegrees(orientationAngles[0].toDouble())
        var degrees = (oriDegrees + 360) % 360.0
        degrees = round(degrees * 100) / 100

        val direction = getDirection(degrees)
//        Logger.d("罗盘", "原始 角度:$oriDegrees ，修正：$degrees 描述:$direction")

        return degrees.toFloat() to direction
    }

    fun init(ctx: Context) {
        sensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    fun onSensorActive() {
        accSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        // 给传感器注册监听：
        sensorManager?.registerListener(
            sensorEventListener,
            accSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager?.registerListener(
            sensorEventListener,
            magSensor,
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
fun AMSensorCompose(
    getAdjustInfo: ((Triple<Float, Float, Float>) -> Unit)? = null,
    getLevelInfo: ((Pair<Float, Float>) -> Unit)? = null,
    getComPassInfo: ((Pair<Float, String>) -> Unit)? = null,
    getMagnetic: ((Float) -> Unit)? = null,
    getRotationDegree: ((Float) -> Unit)? = null
) {
    val ctx = LocalContext.current

    val sensorHelper = remember {
        AMSensorHelper(
            getAdjustInfo = getAdjustInfo,
            getComPassInfo = getComPassInfo,
            getLevelInfo = getLevelInfo,
            getMagnetic = getMagnetic,
            getRotationDegree = getRotationDegree
        ).apply { init(ctx) }
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