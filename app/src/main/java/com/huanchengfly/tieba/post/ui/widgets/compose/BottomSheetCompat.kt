@file:Suppress("DEPRECATION")

package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable

object TiebaBottomSheetDefaults {
    val AnimationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun rememberTiebaBottomSheetState(
    skipHalfExpanded: Boolean = false,
    animationSpec: AnimationSpec<Float> = TiebaBottomSheetDefaults.AnimationSpec,
): ModalBottomSheetState = rememberModalBottomSheetState(
    initialValue = ModalBottomSheetValue.Hidden,
    animationSpec = animationSpec,
    skipHalfExpanded = skipHalfExpanded,
)
