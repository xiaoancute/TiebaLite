package com.huanchengfly.tieba.post.ui.page.main

import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MainTabNavigationPolicyTest {

    @Test
    fun `switching main tabs replaces the current root destination`() {
        val options = mainTabNavigationOptions(
            currentDestination = MainDestination.Explore,
            fallbackStartDestination = MainDestination.Home,
        )

        assertSame(MainDestination.Explore, options.popUpTo)
        assertTrue(options.inclusive)
        assertTrue(options.saveState)
        assertTrue(options.restoreState)
        assertTrue(options.launchSingleTop)
    }

    @Test
    fun `switching before current destination is known falls back to start destination`() {
        val options = mainTabNavigationOptions(
            currentDestination = null,
            fallbackStartDestination = MainDestination.Home,
        )

        assertSame(MainDestination.Home, options.popUpTo)
        assertTrue(options.inclusive)
    }
}
