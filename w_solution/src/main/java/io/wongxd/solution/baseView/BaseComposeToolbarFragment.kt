package io.wongxd.solution.baseView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.wongxd.solution.compose.common.debouncedClickable
import io.wongxd.solution.compose.composeTheme.AppTheme
import io.wongxd.solution.compose.composeTheme.AppThemeHolder
import io.wongxd.solution.compose.composeTheme.colorAssets
import io.wongxd.solution.compose.composeTheme.textStyle
import io.wongxd.solution.compose.custom.ThemeAdapterImage

abstract class BaseComposeToolBarFgt : BaseComposeFragment() {

    open val title: String = ""

    open val centerTitle: Boolean = true

    open val showBack: Boolean = true

    open val blackTint: Boolean = true

    open val rightTxt: String = ""

    open val showRightImg: Boolean = false

    open val rightRes: Int = 0

    override val needSetStatusBarByBase: Boolean
        get() = true

    @Composable
    open fun getRightColor(): Color {
        return MaterialTheme.colorAssets.appColor
    }

    open val rightClick: () -> Unit = {}

    protected val rightCanClickState by lazy { mutableStateOf(false) }

    @Composable
    override fun SetComposeRoot() {

        statusBarColorCompose = AppThemeHolder.colorAssets.appBg

        AppTheme {
            AvoidStatusBar {
                Column(Modifier.fillMaxSize()) {

                    ComposeToolBar(
                        showBack = showBack,
                        blackTint = blackTint,
                        leftClick = {
                            if (!onBackPressedSupport()) atySupportBack()
                        },
                        title, centerTitle, rightTxt, rightCanClickState,
                        @Composable {
                            getRightColor()
                        }, rightClick, showRightImg, rightRes
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        Render()
                    }

                }
            }

            FullScreenBottomSheet()
        }
    }

    @Composable
    open fun FullScreenBottomSheet() {

    }


}

@Composable
fun ComposeToolBar(
    showBack: Boolean = true,
    blackTint: Boolean = false,
    leftClick: () -> Unit,
    title: String,
    centerTitle: Boolean = true,
    rightTxt: String = "",
    rightCanClickState: MutableState<Boolean> = mutableStateOf(false),
    getRightColor: @Composable () -> Color = { Color.Transparent },
    rightClick: () -> Unit = {},
    showRightImg: Boolean = false,
    rightRes: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .height(48.dp)
            .fillMaxWidth(),
    ) {
        if (showBack) ThemeAdapterImage(
            painter = painterResource(
                id = if (blackTint)
                    io.wongxd.solution.R.drawable.svg_return_black
                else io.wongxd.solution.R.drawable.svg_return_white
            ),
            contentDescription = null,
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 15.dp)
                .size(34.dp)
                .padding(5.dp)
                .clip(CircleShape)
                .debouncedClickable {
                    leftClick.invoke()
                }
        )

        Text(
            text = title,
            style = MaterialTheme.textStyle.sp18.copy(color = if (blackTint) MaterialTheme.colorAssets.black else MaterialTheme.colorAssets.white),
            modifier = Modifier
                .padding(start = if (centerTitle) 0.dp else 49.dp)
                .align(if (centerTitle) Alignment.Center else Alignment.CenterStart)
        )

        if (rightTxt.isNotBlank() && rightCanClickState.value) {
            Text(text = "   $rightTxt   ",
                style = MaterialTheme.textStyle.sp18.copy(
                    color = if (rightCanClickState.value) getRightColor()
                    else if (blackTint) MaterialTheme.colorAssets.black else MaterialTheme.colorAssets.white
                ),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .debouncedClickable {
                        rightClick.invoke()
                    })
        }


        if (showRightImg) ThemeAdapterImage(
            painter = painterResource(rightRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 15.dp)
                .size(24.dp)
                .clip(CircleShape)
                .debouncedClickable {
                    rightClick.invoke()
                },
            contentScale = ContentScale.Inside
        )

    }
}