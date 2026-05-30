package com.huanchengfly.tieba.post.ui.models.settings

import androidx.compose.runtime.Immutable

/**
 * 隐私设置
 *
 * @param incognitoMode 不保存浏览记录
 * @param requestNotificationPermission 启动时请求通知权限
 * @param readClipBoardLink 读取并打开剪贴板中的贴吧链接
 * */
@Immutable
data class PrivacySettings(
    val incognitoMode: Boolean = false,
    val requestNotificationPermission: Boolean = true,
    val readClipBoardLink: Boolean = true
)
