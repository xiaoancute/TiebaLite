package com.huanchengfly.tieba.post.ui.utils

import android.app.Activity
import android.graphics.Color as AndroidColor
import android.os.Build
import android.view.Window
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.huanchengfly.tieba.post.findActivity
import com.huanchengfly.tieba.post.utils.ThemeUtil

data class AppSystemBarStyle(
    val decorFitsSystemWindows: Boolean = false,
    @ColorInt val statusBarColor: Int = AndroidColor.TRANSPARENT,
    @ColorInt val navigationBarColor: Int = AndroidColor.TRANSPARENT,
    val darkStatusBarIcons: Boolean = false,
    val darkNavigationBarIcons: Boolean = false,
    val navigationBarContrastEnforced: Boolean = false,
    val statusBarContrastEnforced: Boolean = false,
    val statusBarVisible: Boolean? = true,
    val navigationBarVisible: Boolean? = true,
    val systemBarsBehavior: Int? = null,
)

fun transparentSystemBarStyle(
    darkStatusBarIcons: Boolean = ThemeUtil.isStatusBarFontDark(),
    darkNavigationBarIcons: Boolean = ThemeUtil.isNavigationBarFontDark(),
    statusBarVisible: Boolean? = true,
    navigationBarVisible: Boolean? = true,
    systemBarsBehavior: Int? = null,
): AppSystemBarStyle = AppSystemBarStyle(
    decorFitsSystemWindows = false,
    statusBarColor = AndroidColor.TRANSPARENT,
    navigationBarColor = AndroidColor.TRANSPARENT,
    darkStatusBarIcons = darkStatusBarIcons,
    darkNavigationBarIcons = darkNavigationBarIcons,
    navigationBarContrastEnforced = false,
    statusBarContrastEnforced = false,
    statusBarVisible = statusBarVisible,
    navigationBarVisible = navigationBarVisible,
    systemBarsBehavior = systemBarsBehavior,
)

fun Activity.applySystemBarStyle(style: AppSystemBarStyle) {
    window.applySystemBarStyle(style)
}

fun Activity.setSystemBarsVisible(
    visible: Boolean,
    transientBarsBySwipe: Boolean = false,
) {
    window.setSystemBarVisibility(
        statusBarVisible = visible,
        navigationBarVisible = visible,
        transientBarsBySwipe = transientBarsBySwipe
    )
}

fun Window.applySystemBarStyle(style: AppSystemBarStyle) {
    WindowCompat.setDecorFitsSystemWindows(this, style.decorFitsSystemWindows)
    statusBarColor = style.statusBarColor
    navigationBarColor = style.navigationBarColor
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        isStatusBarContrastEnforced = style.statusBarContrastEnforced
        isNavigationBarContrastEnforced = style.navigationBarContrastEnforced
    }
    val controller = WindowCompat.getInsetsController(this, decorView)
    controller.isAppearanceLightStatusBars = style.darkStatusBarIcons
    controller.isAppearanceLightNavigationBars = style.darkNavigationBarIcons
    style.systemBarsBehavior?.let { controller.systemBarsBehavior = it }
    style.statusBarVisible?.let { visible ->
        if (visible) {
            controller.show(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        }
    }
    style.navigationBarVisible?.let { visible ->
        if (visible) {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }
}

fun Window.setSystemBarVisibility(
    statusBarVisible: Boolean? = null,
    navigationBarVisible: Boolean? = null,
    transientBarsBySwipe: Boolean = false,
) {
    val controller = WindowCompat.getInsetsController(this, decorView)
    if (transientBarsBySwipe) {
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    statusBarVisible?.let { visible ->
        if (visible) {
            controller.show(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        }
    }
    navigationBarVisible?.let { visible ->
        if (visible) {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }
}

@Composable
fun ApplySystemBarStyle(style: AppSystemBarStyle) {
    val activity = LocalContext.current.findActivity()
    SideEffect {
        activity?.applySystemBarStyle(style)
    }
}
