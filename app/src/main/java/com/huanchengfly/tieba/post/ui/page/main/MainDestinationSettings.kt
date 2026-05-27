package com.huanchengfly.tieba.post.ui.page.main

import com.huanchengfly.tieba.post.ui.models.settings.DefaultMainPage

fun DefaultMainPage.resolveMainDestination(
    loggedIn: Boolean,
    hideExplore: Boolean,
): MainDestination {
    return when (this) {
        DefaultMainPage.HOME -> MainDestination.Home
        DefaultMainPage.EXPLORE -> MainDestination.Explore.takeUnless { hideExplore } ?: MainDestination.Home
        DefaultMainPage.NOTIFICATION -> MainDestination.Notification.takeIf { loggedIn } ?: MainDestination.Home
        DefaultMainPage.USER -> MainDestination.User
    }
}
