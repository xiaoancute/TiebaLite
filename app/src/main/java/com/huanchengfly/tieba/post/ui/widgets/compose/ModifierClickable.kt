package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role


@Composable
fun Modifier.debounceClickable(
    delayMillis: Long = 500L,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
    indication: Indication?,
    enabled: Boolean = true,
    role: Role? = null,
): Modifier = composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    clickable(
        enabled = enabled,
        role = role,
        interactionSource = interactionSource,
        indication = if (System.currentTimeMillis() - lastClickTime < delayMillis)
            null
        else
            indication
    ) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= delayMillis) {
            onClick()
        }
        lastClickTime = currentTime
    }
}


@Composable
fun Modifier.debounceClickable(
    delayMillis: Long = 500L,
    onClick: () -> Unit,
    enabled: Boolean = true,
    role: Role? = null,
): Modifier = composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    clickable(
        enabled = enabled,
        role = role
    ) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= delayMillis) {
            onClick()
        }
        lastClickTime = currentTime
    }
}