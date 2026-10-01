package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.ForumOverviewResponse
import com.huanchengfly.tieba.post.api.models.protos.BawuTeam
import com.huanchengfly.tieba.post.ui.models.forum.ForumDetail
import com.huanchengfly.tieba.post.ui.models.forum.ForumManager
import com.huanchengfly.tieba.post.ui.models.forum.ForumManagerGroup
import com.huanchengfly.tieba.post.ui.models.forum.FriendForum
import com.huanchengfly.tieba.post.utils.StringUtil

internal fun ForumOverviewResponse.toForumDetail(requestedName: String): ForumDetail {
    val info = requireNotNull(forum)
    return ForumDetail(
        id = info.id,
        name = info.name?.trim().takeUnless { it.isNullOrEmpty() } ?: requestedName,
        avatar = info.avatar.orEmpty(),
        slogan = info.slogan.orEmpty().trim(),
        intro = info.desc?.trim()?.takeIf { it.isNotEmpty() },
        memberCount = info.memberCount,
        threadCount = info.threadCount,
        postCount = info.postCount,
        friendForums = friendForums.orEmpty().mapNotNull {
            val name = it.name?.trim()?.takeIf(String::isNotEmpty) ?: return@mapNotNull null
            FriendForum(it.id, name, it.avatar.orEmpty())
        }.distinctBy { it.name },
    )
}

internal fun BawuTeam.toManagerGroups(): List<ForumManagerGroup> = bawu_team_list.mapNotNull { group ->
    val members = group.role_info.map { member ->
        ForumManager(
            id = member.user_id,
            name = member.name_show.ifBlank { member.user_name }.ifBlank { member.user_id.toString() },
            avatarUrl = StringUtil.getAvatarUrl(member.portrait),
        )
    }.distinctBy { if (it.id > 0) it.id.toString() else it.name }
    if (members.isEmpty()) null else ForumManagerGroup(
        name = group.role_name.ifBlank { group.role_info.first().role_name },
        members = members,
    )
}
