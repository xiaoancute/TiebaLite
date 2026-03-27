package com.huanchengfly.tieba.post.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

fun <T> wrapOKSignFlowForFailurePolicy(
    stopOnFailure: Boolean,
    source: Flow<T>,
    onFailure: suspend (Throwable) -> Unit,
): Flow<T> = if (stopOnFailure) {
    source
} else {
    source.catch { throwable ->
        onFailure(throwable)
    }
}
