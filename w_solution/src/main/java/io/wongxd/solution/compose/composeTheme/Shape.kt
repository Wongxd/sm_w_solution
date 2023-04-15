

package io.wongxd.solution.compose.composeTheme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp


object ShapeBrushAssets {
    val blueFullBgShape = Brush.horizontalGradient(listOf(Color(0xFFB87AE6), Color(0xFF3E4FDA)))

    val redGradientShape = Brush.horizontalGradient(listOf(Color(0xFFFBC09F), Color(0xFFFD6691)))

    val blueGradientShape = Brush.horizontalGradient(listOf(Color(0xFFB87AE6), Color(0xFF3E4FDA)))
}

val AppThemeHolder.shapeBrushAssets
    @Composable
    @ReadOnlyComposable
    get() = ShapeBrushAssets


val baseShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)

val BottomSheetShape = RoundedCornerShape(
    topStart = CornerSize(16.dp),
    topEnd = CornerSize(16.dp),
    bottomEnd = CornerSize(0.dp),
    bottomStart = CornerSize(0.dp)
)

val DiagonalShape: Shape = object : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        path.lineTo(size.width, 0f)
        path.lineTo(size.width, size.height / 1.5f)
        path.lineTo(0f, size.height)
        path.close()
        return Outline.Generic(path = path)
    }

    override fun toString(): String = "DiagonalShape"

}

class BezierShape(private val padding: Float) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        path.lineTo(0f, size.height - padding)
        path.quadraticBezierTo(size.width / 2, size.height, size.width, size.height - padding)
        path.lineTo(size.width, size.height - padding)
        path.lineTo(size.width, 0f)
        path.lineTo(0f, 0f)
        return Outline.Generic(path = path)
    }

    override fun toString(): String = "DiagonalShape"

}