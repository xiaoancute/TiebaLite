package com.huanchengfly.tieba.post.ui.page.thread

internal fun formatPollOptionIds(optionIds: Set<Int>): String {
    return optionIds
        .filter { it >= 0 }
        .sorted()
        .joinToString(separator = ",")
}

internal fun parsePollOptionIds(value: String): Set<Int> {
    return value
        .split(',')
        .mapNotNull { it.trim().toIntOrNull() }
        .filter { it >= 0 }
        .toSet()
}
