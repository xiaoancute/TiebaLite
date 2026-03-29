package com.huanchengfly.tieba.post.models.database

import androidx.compose.runtime.Immutable
import org.litepal.crud.LitePalSupport

@Immutable
data class ReadingProgress(
    val threadId: Long = 0L,
    val threadTitle: String = "",
    val forumName: String? = null,
    val authorName: String? = null,
    val authorAvatar: String? = null,
    val postId: Long = 0L,
    val floor: Int = 0,
    val seeLz: Boolean = false,
    val timestamp: Long = 0L,
) : LitePalSupport() {
    val id: Long = 0L
}
