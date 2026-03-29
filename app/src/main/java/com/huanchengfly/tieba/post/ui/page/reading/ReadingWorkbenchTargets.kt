package com.huanchengfly.tieba.post.ui.page.reading

import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.fromJson
import com.huanchengfly.tieba.post.models.ThreadHistoryInfoBean
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.utils.HistoryUtil
import com.huanchengfly.tieba.post.utils.LocalFavoriteUtil
import com.huanchengfly.tieba.post.utils.ReadLaterUtil
import com.huanchengfly.tieba.post.utils.StringUtil

sealed interface ReadingWorkbenchTargetCandidate {
    data class Thread(
        val threadId: Long,
        val title: String,
        val forumName: String?,
        val username: String?,
        val avatar: String?,
    ) : ReadingWorkbenchTargetCandidate

    data class Forum(
        val forumName: String,
        val avatar: String?,
    ) : ReadingWorkbenchTargetCandidate
}

fun buildForumReadingTargetCandidate(
    forumName: String,
    avatar: String? = null,
): ReadingWorkbenchTargetCandidate.Forum? {
    return forumName
        .trim()
        .takeIf { it.isNotBlank() }
        ?.let { ReadingWorkbenchTargetCandidate.Forum(it, avatar) }
}

fun buildHistoryReadingTargetCandidate(
    history: History,
): ReadingWorkbenchTargetCandidate? {
    return when (history.type) {
        HistoryUtil.TYPE_THREAD -> {
            val extra = history.extras
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    runCatching { it.fromJson<ThreadHistoryInfoBean>() }.getOrNull()
                }
            history.data.toLongOrNull()?.let { threadId ->
                ReadingWorkbenchTargetCandidate.Thread(
                    threadId = threadId,
                    title = history.title,
                    forumName = extra?.forumName,
                    username = history.username,
                    avatar = history.avatar,
                )
            }
        }

        HistoryUtil.TYPE_FORUM -> buildForumReadingTargetCandidate(history.data, history.avatar)
        else -> null
    }
}

fun buildSearchForumReadingTargetCandidate(
    item: SearchForumBean.ForumInfoBean,
): ReadingWorkbenchTargetCandidate.Forum? {
    return buildForumReadingTargetCandidate(
        forumName = item.forumName.orEmpty(),
        avatar = item.avatar,
    )
}

fun buildSearchThreadReadingTargetCandidate(
    item: SearchThreadBean.ThreadInfoBean,
): ReadingWorkbenchTargetCandidate.Thread? {
    return item.tid.toLongOrNull()?.let { threadId ->
        ReadingWorkbenchTargetCandidate.Thread(
            threadId = threadId,
            title = item.title
                .ifBlank { item.mainPost?.title.orEmpty() }
                .ifBlank { item.postInfo?.title.orEmpty() }
                .ifBlank { item.content.trim() },
            forumName = item.forumName,
            username = item.user.userName ?: item.user.showNickname,
            avatar = StringUtil.getAvatarUrl(item.user.portrait),
        )
    }
}

fun ReadingWorkbenchTargetCandidate.isInReadLater(): Boolean {
    return when (this) {
        is ReadingWorkbenchTargetCandidate.Thread -> ReadLaterUtil.hasThread(threadId)
        is ReadingWorkbenchTargetCandidate.Forum -> ReadLaterUtil.hasForum(forumName)
    }
}

fun ReadingWorkbenchTargetCandidate.toggleReadLater(): Boolean {
    val added = !isInReadLater()
    when (this) {
        is ReadingWorkbenchTargetCandidate.Thread -> {
            if (added) {
                ReadLaterUtil.saveThread(
                    threadId = threadId,
                    title = title,
                    forumName = forumName,
                    username = username,
                    avatar = avatar,
                )
            } else {
                ReadLaterUtil.removeThread(threadId)
            }
        }

        is ReadingWorkbenchTargetCandidate.Forum -> {
            if (added) {
                ReadLaterUtil.saveForum(forumName = forumName, avatar = avatar)
            } else {
                ReadLaterUtil.removeForum(forumName)
            }
        }
    }
    return added
}

fun ReadingWorkbenchTargetCandidate.isInLocalFavorite(): Boolean {
    return when (this) {
        is ReadingWorkbenchTargetCandidate.Thread -> LocalFavoriteUtil.hasThread(threadId)
        is ReadingWorkbenchTargetCandidate.Forum -> LocalFavoriteUtil.hasForum(forumName)
    }
}

fun ReadingWorkbenchTargetCandidate.toggleLocalFavorite(): Boolean {
    val added = !isInLocalFavorite()
    when (this) {
        is ReadingWorkbenchTargetCandidate.Thread -> {
            if (added) {
                LocalFavoriteUtil.saveThread(
                    threadId = threadId,
                    title = title,
                    forumName = forumName,
                    username = username,
                    avatar = avatar,
                )
            } else {
                LocalFavoriteUtil.removeThread(threadId)
            }
        }

        is ReadingWorkbenchTargetCandidate.Forum -> {
            if (added) {
                LocalFavoriteUtil.saveForum(forumName = forumName, avatar = avatar)
            } else {
                LocalFavoriteUtil.removeForum(forumName)
            }
        }
    }
    return added
}
