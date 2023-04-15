package io.wongxd.solution.util.ext

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import androidx.core.view.ViewCompat
import io.wongxd.solution.util.AppManager
import java.math.BigDecimal


fun Activity.setCustomDensity(designWidthDp: Float = 375f) {
    val activityDisplayMetrics = resources.displayMetrics
    val targetDensity = 1.0f * activityDisplayMetrics.widthPixels / designWidthDp
    activityDisplayMetrics.density = targetDensity
}

fun View.getScreenSize(): Point {
    val point = Point()
    ViewCompat.getDisplay(this)?.getRealSize(point)
    return point
}

fun Activity.getScreenSize(): Point {
    val resources: Resources = this.resources
    val dm: DisplayMetrics = resources.displayMetrics
    val screenWidth = dm.widthPixels
    val screenHeight = dm.heightPixels
    return Point(screenWidth, screenHeight)
}

fun Context.getScreenSize(): Point {
    val resources: Resources = this.resources
    val dm: DisplayMetrics = resources.displayMetrics
    val screenWidth = dm.widthPixels
    val screenHeight = dm.heightPixels
    return Point(screenWidth, screenHeight)
}


inline val Double.dp: Int
    get() = run {
        return toFloat().dp
    }

inline val Int.dp: Int
    get() = run {
        return toFloat().dp
    }

inline val Float.dp: Int
    get() = run {
        val aty = AppManager.getCurrentActivity() ?: return@run 0
        val scale: Float = aty.resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

fun Context.dp(f: Float): Float {
    val scale: Float = this.resources.displayMetrics.density
    return (f * scale + 0.5f)
}

//px转毫米
fun Int.pxWidthToMm(): Float {
    val inch = this / getDPI()
    return (inch * 25.4f)
}

//px转毫米
fun Int.pxHeightToMm(): Float {
    val inch = this / getDPI()
    return (inch * 25.4f)
}

//毫米转px
fun Int.mmToPx(): Float {
    val inch = this / 25.4f
    return (inch * getDPI())
}

/**
 * 获取DPI，图像每英寸长度内的像素点数
 * DPI = 宽 / ((尺寸2 × 宽2) / (宽2 + 高2))1/2 = 长 / ((尺寸2 × 高2) / (宽2 + 高2))1/2
 * @return
 */
fun getDPI(): Float {
    val aty = AppManager.getCurrentActivity() ?: return 0f
    //获取屏幕尺寸
    val screenSize: Double = aty.getScreenInch()
    //获取宽高大小
    val widthPx: Int = aty.getResources().getDisplayMetrics().widthPixels
    val heightPx: Int = aty.getResources().getDisplayMetrics().heightPixels
    return (widthPx / Math.sqrt(screenSize * screenSize * widthPx * widthPx / (widthPx * widthPx + heightPx * heightPx))).toFloat()
}

private var mInch = 0.0
fun Activity.getScreenInch(): Double {
    if (mInch != 0.0) {
        return mInch
    }

    try {
        var realWidth = 0
        var realHeight = 0
        val display = getWindowManager().getDefaultDisplay()
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        if (Build.VERSION.SDK_INT >= 17) {
            val size = Point()
            display.getRealSize(size)
            realWidth = size.x
            realHeight = size.y
        }

        mInch =
            Math.sqrt((realWidth.toDouble() / metrics.xdpi) * (realWidth / metrics.xdpi) + (realHeight / metrics.ydpi) * (realHeight / metrics.ydpi))
                .formatDouble(1)

    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }

    return mInch
}

/**
 * Double类型保留指定位数的小数，返回double类型（四舍五入）
 * newScale 为指定的位数
 */
fun Double.formatDouble(newScale: Int): Double {
    val bd = BigDecimal(this)
    return bd.setScale(newScale, BigDecimal.ROUND_HALF_UP).toDouble()
}
