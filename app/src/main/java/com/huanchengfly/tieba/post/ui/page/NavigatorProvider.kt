@file:Suppress("NOTHING_TO_INLINE")

package com.huanchengfly.tieba.post.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.huanchengfly.tieba.post.ui.widgets.compose.SimplePredictiveBackHandler

val LocalNavController = staticCompositionLocalOf<NavController> { error("No navigator is available") }

@NonSkippableComposable
@Composable
fun ProvideNavigator(
    navigator: NavController,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNavController provides navigator, content = content)
}

/**
 * Destination-level fallback: higher priority than NavHost's default back callback,
 * lower priority than screen-local handlers composed after it.
 */
@Composable
fun PredictiveNavigateUpHandler(
    navController: NavController,
    backStackEntry: NavBackStackEntry? = null,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    SimplePredictiveBackHandler(
        enabled = currentBackStackEntry != null &&
            (backStackEntry == null || currentBackStackEntry == backStackEntry) &&
            navController.previousBackStackEntry != null
    ) {
        navController.navigateUp()
    }
}

inline fun <T> NavController.setResult(key: String, value: T) {
    previousBackStackEntry?.savedStateHandle?.set(key, value)
}

inline fun <reified Route : Any, T> NavController.consumeResult(key: String): T? {
    return getBackStackEntry<Route>().savedStateHandle.remove(key)
}
