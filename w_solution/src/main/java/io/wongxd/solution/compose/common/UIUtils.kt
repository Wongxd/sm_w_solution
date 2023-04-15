package io.wongxd.solution.compose.common

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import io.wongxd.solution.compose.composeTheme.colorAssets
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt



/**
 * 边界检测，防止小球拖动到屏幕外面
 * @param parentSize  父容器的宽度或者高度
 * @param size        小球的的宽度或者高度
 * @param offset      小球x或者y的偏移量
 */
fun sideDetect(parentSize: Int, size: Int, offset: Float): Float {
    return if (offset <= 0) {
        0f
    } else if (offset >= parentSize - size) {
        (parentSize - size).toFloat()
    } else {
        offset
    }
}

/**
 * 拖动和吸边动画
 *
 *
@Composable
fun SwipeToSideExample(modifier: Modifier = Modifier) {
BoxWithConstraints(modifier = modifier.fillMaxSize()) {
val width: Int = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
val height = with(LocalDensity.current) { maxHeight.toPx() }.toInt()
Box(
modifier = Modifier
.size(45.dp)
.dragToSide(width, height)
.background(color = MaterialTheme.colors.primary, shape = CircleShape)
)
}
}
 *
 *
 */
fun Modifier.dragToSide(width: Int, height: Int): Modifier = composed {
    val animOffset = remember { Animatable(Offset(0f, 0f), Offset.VectorConverter) }
    offset {
        //Log.i("dragToSide", "animOffset = ${animOffset.value.toString()}")
        IntOffset(animOffset.value.x.roundToInt(), animOffset.value.y.roundToInt())
    }.pointerInput(Unit) {
        // 用于计算抛掷衰减。
        val decay = splineBasedDecay<Offset>(this)
        //对触摸事件和 Animatable 使用挂起函数。
        coroutineScope {
            while (true) {
                // 检测touch down 事件.
                val firstDownPointerId = awaitPointerEventScope { awaitFirstDown().id }
                val velocityTracker = VelocityTracker()
                // 停止正在进行的动画
                animOffset.stop()
                //touch down 之后的一系列touch 事件
                awaitPointerEventScope {
                    drag(firstDownPointerId) { change ->
                        //边界检测防止小球移除到屏幕外面
                        val offsetX = sideDetect(
                            width,
                            size.width,
                            animOffset.value.x + change.positionChange().x
                        )
                        val offsetY = sideDetect(
                            height,
                            size.height,
                            animOffset.value.y + change.positionChange().y
                        )
                        //Log.i("hj", "offsetX = $offsetX , offsetY = $offsetY ")
                        //更新动画的坐标
                        launch {
                            animOffset.snapTo(Offset(offsetX, offsetY))
                        }
                        velocityTracker.addPosition(
                            change.uptimeMillis,
                            change.position
                        )
                    }
                }
                //touch事件结束，准备动画
                val velocity = velocityTracker.calculateVelocity()
                val targetOffset = decay.calculateTargetValue(
                    typeConverter = Offset.VectorConverter,
                    initialValue = animOffset.value,
                    initialVelocity = velocity.toOffset()
                )
                // 动画在到达边界时停止
                animOffset.updateBounds(
                    lowerBound = Offset(0f, 0f),
                    upperBound = Offset(
                        (width - size.width).toFloat(),
                        (height - size.height).toFloat()
                    )
                )
                launch {
                    if (targetOffset.x.absoluteValue <= width / 2f) {
                        animOffset.animateTo(targetValue = Offset(0f, targetOffset.y))
                    } else {
                        animOffset.animateTo(
                            targetValue = Offset(
                                (width - size.width).toFloat(),
                                targetOffset.y
                            )
                        )
                    }
                }
            }
        }
    }
}

fun Velocity.toOffset() = Offset(x, y)

/**
 * 增加未读小红点
 */
fun Modifier.unread(hasUnread: Boolean, badgeColor: Color) = this
    .drawWithContent {
        drawContent()
        if (hasUnread) {
            drawCircle(
                color = badgeColor,
                radius = 5.dp.toPx(),
                center = Offset(size.width - 1.dp.toPx(), 1.dp.toPx()),
            )
        }
    }

var noRippleClickableLastClickTime = 0L

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        if (System.currentTimeMillis() - noRippleClickableLastClickTime > 800)
            onClick()
    }
}


//Modifier 的扩展方法处理如下
fun Modifier.debouncedClickable(enabled: Boolean = true, delay: Long = 800, onClick: () -> Unit) =
    composed {
        var clicked by remember {
            mutableStateOf(!enabled)
        }
        LaunchedEffect(key1 = clicked, block = {
            if (clicked) {
                delay(delay)
                clicked = !clicked
            }
        })

        Modifier.clickable(if (enabled) !clicked else false) {
            clicked = !clicked
            onClick()
        }
    }


@Composable
fun TintStatusBar(
    color: Color = MaterialTheme.colorAssets.statusBar,
    aty: Activity? = null,
    darkIcon: Boolean = true,
    insert2StatusBar: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    // 1. 设置状态栏沉浸式
    aty?.let {
        WindowCompat.setDecorFitsSystemWindows(it.window, false)
    }
    // 加入ProvideWindowInsets
    ProvideWindowInsets {
        // 2. 设置状态栏颜色
        rememberSystemUiController().setStatusBarColor(
            Color.Transparent, darkIcons = MaterialTheme.colors.isLight && darkIcon
        )
        Column {
            // 3. 获取状态栏高度并设置占位
            if (!insert2StatusBar)
                Spacer(
                    modifier = Modifier
                        .background(color)
                        .statusBarsHeight()
                        .fillMaxWidth()
                )

            val insets = LocalWindowInsets.current
            // 切记，这些信息都是px单位，使用时要根据需求转换单位
            val statusBarTop = insets.statusBars.top
            if (statusBarTop != 0) //防止布局闪烁
                content()
        }
    }
}
