package com.huanchengfly.tieba.post.ui.page.main

import com.huanchengfly.tieba.post.ui.models.settings.DefaultMainPage
import org.junit.Assert.assertSame
import org.junit.Test

class MainDestinationSettingsTest {

    @Test
    fun `home default opens home`() {
        val destination = DefaultMainPage.HOME.resolveMainDestination(loggedIn = false, hideExplore = false)

        assertSame(MainDestination.Home, destination)
    }

    @Test
    fun `explore default falls back to home when explore is hidden`() {
        val destination = DefaultMainPage.EXPLORE.resolveMainDestination(loggedIn = true, hideExplore = true)

        assertSame(MainDestination.Home, destination)
    }

    @Test
    fun `notification default falls back to home when logged out`() {
        val destination = DefaultMainPage.NOTIFICATION.resolveMainDestination(loggedIn = false, hideExplore = false)

        assertSame(MainDestination.Home, destination)
    }

    @Test
    fun `available defaults resolve to their matching destination`() {
        assertSame(
            MainDestination.Explore,
            DefaultMainPage.EXPLORE.resolveMainDestination(loggedIn = false, hideExplore = false)
        )
        assertSame(
            MainDestination.Notification,
            DefaultMainPage.NOTIFICATION.resolveMainDestination(loggedIn = true, hideExplore = false)
        )
        assertSame(
            MainDestination.User,
            DefaultMainPage.USER.resolveMainDestination(loggedIn = false, hideExplore = true)
        )
    }
}
