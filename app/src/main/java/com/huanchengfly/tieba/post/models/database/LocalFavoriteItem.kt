package com.huanchengfly.tieba.post.models.database

import androidx.compose.runtime.Immutable
import org.litepal.crud.LitePalSupport

@Immutable
data class LocalFavoriteItem(
    val targetType: Int = 0,
    val threadId: Long = 0L,
    val forumName: String = "",
    val title: String = "",
    val subtitle: String? = null,
    val avatar: String? = null,
    val username: String? = null,
    val timestamp: Long = 0L,
) : LitePalSupport() {
    val id: Long = 0L
}
