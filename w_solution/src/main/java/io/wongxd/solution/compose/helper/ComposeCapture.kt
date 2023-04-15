package io.wongxd.solution.compose.helper

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.FrameLayout
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Compose 对包裹控件截图
 *
 *
 *
@Compose
fun TestCapture(){
//截图控制器
val captureController = rememberCaptureController()
Column(horizontalAlignment = Alignment.CenterHorizontally) {
ComposeCapture(
captureController = captureController,
onSaveBitmap = {
//Todo 返回截图的 bitmap
}) {
Column(
horizontalAlignment = Alignment.CenterHorizontally
) {
Image(
painter = painterResource(id = R.drawable.wechat),
contentDescription = null
)
Text(text = "测试截图bitmap")
Text(text = "测试截图bitmap")
Text(text = "测试截图bitmap")
Text(text = "测试截图bitmap")
}
}
Button(
onClick = {
//触发截图
captureController.capture()
}
) {
Text(text = "save")
}
}
}

 *
 *
 */

data class CaptureState(
    val capture: Boolean = false
)

fun MutableState<CaptureState>.capture() {
    this.value = this.value.copy(capture = true)
}

private fun MutableState<CaptureState>.captureComplete() {
    this.value = this.value.copy(capture = false)
}

@Composable
fun rememberCaptureController(): MutableState<CaptureState> {
    return remember {
        mutableStateOf(CaptureState(capture = false))
    }
}

@Composable
fun ComposeCapture(
    captureController: MutableState<CaptureState> = rememberCaptureController(),
    onSaveBitmap: (Bitmap?) -> Unit,
    content: @Composable () -> Unit
) {
    val bounds = remember {
        mutableStateOf<Rect?>(null)
    }
    //依据状态值 选择是否使用AndroidView进行展示获取截图
    if (captureController.value.capture) {
        CaptureView(
            captureController = captureController,
            onSaveBitmap = onSaveBitmap,
            bounds = bounds,
            content = content
        )
    } else {
        Surface(modifier = Modifier.onGloballyPositioned {
            bounds.value = it.boundsInRoot()
        }, color = Color.Transparent, content = content)

    }
}

@Composable
private fun CaptureView(
    captureController: MutableState<CaptureState>,
    bounds: MutableState<Rect?>,
    onSaveBitmap: ((Bitmap?) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    AndroidView(factory = {
        FrameLayout(it).apply {
            layoutParams = ViewGroup.LayoutParams(
                (bounds.value!!.right - bounds.value!!.left).toInt(),
                (bounds.value!!.bottom - bounds.value!!.top).toInt()
            )
            val composeView = ComposeView(it).apply {
                setContent {
                    content()
                }
            }
            drawListener(composeView, this, captureController, onSaveBitmap)
            addView(
                composeView, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

        }
    })
}

private fun drawListener(
    composeView: View, viewGroup: ViewGroup,
    captureController: MutableState<CaptureState>,
    onSaveBitmap: ((Bitmap?) -> Unit)? = null,
) {
    val drawListener = object : ViewTreeObserver.OnDrawListener {
        var remove = false
        override fun onDraw() {
            if (composeView.width > 0) {
                if (!remove) {
                    // View 绘制第一帧 开始截图并移除 监听，随后切换截图状态 回到Compose组件
                    remove = true
                    composeView.post {
                        val bitmap = getViewGroupBitmap(viewGroup)
                        // 切换状态 回到Compose
                        captureController.captureComplete()
                        onSaveBitmap?.invoke(bitmap)
                        composeView.viewTreeObserver.removeOnDrawListener(this)
                    }
                }

            }
        }
    }
    composeView.viewTreeObserver.addOnDrawListener(drawListener)
}

/**
 * @param viewGroup viewGroup
 * @return Bitmap
 */
private fun getViewGroupBitmap(viewGroup: ViewGroup): Bitmap? {
    return viewGroup.toBitmap()
}

// start of extension.
fun View.toBitmap(
    onBitmapReady: (Bitmap) -> Unit = {},
    onBitmapError: (Exception) -> Unit = {}
): Bitmap? {

    var temporalBitmap: Bitmap? = null

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            temporalBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)

            // Above Android O, use PixelCopy due
            // https://stackoverflow.com/questions/58314397/
            val window: Window = (this.context as Activity).window

            val location = IntArray(2)

            this.getLocationInWindow(location)

            val viewRectangle = android.graphics.Rect(
                location[0],
                location[1], location[0] + this.width, location[1] + this.height
            )

            val onPixelCopyListener: PixelCopy.OnPixelCopyFinishedListener =
                PixelCopy.OnPixelCopyFinishedListener { copyResult ->

                    if (copyResult == PixelCopy.SUCCESS) {
                        onBitmapReady(temporalBitmap!!)
                    } else {
                        error("Error while copying pixels, copy result: $copyResult")
                    }
                }

            PixelCopy.request(
                window, viewRectangle, temporalBitmap, onPixelCopyListener, Handler(
                    Looper.getMainLooper()
                )
            )
        } else {

            temporalBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.RGB_565)

            val canvas = android.graphics.Canvas(temporalBitmap)

            this.draw(canvas)

            canvas.setBitmap(null)

            onBitmapReady(temporalBitmap!!)
            return temporalBitmap
        }

    } catch (exception: Exception) {
        onBitmapError(exception)
    }

    return temporalBitmap
}
