package com.huanchengfly.tieba.post

import androidx.lifecycle.Lifecycle
import com.ramcosta.composedestinations.spec.Direction

internal fun navigateDirectionIfResumed(
    currentLifecycleState: Lifecycle.State?,
    direction: Direction?,
    navigate: (Direction) -> Unit,
) {
    if (currentLifecycleState == Lifecycle.State.RESUMED && direction != null) {
        navigate(direction)
    }
}
