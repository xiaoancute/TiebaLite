package com.huanchengfly.tieba.post.ui.models.forum

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.ui.models.Author
import com.huanchengfly.tieba.post.ui.common.PbContentRender

typealias ForumManager = Author

@Immutable
data class ForumDetail(
    val avatar: String,
    val name: String,
    val id: Long,
    val intro: String? = null,
    val slogan: String = "",
    val memberCount: Long = 0,
    val threadCount: Long = 0,
    val postCount: Long = 0,
    val introRenders: List<PbContentRender> = emptyList(),
    val managerGroups: List<ForumManagerGroup> = emptyList(),
    val friendForums: List<FriendForum> = emptyList(),
)

@Immutable
data class ForumManagerGroup(val name: String, val members: List<ForumManager>)

@Immutable
data class FriendForum(val id: Long, val name: String, val avatar: String)

@Immutable
data class ForumIntroduction(val slogan: String, val content: List<PbContentRender>)
