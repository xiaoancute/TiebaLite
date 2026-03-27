package com.huanchengfly.tieba.post.ui.page.main.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeStartupTest {
    @Test
    fun loggedOutHomeStartsIdleForLoginGuide() {
        assertFalse(HomeUiState().isLoading)
        assertTrue(buildHomeStartupIntents(isLoggedIn = false, initialized = false).isEmpty())
    }

    @Test
    fun loggedInHomeRequestsStartupRefreshOnlyOnce() {
        assertEquals(
            listOf(HomeUiIntent.RefreshHistory, HomeUiIntent.Refresh),
            buildHomeStartupIntents(isLoggedIn = true, initialized = false)
        )
        assertTrue(buildHomeStartupIntents(isLoggedIn = true, initialized = true).isEmpty())
    }
}
