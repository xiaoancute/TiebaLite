package com.huanchengfly.tieba.post.ui.page.search.forum

import com.huanchengfly.tieba.post.api.models.SearchForumBean

internal fun SearchForumBean.ForumInfoBean.preferredSummaryText(): String? =
    slogan?.takeIf { it.isNotBlank() }
        ?: intro?.takeIf { it.isNotBlank() }

internal fun SearchForumBean.ForumInfoBean.displayName(): String =
    forumNameShow?.takeIf { it.isNotBlank() }
        ?: forumName.orEmpty()
