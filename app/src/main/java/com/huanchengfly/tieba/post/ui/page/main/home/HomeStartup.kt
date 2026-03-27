package com.huanchengfly.tieba.post.ui.page.main.home

internal fun buildHomeStartupIntents(
    isLoggedIn: Boolean,
    initialized: Boolean,
): List<HomeUiIntent> {
    if (!isLoggedIn || initialized) return emptyList()
    return listOf(HomeUiIntent.RefreshHistory, HomeUiIntent.Refresh)
}
