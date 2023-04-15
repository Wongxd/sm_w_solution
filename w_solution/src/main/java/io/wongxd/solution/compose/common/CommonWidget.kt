package io.wongxd.solution.compose.common

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import io.wongxd.solution.compose.composeTheme.black
import io.wongxd.solution.compose.composeTheme.colorAssets
import io.wongxd.solution.compose.composeTheme.size
import io.wongxd.solution.compose.composeTheme.textStyle
import io.wongxd.solution.compose.custom.LoadingAnimation
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SetSystemBarsColor(
    key: Any = Unit,
    statusBarColor: Color = MaterialTheme.colors.background,
    navigationBarColor: Color = MaterialTheme.colors.background
) {
    val systemUiController = rememberSystemUiController()
    val isLight = MaterialTheme.colors.isLight
    DisposableEffect(key1 = key) {
        systemUiController.setStatusBarColor(
            color = statusBarColor, darkIcons = isLight
        )
        systemUiController.setNavigationBarColor(
            color = navigationBarColor, darkIcons = isLight
        )
        systemUiController.systemBarsDarkContentEnabled = isLight
        onDispose {

        }
    }
}

@Composable
fun CommonDivider(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier, thickness = 0.3.dp,
    )
}

@SuppressLint("ModifierParameter")
@Composable
fun CommonButton(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 10.dp),
    text: String,
    onClick: () -> Unit
) {
    Button(modifier = modifier, onClick = {
        onClick()
    }) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1,
        )
    }
}

@Composable
fun BoxScope.CircularProgress(
    alignment: Alignment = Alignment.Center,
    size: Float = 28f,
    strokeWidth: Float = 4.4f
) {
//    Surface(
//        elevation = 10.dp,
//        shape = CircleShape,
//        modifier = Modifier.align(alignment)
//    ) {
//        CircularProgressIndicator(
//            modifier = Modifier.size(size.dp),
//            color = MaterialTheme.colorAssets.appColor,
//            strokeWidth = strokeWidth.dp
//        )
//    }

    CircularProgressIndicator(
        modifier = Modifier
            .align(alignment)
            .size(size.dp),
        color = MaterialTheme.colorAssets.appColor,
        strokeWidth = strokeWidth.dp
    )
}

@Composable
fun BoxScope.LoadingAnimation(alignment: Alignment = Alignment.Center) {
    LoadingAnimation(modifier = Modifier.align(alignment), circleSize = 6.dp, spaceBetween = 3.dp)
}

@Preview
@Composable
fun LoadingProgressIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgress()
    }
}

@Preview
@Composable
fun LoadingPage() {
    Box(Modifier.fillMaxSize()) {
        CircularProgress()
    }
}

@Preview
@Composable
fun ErrorPage(msg: String = "预料之外的情况", click: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .noRippleClickable(click), contentAlignment = Alignment.Center
    ) {
        Text(text = msg, color = MaterialTheme.colors.onBackground, fontSize = 14.sp)
    }
}

@Preview
@Composable
fun EmptyPage(msg: String = "这里空空的", click: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .noRippleClickable { click.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = msg, color = MaterialTheme.colors.onBackground, fontSize = 14.sp)
    }
}

@Preview
@Composable
fun EmptyPart(msg: String = "这里空空的", topPaddingDp: Int = 10, click: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .padding(top = topPaddingDp.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = msg, color = MaterialTheme.colors.onBackground, fontSize = 14.sp)
    }
}

@Composable
fun SwipeLoadFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(MaterialTheme.colors.background), contentAlignment = Alignment.Center
    ) {
        Text(text = "--上拉加载更多--", color = MaterialTheme.colors.onBackground, fontSize = 12.sp)
    }
}

@Composable
fun LoadingFooter() {
    Box(
        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
    ) {

        CircularProgress()
    }
}

@Composable
fun NoMoreFooter(bgColor: Color = MaterialTheme.colors.background) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "--我是有底线的--", color = MaterialTheme.colors.onBackground, fontSize = 12.sp)
    }
}

fun Modifier.scrim(colors: List<Color>): Modifier = drawWithContent {
    drawContent()
    drawRect(Brush.verticalGradient(colors))
}

@Composable
fun InputEditText(
    modifier: Modifier = Modifier,
    value: String?,
    onValueChange: (String) -> Unit,
    fontSize: Int = 17,
    contentTextStyle: TextStyle? = null,
    placeHolderString: String = "",
    hintTextStyle: TextStyle? = null,
    textCenter: Boolean = false,
    textEnd: Boolean = false,
    pwd: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    maxLength: Int = Int.MAX_VALUE,
    offsetDp: Dp = 0.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    cursorColor: Color = MaterialTheme.colorAssets.black,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {


    val finalContentTextStyle = contentTextStyle ?: MaterialTheme.textStyle.default.black()
        .size(fontSize)
        .copy(textAlign = if (textCenter) TextAlign.Center else null)
        .copy(textAlign = if (textEnd) TextAlign.End else null)
    val finalHintTextStyle = hintTextStyle ?: MaterialTheme.textStyle.default
        .size(fontSize)
        .copy(
            color = MaterialTheme.colorAssets.txtGray,
            textAlign = if (textCenter) TextAlign.Center else null
        )

    val hintTextAlignment = if (textCenter) {
        Alignment.Center
    } else if (textEnd) {
        Alignment.CenterEnd
    } else {
        Alignment.CenterStart
    }

    BasicTextField(
        value = value ?: "",
        onValueChange = { str ->
            if (str.length <= maxLength) {
                if (keyboardType in arrayOf(KeyboardType.Phone)) {
                    if (str.isDigitsOnly()) onValueChange.invoke(str)
                } else {
                    onValueChange.invoke(str)
                }
            }
        },
        modifier = modifier,
        textStyle = finalContentTextStyle,
        decorationBox = { innerTextField ->
            Box(
                modifier = modifier
                    .offset(x = offsetDp),
                contentAlignment = hintTextAlignment,
            ) {
                if (value.isNullOrBlank()) {
                    Text(
                        text = placeHolderString,
                        color = finalHintTextStyle.color,
                        fontSize = finalHintTextStyle.fontSize
                    )
                }
                innerTextField()
            }
        },
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = if (pwd && keyboardType == KeyboardType.Text) KeyboardType.Password else keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(cursorColor),
        visualTransformation = if (pwd) PasswordVisualTransformation() else visualTransformation,
    )
}