package com.huanchengfly.tieba.post

import androidx.lifecycle.Lifecycle
import com.ramcosta.composedestinations.spec.Direction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectionNavigationTest {
    @Test
    fun navigateDirectionIfResumedDispatchesDirection() {
        val navigatedRoutes = mutableListOf<String>()
        val direction = FakeDirection("tblite://forum/test")

        navigateDirectionIfResumed(
            currentLifecycleState = Lifecycle.State.RESUMED,
            direction = direction,
        ) { navigatedRoutes += it.route }

        assertEquals(listOf(direction.route), navigatedRoutes)
    }

    @Test
    fun navigateDirectionIfResumedSkipsNonResumedState() {
        val navigatedRoutes = mutableListOf<String>()
        val direction = FakeDirection("tblite://forum/test")

        navigateDirectionIfResumed(
            currentLifecycleState = Lifecycle.State.STARTED,
            direction = direction,
        ) { navigatedRoutes += it.route }

        assertTrue(navigatedRoutes.isEmpty())
    }

    @Test
    fun navigateDirectionIfResumedIgnoresMissingDirection() {
        val navigatedRoutes = mutableListOf<String>()

        navigateDirectionIfResumed(
            currentLifecycleState = Lifecycle.State.RESUMED,
            direction = null,
        ) { navigatedRoutes += it.route }

        assertTrue(navigatedRoutes.isEmpty())
    }

    private data class FakeDirection(
        override val route: String
    ) : Direction
}
