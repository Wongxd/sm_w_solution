package io.wongxd.solution.compose.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.wongxd.solution.compose.custom.ThemeAdapterImage


@Preview
@Composable
fun WTopBar(
    title: String = "标题",
    leftRes: Int = 0,
    leftClick: () -> Unit = {},
    rightStartRes: Int = 0,
    rightStr: String = "",
    rightStrColor: Color = Color.Transparent,
    rightRes: Int = 0,
    rightClick: () -> Unit = {},
    tintWhite: Boolean = false
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f)) {
            if (leftRes != 0)
                IconButton(onClick = { leftClick.invoke() }) {
                    ThemeAdapterImage(
                        painter = painterResource(id = leftRes),
                        contentDescription = "left",
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .align(Alignment.CenterStart)
                            .size(22.dp)
                    )
                }
        }

        Text(text = title, color = if (tintWhite) Color.White else Color.Black, fontSize = 18.sp)

        Box(Modifier.weight(1f)) {
            Row(
                Modifier
                    .padding(end = 20.dp)
                    .align(Alignment.CenterEnd)
                    .debouncedClickable { rightClick.invoke() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (rightStartRes != 0)
                    ThemeAdapterImage(
                        painter = painterResource(id = rightStartRes),
                        contentDescription = "rightStart",
                        contentScale = ContentScale.Inside,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(end = 2.5.dp)
                    )
                if (rightStr.isNotBlank())
                    Text(
                        text = rightStr,
                        color = when {
                            rightStrColor != Color.Transparent -> rightStrColor
                            tintWhite -> Color.White
                            else -> Color.Black
                        },
                        fontSize = 14.sp,
                        modifier = Modifier
                    )
                if (rightRes != 0)
                    ThemeAdapterImage(
                        painter = painterResource(id = rightRes),
                        contentDescription = "right",
                        contentScale = ContentScale.Inside,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(start = 2.5.dp)
                    )
            }
        }
    }
}