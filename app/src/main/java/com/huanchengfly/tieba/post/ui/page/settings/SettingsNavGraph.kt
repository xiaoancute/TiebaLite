package com.huanchengfly.tieba.post.ui.page.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.huanchengfly.tieba.post.navigateDebounced
import com.huanchengfly.tieba.post.repository.user.SettingsRepository
import com.huanchengfly.tieba.post.ui.page.PredictiveNavigateUpHandler
import com.huanchengfly.tieba.post.ui.page.settings.blocklist.ForumBlockListPage
import com.huanchengfly.tieba.post.ui.page.settings.blocklist.KeywordBlockListPage
import com.huanchengfly.tieba.post.ui.page.settings.blocklist.UserBlockListPage
import com.huanchengfly.tieba.post.ui.page.settings.theme.AppFontPage
import kotlinx.serialization.Serializable

sealed interface SettingsDestination {

    @Serializable
    object Settings: SettingsDestination

    @Serializable
    object About: SettingsDestination

    @Serializable
    object AccountManage: SettingsDestination

    @Serializable
    object AppFont: SettingsDestination

    @Serializable
    object BlockSettings: SettingsDestination

    @Serializable
    object ForumBlockList: SettingsDestination

    @Serializable
    object KeywordBlockList: SettingsDestination

    @Serializable
    object UserBlockList: SettingsDestination

    @Serializable
    object UI: SettingsDestination

    @Serializable
    object Habit: SettingsDestination

    @Serializable
    object Privacy: SettingsDestination

    @Serializable
    object StickyHeader: SettingsDestination

    @Serializable
    object More: SettingsDestination

    @Serializable
    object OKSign: SettingsDestination

    @Serializable
    object WorkInfo: SettingsDestination
}

fun NavGraphBuilder.settingsGraph(navController: NavController, settingsRepo: SettingsRepository) {
    settingsComposable<SettingsDestination.Settings>(navController) {
        SettingsPage(navController)
    }

    settingsComposable<SettingsDestination.About>(navController) {
        AboutPage(navController::navigateUp)
    }

    settingsComposable<SettingsDestination.AccountManage>(navController) {
        AccountManagePage(myLittleTailSettings = settingsRepo.myLittleTail, navController)
    }

    settingsComposable<SettingsDestination.AppFont>(navController) {
        AppFontPage(navController::navigateUp)
    }

    settingsComposable<SettingsDestination.BlockSettings>(navController) {
        BlockSettingsPage(settings = settingsRepo.blockSettings, navController)
    }

    settingsComposable<SettingsDestination.ForumBlockList>(navController) {
        ForumBlockListPage(onBack = navController::navigateUp)
    }

    settingsComposable<SettingsDestination.KeywordBlockList>(navController) {
        KeywordBlockListPage(onBack = navController::navigateUp)
    }

    settingsComposable<SettingsDestination.UserBlockList>(navController) {
        UserBlockListPage(onBack = navController::navigateUp)
    }

    settingsComposable<SettingsDestination.UI>(navController) {
        UISettingsPage(settings = settingsRepo.uiSettings, navController)
    }

    settingsComposable<SettingsDestination.Habit>(navController) {
        HabitSettingsPage(
            habitSettings = settingsRepo.habitSettings,
            onStickyHeaderClicked = {
                navController.navigateDebounced(SettingsDestination.StickyHeader)
            },
            onBack = navController::navigateUp
        )
    }

    settingsComposable<SettingsDestination.Privacy>(navController) {
        PrivacySettingsPage(settingsRepo.privacySettings, onBack = navController::navigateUp)
    }

    settingsComposable<SettingsDestination.StickyHeader>(navController) {
        StickyHeaderSettingsPage(settingsRepo.habitSettings, onBack = navController::navigateUp)
    }

    settingsComposable<SettingsDestination.More>(navController) {
        MoreSettingsPage(navController, habitSettings = settingsRepo.habitSettings)
    }

    settingsComposable<SettingsDestination.OKSign>(navController) {
        OKSignSettingsPage(settings = settingsRepo.signConfig, onBack = navController::navigateUp)
    }

    settingsComposable<SettingsDestination.WorkInfo>(navController) {
        WorkInfoPage(onBack = navController::navigateUp)
    }
}

private inline fun <reified T : Any> NavGraphBuilder.settingsComposable(
    navController: NavController,
    noinline content: @Composable (NavBackStackEntry) -> Unit,
) {
    composable<T> { backStackEntry ->
        PredictiveNavigateUpHandler(navController, backStackEntry)
        content(backStackEntry)
    }
}
