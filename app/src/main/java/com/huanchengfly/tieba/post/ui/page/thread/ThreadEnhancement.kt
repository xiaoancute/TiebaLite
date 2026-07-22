package com.huanchengfly.tieba.post.ui.page.thread

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.ui.models.PostData

internal const val THREAD_FIRST_POST_LIST_KEY = "FirstPost"

internal fun latestPostListKey(postId: Long): String = "LatestPost_$postId"

@Immutable
data class ThreadEnhancementPost(
    val postId: Long,
    val floor: Int,
    val authorId: Long,
    val title: String?,
    val plainText: String,
    val isLz: Boolean,
    val isBlocked: Boolean,
    val listKey: Any,
) {
    val previewText: String
        get() = if (isBlocked) {
            ""
        } else {
            listOfNotNull(title, plainText)
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .joinToString(separator = " ")
                .replace(WHITESPACE, " ")
        }
}

@Immutable
data class ThreadEnhancementState(
    val posts: List<ThreadEnhancementPost>,
    val lzPosts: List<ThreadEnhancementPost>,
) {
    fun search(query: String): List<ThreadEnhancementPost> {
        val literalQuery = query.trim()
        if (literalQuery.isEmpty()) return emptyList()

        return posts.filter { post ->
            !post.isBlocked && (
                post.title?.contains(literalQuery, ignoreCase = true) == true ||
                    post.plainText.contains(literalQuery, ignoreCase = true)
                )
        }
    }

    companion object {
        val Empty = ThreadEnhancementState(emptyList(), emptyList())
    }
}

internal fun ThreadUiState.toThreadEnhancementState(): ThreadEnhancementState {
    val lzId = lz?.id
    val postsById = linkedMapOf<Long, ThreadEnhancementPost>()

    fun add(post: PostData, listKey: Any) {
        postsById.putIfAbsent(
            post.id,
            post.toThreadEnhancementPost(lzId = lzId, listKey = listKey)
        )
    }

    firstPost?.let { add(it, THREAD_FIRST_POST_LIST_KEY) }

    if (sortType == ThreadSortType.BY_DESC) {
        latestPosts.orEmpty().forEach { add(it, latestPostListKey(it.id)) }
    }

    data.forEach { add(it, it.id) }

    if (sortType != ThreadSortType.BY_DESC) {
        latestPosts.orEmpty().forEach { add(it, latestPostListKey(it.id)) }
    }

    val posts = postsById.values.toList()
    return ThreadEnhancementState(
        posts = posts,
        lzPosts = posts.filter(ThreadEnhancementPost::isLz),
    )
}

internal fun ThreadUiState.threadListIndexOf(listKey: Any): Int? {
    var index = 0

    if (listKey == THREAD_FIRST_POST_LIST_KEY) {
        return index.takeIf { firstPost != null }
    }
    index += 1 // First post item

    if (thread != null) {
        index += 1 // Thread header
    }

    val latestPosts = latestPosts.orEmpty()
    if (sortType == ThreadSortType.BY_DESC && latestPosts.isNotEmpty()) {
        latestPosts.forEach { post ->
            if (listKey == latestPostListKey(post.id)) return index
            index += 1
        }
        index += 1 // Latest-post tip
    }

    if (pageData.hasPrevious) {
        index += 1 // Load previous button
    }

    if (data.isEmpty()) {
        index += 1 // Empty-state item
    } else {
        data.forEach { post ->
            if (listKey == post.id) return index
            index += 1
        }
    }

    if (sortType != ThreadSortType.BY_DESC && latestPosts.isNotEmpty()) {
        index += 1 // Latest-post tip
        latestPosts.forEach { post ->
            if (listKey == latestPostListKey(post.id)) return index
            index += 1
        }
    }

    return null
}

private fun PostData.toThreadEnhancementPost(lzId: Long?, listKey: Any): ThreadEnhancementPost {
    val blockedTitle = title.takeUnless { blocked }
    val blockedPlainText = plainText.takeUnless { blocked }.orEmpty()

    return ThreadEnhancementPost(
        postId = id,
        floor = floor,
        authorId = author.id,
        title = blockedTitle,
        plainText = blockedPlainText,
        isLz = lzId != null && author.id == lzId,
        isBlocked = blocked,
        listKey = listKey,
    )
}

private val WHITESPACE = Regex("\\s+")
