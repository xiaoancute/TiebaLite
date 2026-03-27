package com.huanchengfly.tieba.post.ui.page.search.forum

import com.huanchengfly.tieba.post.api.models.SearchForumBean

enum class SearchForumStatType {
    CONCERN,
    POST,
}

data class SearchForumStatItem(
    val type: SearchForumStatType,
    val value: String,
)

fun buildSearchForumStats(forum: SearchForumBean.ForumInfoBean): List<SearchForumStatItem> =
    listOf(
        SearchForumStatItem(
            type = SearchForumStatType.CONCERN,
            value = forum.concernNum.ifBlank { "0" }
        ),
        SearchForumStatItem(
            type = SearchForumStatType.POST,
            value = forum.postNum.ifBlank { "0" }
        )
    )
