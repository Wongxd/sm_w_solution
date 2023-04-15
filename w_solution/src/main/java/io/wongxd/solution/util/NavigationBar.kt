package io.wongxd.solution.util

import android.app.Activity
import android.app.Service
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

private const val NAVIGATION = "navigationBarBackground"

// 该方法需要在View完全被绘制出来之后调用，否则判断不了 //在比如 onWindowFocusChanged（）方法中可以得到正确的结果
fun Activity.isNavigationBarExist(): Boolean {
    val vp = this.window.decorView as ViewGroup
    if (vp != null) {
        for (i in 0 until vp.childCount) {
            vp.getChildAt(i).context.packageName
            if (vp.getChildAt(i).id != View.NO_ID && NAVIGATION ==
                this.resources.getResourceEntryName(vp.getChildAt(i).id)
            ) {
                return true
            }
        }
    }
    return false
}

fun Context.isNavigationBarHasShown(): Boolean {
    val brand = Build.BRAND
    var deviceInfo: String? = null
    if (brand.equals("HUAWEI", ignoreCase = true)) {
        deviceInfo = "navigationbar_is_min"
    } else if (brand.equals("XIAOMI", ignoreCase = true)) {
        deviceInfo = "force_fsg_nav_bar"
    }
    if (!TextUtils.isEmpty(deviceInfo)) {
        val navigationBarIsMin = Settings.Global.getInt(contentResolver, deviceInfo, 0)
        return navigationBarIsMin != 1
    }
    val windowService = getSystemService(Service.WINDOW_SERVICE)
    return if (windowService == null) {
        false
    } else {
        val windowManager = windowService as WindowManager
        val display = windowManager.defaultDisplay
        val realDisplayMetrics = DisplayMetrics()
        display.getRealMetrics(realDisplayMetrics)
        val realHeight = realDisplayMetrics.heightPixels
        val realWidth = realDisplayMetrics.widthPixels
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        val displayHeight = displayMetrics.heightPixels
        val displayWidth = displayMetrics.widthPixels
        if (displayHeight > displayWidth) {
            if (displayHeight + getNavigateHeight() > realHeight) {
                return false
            }
        } else if (displayWidth + getNavigateHeight() > realWidth) {
            return false
        }
        realWidth - displayWidth > 0 || realHeight - displayHeight > 0
    }
}

fun Context.getNavigateHeight(): Int {
    try {
        val resources = this.resources
        val resId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resId > 0) return resources.getDimensionPixelSize(resId)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0
}