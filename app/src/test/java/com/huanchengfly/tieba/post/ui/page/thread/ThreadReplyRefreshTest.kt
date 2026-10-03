package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.post.repository.PageData
import com.huanchengfly.tieba.post.ui.models.LikeZero
import com.huanchengfly.tieba.post.ui.models.PostData
import com.huanchengfly.tieba.post.ui.models.UserData
import org.junit.Assert.*
import org.junit.Test

class ThreadReplyRefreshTest {
    @Test fun `nested reply refresh preserves loaded floor order and pagination in either sort direction`() {
        for (sort in listOf(ThreadSortType.DEFAULT, ThreadSortType.BY_DESC)) {
            val posts = (2L..21L).map(::post).let { if (sort == ThreadSortType.BY_DESC) it.reversed() else it }
            val state = ThreadUiState(
                firstPost = post(1), data = posts, latestPosts = listOf(post(50)),
                sortType = sort, pageData = PageData(current = 2, hasMore = true, hasPrevious = true),
            )
            val refreshed = post(5).copy(subPostNumber = 10)
            val result = state.refreshRepliedPost(refreshed)
            assertEquals(posts.map { it.id }, result.data.map { it.id })
            assertEquals(10, result.data.first { it.id == 5L }.subPostNumber)
            assertEquals(state.pageData, result.pageData)
            assertEquals(state.latestPosts, result.latestPosts)
            assertEquals(state.sortType, result.sortType)
            assertEquals(state.threadListIndexOf(5L), result.threadListIndexOf(5L))
        }
    }

    @Test fun `missing or unloaded parent does not insert unrelated floors`() {
        val state = ThreadUiState(firstPost = post(1), data = listOf(post(2), post(3)))
        assertSame(state, state.refreshRepliedPost(null))
        assertEquals(state, state.refreshRepliedPost(post(99)))
    }

    @Test fun `parent in latest reply section updates without replacing that section`() {
        val state = ThreadUiState(data = listOf(post(2)), latestPosts = listOf(post(50), post(51)))
        val result = state.refreshRepliedPost(post(50).copy(subPostNumber = 2))
        assertEquals(listOf(50L, 51L), result.latestPosts!!.map { it.id })
        assertEquals(2, result.latestPosts.first().subPostNumber)
        assertEquals(state.data, result.data)
    }

    private fun post(id: Long) = PostData(
        id = id, floor = id.toInt(), title = null, time = 0, like = LikeZero,
        blocked = false, plainText = "",
        author = UserData(
            id = 1, name = "user", nameShow = "user", showBothName = false,
            avatarUrl = "", portrait = "", ip = "", levelId = 0, bawuType = null, isLz = false,
        ),
        contentRenders = emptyList(), subPosts = null, subPostNumber = 0,
    )
}
