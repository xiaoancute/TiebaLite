@file:Suppress("DEPRECATION")

package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder as accompanistPlaceholder

@Composable
fun Modifier.tiebaPlaceholder(
    visible: Boolean,
    color: Color? = null,
    shape: Shape? = null,
): Modifier = tiebaPlaceholderInternal(
    visible = visible,
    color = color,
    shape = shape,
    highlight = null,
)

@Composable
fun Modifier.tiebaPlaceholderFade(
    visible: Boolean,
    color: Color? = null,
    shape: Shape? = null,
): Modifier = tiebaPlaceholderInternal(
    visible = visible,
    color = color,
    shape = shape,
    highlight = PlaceholderHighlight.fade(),
)

@Composable
private fun Modifier.tiebaPlaceholderInternal(
    visible: Boolean,
    color: Color?,
    shape: Shape?,
    highlight: PlaceholderHighlight?,
): Modifier {
    return if (color != null) {
        accompanistPlaceholder(
            visible = visible,
            color = color,
            shape = shape,
            highlight = highlight,
        )
    } else {
        accompanistPlaceholder(
            visible = visible,
            shape = shape,
            highlight = highlight,
        )
    }
}
