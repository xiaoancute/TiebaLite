package com.huanchengfly.tieba.post.api.interfaces.impls

import com.huanchengfly.tieba.post.api.models.FollowedForum
import com.huanchengfly.tieba.post.api.models.protos.forumGuide.LikeForum
import com.huanchengfly.tieba.post.api.models.web.ForumHomeData

internal suspend fun collectAllForumHomeItems(
    fetchPage: suspend (page: Int) -> ForumHomeData.LikeForum?,
): List<ForumHomeData.LikeForum.ListItem> {
    val allForums = mutableListOf<ForumHomeData.LikeForum.ListItem>()
    var page = 0

    while (true) {
        val likeForum = fetchPage(page) ?: break
        allForums += likeForum.list

        val totalPage = likeForum.page.totalPage
        if (totalPage <= 0 || page >= totalPage - 1 || likeForum.list.isEmpty()) {
            break
        }
        page += 1
    }

    return allForums
}

internal fun mergeFollowedForums(
    pagedForums: List<ForumHomeData.LikeForum.ListItem>,
    guideForums: List<LikeForum>,
): List<FollowedForum> {
    val mergedForums = linkedMapOf<Long, FollowedForum>()
    val guideById = guideForums.associateBy { it.forum_id }

    pagedForums.forEach { pagedForum ->
        val guideForum = guideById[pagedForum.forumId]
        mergedForums[pagedForum.forumId] = if (guideForum != null) {
            guideForum.toFollowedForum()
        } else {
            pagedForum.toFollowedForum()
        }
    }

    guideForums.forEach { guideForum ->
        mergedForums.putIfAbsent(guideForum.forum_id, guideForum.toFollowedForum())
    }

    return mergedForums.values.toList()
}

internal fun LikeForum.toFollowedForum(): FollowedForum =
    FollowedForum(
        avatar = avatar,
        forumId = forum_id,
        forumName = forum_name,
        isSign = is_sign == 1,
        levelId = level_id,
        hotNum = hot_num,
    )

private fun ForumHomeData.LikeForum.ListItem.toFollowedForum(): FollowedForum =
    FollowedForum(
        avatar = avatar,
        forumId = forumId,
        forumName = forumName,
        isSign = false,
        levelId = levelId,
        hotNum = hotNum.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
    )
