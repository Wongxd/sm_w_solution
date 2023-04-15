package io.wongxd.solution.compose.custom

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
fun MyAutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Visible,
    softWrap: Boolean = true,
    maxLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    fontList: Array<TextUnit>,
) {
    BoxWithConstraints(modifier) {
        val localDensity = LocalDensity.current
        SubcomposeLayout{ constraints ->
            Log.i("MyAutoSizeText","width = ${maxWidth}")

            val index = subcompose("text") {
                fontList.forEach {
                    Text(
                        text = text,
                        color = color,
                        maxLines = maxLines,
                        fontStyle = fontStyle,
                        fontWeight = fontWeight,
                        fontFamily = fontFamily,
                        letterSpacing = letterSpacing,
                        textDecoration = textDecoration,
                        textAlign = textAlign,
                        lineHeight = lineHeight,
                        overflow = overflow,
                        softWrap = softWrap,
                        style = style.copy(fontSize = it))
                }
            }.indexOfFirst {
                val placeable = it.measure(constraints.copy(maxWidth = Int.MAX_VALUE))
                placeable.width <= with(localDensity){maxWidth.toPx()}
            }

            val textSize = if (index >= 0) {
                fontList[index]
            } else {
                fontList.minOf { it.value }.toSp()
            }

            Log.i("MyAutoSizeText","textSize = ${textSize}")

            val contentPlaceable = subcompose("content") {
                Text(
                    modifier = modifier,
                    text = text,
                    color = color,
                    maxLines = maxLines,
                    fontStyle = fontStyle,
                    fontWeight = fontWeight,
                    fontFamily = fontFamily,
                    letterSpacing = letterSpacing,
                    textDecoration = textDecoration,
                    textAlign = textAlign,
                    lineHeight = lineHeight,
                    overflow = overflow,
                    softWrap = softWrap,
                    style = style.copy(fontSize = textSize))
            }[0].measure(constraints)

            layout(contentPlaceable.width, contentPlaceable.height) {
                contentPlaceable.place(0, 0)
            }
        }
    }
}