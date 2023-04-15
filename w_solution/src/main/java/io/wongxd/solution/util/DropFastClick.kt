package io.wongxd.solution.util

import android.view.View

internal abstract class DebounceListener(private val interval: Long) :
    View.OnClickListener {

    private var lastClickTime: Long = 0

    override fun onClick(v: View?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            onClick()
            lastClickTime = currentTime
        }
    }

    protected abstract fun onClick()
}

/**
 * 丢弃[interval]内的相同点击
 */
fun View.setDropFastClick(interval: Long = 800L, logic: () -> Unit) {
    this.setOnClickListener(object : DebounceListener(interval) {
        override fun onClick() {
            logic.invoke()
        }
    })
}