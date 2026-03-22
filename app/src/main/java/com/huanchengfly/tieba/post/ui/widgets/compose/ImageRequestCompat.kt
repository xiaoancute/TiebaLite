package com.huanchengfly.tieba.post.ui.widgets.compose

import com.github.panpf.sketch.request.DisplayRequest

fun DisplayRequest.Builder.applyTiebaImageTransition(): DisplayRequest.Builder = apply {
    crossfade()
}

fun DisplayRequest.Builder.applyTiebaPreviewTransition(): DisplayRequest.Builder = apply {
    crossfade(fadeStart = false)
}
