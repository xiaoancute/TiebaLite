package com.huanchengfly.tieba.post.api.models

import androidx.compose.runtime.Immutable

@Immutable
data class FollowedForum(
    val avatar: String,
    val forumId: Long,
    val forumName: String,
    val isSign: Boolean,
    val levelId: Int,
    val hotNum: Int,
)
