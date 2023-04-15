package io.wongxd.solution.compose.composeTheme

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import io.wongxd.solution.R

private val LightColorPalette = lightColors(
    primary = PrimaryColorLight,
    primaryVariant = PrimaryVariantColorLight,
    secondary = DivideColorLight,
    background = BackgroundColorLight,
    surface = SurfaceColorLight
)

@Composable
fun AppTheme(
    appTheme: AppTheme = AppThemeHolder.currentTheme,
    content: @Composable () -> Unit
) {

//    val colors = when (appTheme) {
//        AppTheme.Light -> {
//            LightColorPalette
//        }
//        AppTheme.Dark -> {
//            LightColorPalette
//        }
//    }
//
//    val typography = if (appTheme.isDarkTheme()) {
//        LightTypography
//    } else {
//        LightTypography
//    }

    val themeType = remember {
        AppCompatDelegate.getDefaultNightMode()
    }

    var colors = LightColorPalette
    if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
        // 关闭暗黑模式
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    } else {
        colors = LightColorPalette
        // 开启暗黑模式
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    val fontScale = LocalDensity.current.fontScale
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val widthPixels = displayMetrics.widthPixels
    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = baseShapes,
        content = {
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = widthPixels / 375.0f,
                    fontScale = fontScale
                )
            ) {
                content()
            }
        }
    )
}

@Composable
fun PreviewTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = LightColorPalette,
        typography = LightTypography,
        shapes = baseShapes,
        content = content
    )
}