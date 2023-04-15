package io.wongxd.solution.compose.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import io.wongxd.solution.compose.composeTheme.colorAssets

//    VerificationCodeField(6){text,focused->
//        SimpleVerificationCodeItem(text,focused)
//    }

/**
 * @param text 文本内容
 * @param focused 是否高亮当前输入框
 */
@Composable
fun RowScope.SimpleVerificationCodeItem(text: String, focused: Boolean) {
    val borderColor = if (focused) Color.Gray else Color(0xeeeeeeee)

    Box(
        modifier = Modifier
            .border(1.dp, borderColor)
            .size(55.dp, 55.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }

}

/**
 * 登录页圆形输入框
 */
@Composable
fun RowScope.FBVerificationCodeItem(text: String, focused: Boolean) {
    val borderColor =
        if (focused) MaterialTheme.colorAssets.appColor else MaterialTheme.colorAssets.white
    val bgColor =
        if (text.isNotBlank()) MaterialTheme.colorAssets.appColor else MaterialTheme.colorAssets.white

    val shape = CircleShape

    Box(
        modifier = Modifier
            .border(2.dp, borderColor, shape)
            .size(62.dp)
            .clip(shape)
            .background(bgColor), contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (text.isNotBlank()) MaterialTheme.colorAssets.white else MaterialTheme.colorAssets.black,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }

}

/**
 * 锁屏密码输入框
 */
@Composable
fun RowScope.FBLockPwdCodeItem(index: Int, text: String, focused: Boolean) {
    val bgColor = if (text.isNotBlank()) MaterialTheme.colorAssets.appColor else Color.Transparent

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .weight(1f)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(10.dp)
                .clip(CircleShape)
                .background(bgColor)
        )

        if (index != 5)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(vertical = 10.dp)
                    .width(1.5.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorAssets.appDivider)
            )
    }

}

/**
 * @param digits 验证码位数（框数量）
 * @param horizontalMargin 水平间距
 * @param inputCallback 输入回调
 */
@Composable
fun VerificationCodeField(
    digits: Int,
    horizontalMargin: Int = 10,
    focusRequester: FocusRequester = FocusRequester(),
    inputCallback: (content: String) -> Unit,
    itemScopeWithIndex: @Composable RowScope.(index: Int, text: String, focused: Boolean) -> Unit = { index, txt, focused -> },
    itemScope: @Composable RowScope.(text: String, focused: Boolean) -> Unit
) {
    var content by remember { mutableStateOf("") }
    Box {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //绘制框
            repeat(digits) { index ->
                if (index != 0) {
                    //添加间距
                    Spacer(modifier = Modifier.width(horizontalMargin.dp))
                }
                //获取当前框的文本
                val text = content.getOrNull(index)?.toString() ?: ""
                //是否正在输入的框
                val focused = index == content.length
                //绘制文本
                itemScope(text, focused)

                itemScopeWithIndex(index, text, focused)
            }

        }
        BasicTextField(
            value = content, onValueChange = {
                if (it.length <= digits && it.isDigitsOnly()) {
                    content = it
                    inputCallback(it)
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .drawWithContent { }//清除绘制内容
                .matchParentSize()
                .focusRequester(focusRequester)
        )//填充至父布局大小
    }

}
