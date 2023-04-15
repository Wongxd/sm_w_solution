package io.wongxd.solution.compose.composeTheme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

object SizeAssets {

    val smallHorizontalPadding = 15.dp

    val midHorizontalPadding = 20.dp

}

val AppThemeHolder.sizeAssets
    @Composable @ReadOnlyComposable get() = SizeAssets

val MaterialTheme.sizeAssets
    @Composable @ReadOnlyComposable get() = AppThemeHolder.sizeAssets