package io.wongxd.solution.compose.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.wongxd.solution.compose.composeTheme.*

@Composable
fun HorizontalGradientBtn(
    des: String,
    width: Int = 666,
    height: Int = 46,
    radius: Int = 23,
    topPadding: Int = 0,
    bottomPadding: Int = 0,
    horizontalPadding: Int = 30,
    fontSize: Int = 16,
    enable: Boolean = true,
    red: Boolean = false,
    content: (@Composable BoxScope.() -> Unit)? = null,
    onClick: () -> Unit
) {

    val shape = RoundedCornerShape(radius.dp)

    Box(
        modifier = Modifier
            .padding(top = topPadding.dp, bottom = bottomPadding.dp)
            .padding(horizontal = horizontalPadding.dp)
            .width(width.dp)
            .height(height.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        if (red) Color(0xFFEC4E34)
                        else if (enable) Color(0xFF5961FF)
                        else Color(0x1F868686),
                        if (red) Color(0xFFEF8473)
                        else if (enable) Color(0xFFFBA39F)
                        else Color(0x1F868686)
                    )
                ), shape
            )
            .clip(shape)
            .debouncedClickable(enable) { onClick.invoke() },
        contentAlignment = Alignment.Center
    ) {

        if (content != null) {
            content()
        } else
            Text(
                text = des,
                style = MaterialTheme.textStyle.default.size(fontSize).color(c = Color.White)
            )

    }
}

@Composable
fun VerticalGradientBtn(
    des: String,
    width: Int = 666,
    height: Int = 46,
    radius: Int = 23,
    topPadding: Int = 0,
    bottomPadding: Int = 0,
    horizontalPadding: Int = 30,
    fontSize: Int = 16,
    enable: Boolean = true,
    red: Boolean = false,
    content: (@Composable BoxScope.() -> Unit)? = null,
    onClick: () -> Unit
) {

    val shape = RoundedCornerShape(radius.dp)

    Box(
        modifier = Modifier
            .padding(top = topPadding.dp, bottom = bottomPadding.dp)
            .padding(horizontal = horizontalPadding.dp)
            .width(width.dp)
            .height(height.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (red) Color(0xFFEC4E34)
                        else if (enable) Color(0xFF0487FF)
                        else Color(0x1F868686), if (red) Color(0xFFEF8473)
                        else if (enable) Color(0xFF66B6FF)
                        else Color(0x1F868686)
                    )
                ), shape
            )
            .clip(shape)
            .debouncedClickable(enable) { onClick.invoke() },
        contentAlignment = Alignment.Center
    ) {

        if (content != null) {
            content()
        } else
            Text(
                text = des,
                style = MaterialTheme.textStyle.default.size(fontSize).color(c = Color.White)
            )

    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
fun VerticalGradientBtnPreView() {

    HorizontalGradientBtn(des = "HorizontalGradientBtn", horizontalPadding = 50) {

    }

    VerticalGradientBtn(des = "VerticalGradientBtn", horizontalPadding = 50) {

    }
}

@Composable
fun StrokeBtn(
    des: String,
    width: Int = 666,
    height: Int = 46,
    radius: Int = 23,
    topPadding: Int = 0,
    bottomPadding: Int = 0,
    horizontalPadding: Int = 30,
    fontSize: Int = 16,
    enable: Boolean = true,
    onClick: () -> Unit
) {

    val shape = RoundedCornerShape(radius.dp)

    Box(
        modifier = Modifier
            .padding(top = topPadding.dp, bottom = bottomPadding.dp)
            .padding(horizontal = horizontalPadding.dp)
            .width(width.dp)
            .height(height.dp)
            .border(
                1.dp,
                if (enable) MaterialTheme.colorAssets.appColor else Color(0x1F868686),
                shape
            )
            .padding(1.dp)
            .clip(shape)
            .debouncedClickable(enable) { onClick.invoke() },
        contentAlignment = Alignment.Center
    ) {

        Text(text = des, style = MaterialTheme.textStyle.default.size(fontSize).appColor())

    }
}

@Composable
fun SolidBtn(
    des: String,
    width: Int = 666,
    height: Int = 46,
    radius: Int = 23,
    bgColor: Color = AppThemeHolder.colorAssets.appColor,
    topPadding: Int = 0,
    bottomPadding: Int = 0,
    horizontalPadding: Int = 30,
    fontSize: Int = 16,
    enable: Boolean = true,
    onClick: () -> Unit
) {

    val shape = RoundedCornerShape(radius.dp)

    Box(
        modifier = Modifier
            .padding(top = topPadding.dp, bottom = bottomPadding.dp)
            .padding(horizontal = horizontalPadding.dp)
            .width(width.dp)
            .height(height.dp)
            .padding(1.dp)
            .clip(shape)
            .background(bgColor)
            .debouncedClickable(enable) { onClick.invoke() },
        contentAlignment = Alignment.Center
    ) {

        Text(text = des, style = MaterialTheme.textStyle.default.size(fontSize).white())

    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
fun StrokeBtnPreView() {

    StrokeBtn(des = "StrokeBtn", horizontalPadding = 50, topPadding = 16) {

    }
}