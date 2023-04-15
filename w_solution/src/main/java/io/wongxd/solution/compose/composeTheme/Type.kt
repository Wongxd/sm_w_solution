package io.wongxd.solution.compose.composeTheme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val LightTypography = Typography(
    subtitle1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color.Black.copy(alpha = 0.8f),
    ), subtitle2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.Black.copy(alpha = 0.5f),
    ), body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = Color.Black.copy(alpha = 0.5f),
    ), body2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = Color.Black.copy(alpha = 0.5f),
    ), button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
    ), caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = color99
    )
)

object FontSizeStyle {
    val default = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = TextUnit.Unspecified,
        color = Color.Unspecified
    )

    val normal = TextStyle(fontWeight = FontWeight.Normal)

    val bold = TextStyle(fontWeight = FontWeight.Bold)

    val sp10 = TextStyle(fontSize = 10.sp)

    val sp11 = TextStyle(fontSize = 11.sp)

    val sp12 = TextStyle(fontSize = 12.sp)

    val sp13 = TextStyle(fontSize = 13.sp)

    val sp14 = TextStyle(fontSize = 14.sp)

    val sp15 = TextStyle(fontSize = 15.sp)

    val sp16 = TextStyle(fontSize = 16.sp)

    val sp17 = TextStyle(fontSize = 17.sp)

    val sp18 = TextStyle(fontSize = 18.sp)

    val sp19 = TextStyle(fontSize = 19.sp)

    val sp20 = TextStyle(fontSize = 20.sp)
}

@Composable
fun TextStyle.weight(weight: Int): TextStyle = this.copy(fontWeight = FontWeight(weight))

@Composable
fun TextStyle.bold(): TextStyle = this.copy(fontWeight = FontWeight.Bold)

@Composable
fun TextStyle.normal(): TextStyle = this.copy(fontWeight = FontWeight.Normal)

@Composable
fun TextStyle.size(size: Int): TextStyle = this.copy(fontSize = size.sp)

@Composable
fun TextStyle.black(): TextStyle = this.copy(color = MaterialTheme.colorAssets.black)

@Composable
fun TextStyle.white(): TextStyle = this.copy(color = MaterialTheme.colorAssets.white)

@Composable
fun TextStyle.appGray(): TextStyle = this.copy(color = MaterialTheme.colorAssets.txtGray)

@Composable
fun TextStyle.appGrayLight(): TextStyle = this.copy(color = MaterialTheme.colorAssets.txtGrayLight)

@Composable
fun TextStyle.color(c: Color): TextStyle = this.copy(color = c)

@Composable
fun TextStyle.customDarkColor(c: Color, darkColor: Color): TextStyle =
    this.copy(color = if (isSystemInDarkTheme()) darkColor else c)

@Composable
fun TextStyle.appColor(): TextStyle = this.copy(color = MaterialTheme.colorAssets.appColor)

@Composable
fun TextStyle.appRed(): TextStyle = this.copy(color = MaterialTheme.colorAssets.appRed)

val AppThemeHolder.textStyle
    @Composable @ReadOnlyComposable get() = FontSizeStyle

val MaterialTheme.textStyle
    @Composable @ReadOnlyComposable get() = AppThemeHolder.textStyle