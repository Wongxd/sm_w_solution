package io.wongxd.solution.util.ext


import android.content.Context
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.luminance

inline val Context.isDarkTheme: Boolean
    get() = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES

@ColorInt
fun Int.getThemeAdapterColor(context: Context): Int {
    return if (context.isDarkTheme) this.getDarkThemeColor() else this
}

@ColorInt
fun Int.getDarkThemeColor(): Int {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this, hsv)//转化hsv
    //转化结果
    val mHue = hsv[0]//色调范围0-360
    val mSat = hsv[1]//饱和度范围0-1
    var mVal = hsv[2]//亮度范围0-1

    //调整大小
    mVal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        1.1f - this.luminance
    } else {
        1.1f - mVal
    }

    if (mVal > 1) mVal = .98976f
    if (mVal < 0) mVal = .01024f

    return android.graphics.Color.HSVToColor(floatArrayOf(mHue, mSat, mVal))
}
