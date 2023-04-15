package io.wongxd.solution.compose.composeTheme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

val colorF5 = Color(0xfff5f5f5).convert(ColorSpaces.CieXyz)
val colorAD = Color(0xffADADAD).convert(ColorSpaces.CieXyz)
val color8F = Color(0xff8f8f8f).convert(ColorSpaces.CieXyz)
val color32 = Color(0xff323232).convert(ColorSpaces.CieXyz)
val color33 = Color(0xff333333).convert(ColorSpaces.CieXyz)
val color3B = Color(0xff3B3B3B).convert(ColorSpaces.CieXyz)
val color4D = Color(0xff4D4D4D).convert(ColorSpaces.CieXyz)
val colorB3 = Color(0xffb3b3b3).convert(ColorSpaces.CieXyz)
val color61 = Color(0xff616161).convert(ColorSpaces.CieXyz)
val color66 = Color(0xff666666).convert(ColorSpaces.CieXyz)
val color70 = Color(0xff707070).convert(ColorSpaces.CieXyz)
val color90 = Color(0xff909090).convert(ColorSpaces.CieXyz)
val color99 = Color(0xff999999).convert(ColorSpaces.CieXyz)
val color9A = Color(0xff9A9A9A).convert(ColorSpaces.CieXyz)
val colorC6 = Color(0xffC6C6C6).convert(ColorSpaces.CieXyz)
val colorD6 = Color(0xffd6d6d6).convert(ColorSpaces.CieXyz)
val colorE0 = Color(0xffe0e0e0).convert(ColorSpaces.CieXyz)
val colorEB = Color(0xffEBEBEB).convert(ColorSpaces.CieXyz)
val colorFA = Color(0xfffafafa).convert(ColorSpaces.CieXyz)

val PrimaryColorLight = Color(0xFF9E71E3)//按钮背景色
val PrimaryVariantColorLight = Color(0xFFFFFFFF)//标题栏、导航栏背景色
val BackgroundColorLight = Color(0xFFFFFFFF)
val OnBackgroundColorLight = Color(0xFF999999)
val SurfaceColorLight = Color(0xFFFFFFFF)//图标着色
val DivideColorLight = Color(0xE6AEAEAE)//分割线颜色

interface ColorAsset {
    val bgBrush: Brush
        get() = Brush.verticalGradient(listOf(Color(0xffE0EAFF), Color.White))
    val appColor2RedBrush: Brush
        get() = Brush.horizontalGradient(listOf(Color(0xff5961FF), Color(0xffFFA59C)))
    val appBg: Color
        get() = Color(0xffFCF5FF)
    val white: Color
        get() = Color.White
    val black: Color
        get() = Color.Black
    val statusBar: Color
        get() = Color.White
    val appColor: Color
        get() = Color(0XFFA16CFF)
    val appYellow: Color
        get() = Color(0XFFFF9D3B)
    val appOrange: Color
        get() = Color(0XFFFF8643)
    val appRed: Color
        get() = Color(0xFFEF5A42)
    val appPartColor: Color
        get() = Color(0xFFF8F8F8)
    val appDivider: Color
        get() = Color(0x1FADB2BA)
    val appLoginBg: Color
        get() = Color(0xFFF7F9FC)
    val appLoginDivider: Color
        get() = Color(0x33ADB2BA)
    val transBlack: Color
        get() = Color(0X85000000)
    val transWhite: Color
        get() = Color(0X85ffffff)
    val bgWhite
        get() = Color(0xFFFFFFFf)
    val bgGray
        get() = Color(0xFFF9F9F9)
    val txtGray: Color
        get() = Color(0xFF7F7F7F)
    val txtGrayLight: Color
        get() = Color(0xFF868686)
    val searchBarBg
        get() = Color(0x17868686)
}

object LightColorAsset : ColorAsset

object DarkColorAsset : ColorAsset {
    override val bgBrush: Brush
        get() = Brush.verticalGradient(
            listOf(
                Color(0xffE0EAFF).getDarkThemeColor(),
                Color.White.getDarkThemeColor()
            )
        )
    override val appColor2RedBrush: Brush
        get() = Brush.horizontalGradient(
            listOf(
                Color(0xff5961FF).getDarkThemeColor(),
                Color(0xffFFA59C).getDarkThemeColor()
            )
        )
    override val appBg: Color
        get() = super.appBg.getDarkThemeColor()
    override val white: Color
        get() = super.white.getDarkThemeColor()
    override val black: Color
        get() = super.black.getDarkThemeColor()
    override val appColor: Color
        get() = super.appColor.getDarkThemeColor()
    override val appYellow: Color
        get() = super.appYellow.getDarkThemeColor()
    override val appOrange: Color
        get() = super.appOrange.getDarkThemeColor()
    override val appRed: Color
        get() = super.appRed.getDarkThemeColor()
    override val appPartColor: Color
        get() = super.appPartColor.getDarkThemeColor()
    override val appDivider: Color
        get() = super.appDivider.getDarkThemeColor()
    override val appLoginBg: Color
        get() = super.appLoginBg.getDarkThemeColor()
    override val appLoginDivider: Color
        get() = super.appLoginDivider.getDarkThemeColor()
    override val bgWhite: Color
        get() = super.bgWhite.getDarkThemeColor()
    override val bgGray: Color
        get() = super.bgGray.getDarkThemeColor()
    override val txtGray: Color
        get() = super.txtGray.getDarkThemeColor()
    override val txtGrayLight: Color
        get() = super.txtGrayLight.getDarkThemeColor()
    override val searchBarBg: Color
        get() = super.searchBarBg.getDarkThemeColor()
}

@Composable
fun Color.themeAdapter(): Color = if (isSystemInDarkTheme()) this.getDarkThemeColor() else this

fun Color.getDarkThemeColor(): Color {

    if (AppThemeHolder.themeAdapterAuto.not()) return this

    val hsv = floatArrayOf(0f, 0f, 0f)//定义一个长度为3的数组
    val viewColor = this.toArgb()
    android.graphics.Color.colorToHSV(viewColor, hsv)//转化hsv
    //转化结果
    val mHue = hsv[0]//色调范围0-360
    val mSat = hsv[1]//饱和度范围0-1
    var mVal = hsv[2]//亮度范围0-1

    //可以自己调整大小
    mVal = 1.1f - this.luminance()

    if (mVal > 1) mVal = .93f
    if (mVal < 0) mVal = .13f
//    ColorSpace
    val newViewColor = android.graphics.Color.HSVToColor(floatArrayOf(mHue, mSat, mVal))
    return Color(newViewColor)
}

val AppThemeHolder.colorAssets
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme())
        DarkColorAsset
    else
        LightColorAsset

val MaterialTheme.colorAssets
    @Composable
    @ReadOnlyComposable
    get() = AppThemeHolder.colorAssets