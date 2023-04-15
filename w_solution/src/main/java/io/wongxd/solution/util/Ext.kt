package io.wongxd.solution.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.getDisplay
import java.math.BigDecimal

/**
 * 使用 正则表达式去掉多余的 .与 0
 */
fun String.subZeroAndDot(): String {
    var s = this
    if (s.indexOf(".") > 0) {
        s = s.replace(Regex("0+?$"), "")//去掉多余的0
        s = s.replace(Regex("[.]$"), "")//如最后一位是.则去掉
    }
    return s
}

/**
 * 判断是否是深色
 */
val Context.isInNightMode: Boolean
    get() = (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

fun Activity.setFullScreen() {
    this.requestWindowFeature(Window.FEATURE_NO_TITLE)
    this.requestWindowFeature(Window.FEATURE_SWIPE_TO_DISMISS)
    window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
    )

    hideNavKey()
}

fun Activity.hideNavKey() {
    if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
        val v = window.decorView
        v.systemUiVisibility = View.GONE
    } else if (Build.VERSION.SDK_INT >= 19) {
        //for new api versions.
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions
    }
}

fun Context.getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(this, drawableId)
    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

/**
 * 软键盘显示/隐藏
 */
fun Context.hideShowKeyboard() {
    val imm: InputMethodManager =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager //得到InputMethodManager的实例
    if (imm.isActive) { //如果开启
        imm.toggleSoftInput(
            InputMethodManager.SHOW_IMPLICIT,
            InputMethodManager.HIDE_NOT_ALWAYS
        ) //关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
    }
}

/**
 * 隐藏软键盘(只适用于Activity，不适用于Fragment)
 */
fun Activity.hideSoftKeyboard() {
    val view: View? = this.currentFocus
    if (view != null) {
        val inputMethodManager: InputMethodManager =
            this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            view.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

/**
 * 隐藏软键盘(可用于Activity，Fragment)
 */
fun Context.hideSoftKeyboard(viewList: List<View?>?) {
    if (viewList == null) return
    val inputMethodManager: InputMethodManager =
        this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    for (v in viewList) {
        inputMethodManager.hideSoftInputFromWindow(
            v?.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}