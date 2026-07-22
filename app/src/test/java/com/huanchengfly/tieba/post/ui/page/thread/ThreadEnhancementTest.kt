package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.post.repository.PageData
import com.huanchengfly.tieba.post.ui.models.LikeZero
import com.huanchengfly.tieba.post.ui.models.PostData
import com.huanchengfly.tieba.post.ui.models.ThreadInfoData
import com.huanchengfly.tieba.post.ui.models.UserData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreadEnhancementTest {
    @Test
    fun `builder includes loaded posts, deduplicates overlaps, and selects LZ posts`() {
        val firstPost = post(id = 1, floor = 1, authorId = LZ_ID, title = "标题")
        val lzReply = post(id = 2, floor = 2, authorId = LZ_ID, plainText = "楼主回复")
        val normalReply = post(id = 3, floor = 3, authorId = 20, plainText = "普通回复")
        val latestLzReply = post(id = 4, floor = 4, authorId = LZ_ID, plainText = "最新回复")
        val state = threadState(
            firstPost = firstPost,
            data = listOf(lzReply, normalReply),
            latestPosts = listOf(lzReply, latestLzReply),
        )

        val enhancement = state.toThreadEnhancementState()

        assertEquals(listOf(1L, 2L, 3L, 4L), enhancement.posts.map { it.postId })
        assertEquals(listOf(1L, 2L, 4L), enhancement.lzPosts.map { it.postId })
        assertEquals(2L, enhancement.posts.first { it.postId == 2L }.listKey)
        assertEquals(latestPostListKey(4), enhancement.posts.first { it.postId == 4L }.listKey)
    }

    @Test
    fun `descending projection keeps the first rendered latest-post target`() {
        val duplicate = post(id = 2, floor = 2, authorId = LZ_ID)
        val state = threadState(
            firstPost = post(id = 1, floor = 1, authorId = LZ_ID),
            data = listOf(duplicate),
            latestPosts = listOf(duplicate),
            sortType = ThreadSortType.BY_DESC,
        )

        val target = state.toThreadEnhancementState().posts.first { it.postId == 2L }

        assertEquals(latestPostListKey(2), target.listKey)
    }

    @Test
    fun `search matches title and text case-insensitively and treats regex characters literally`() {
        val state = threadState(
            firstPost = post(id = 1, floor = 1, authorId = LZ_ID, title = "Hello Title"),
            data = listOf(
                post(id = 2, floor = 2, authorId = 20, plainText = "中文内容"),
                post(id = 3, floor = 3, authorId = 30, plainText = "value.with[brackets]"),
            ),
        ).toThreadEnhancementState()

        assertEquals(listOf(1L), state.search("HELLO").map { it.postId })
        assertEquals(listOf(2L), state.search("中文").map { it.postId })
        assertEquals(listOf(3L), state.search("[").map { it.postId })
        assertEquals(listOf(3L), state.search(".").map { it.postId })
        assertTrue(state.search("   ").isEmpty())
    }

    @Test
    fun `blocked LZ post remains a redacted timeline placeholder and is excluded from search`() {
        val blockedPost = post(
            id = 2,
            floor = 2,
            authorId = LZ_ID,
            title = "不能泄漏的标题",
            plainText = "不能泄漏的正文",
            blocked = true,
        )
        val state = threadState(
            firstPost = post(id = 1, floor = 1, authorId = LZ_ID),
            data = listOf(blockedPost),
        ).toThreadEnhancementState()

        val redacted = state.lzPosts.first { it.postId == 2L }
        assertTrue(redacted.isBlocked)
        assertNull(redacted.title)
        assertEquals("", redacted.plainText)
        assertEquals("", redacted.previewText)
        assertFalse(state.search("不能泄漏").any { it.postId == 2L })
    }

    @Test
    fun `target lookup mirrors ascending thread item order`() {
        val state = threadState(
            firstPost = post(id = 1, floor = 1, authorId = LZ_ID),
            data = listOf(
                post(id = 2, floor = 2, authorId = 20),
                post(id = 3, floor = 3, authorId = 30),
            ),
            latestPosts = listOf(post(id = 4, floor = 4, authorId = 40)),
            hasPrevious = true,
        )

        assertEquals(0, state.threadListIndexOf(THREAD_FIRST_POST_LIST_KEY))
        assertEquals(3, state.threadListIndexOf(2L))
        assertEquals(4, state.threadListIndexOf(3L))
        assertEquals(6, state.threadListIndexOf(latestPostListKey(4)))
        assertNull(state.threadListIndexOf(999L))
    }

    @Test
    fun `target lookup mirrors descending thread item order`() {
        val state = threadState(
            firstPost = post(id = 1, floor = 1, authorId = LZ_ID),
            data = listOf(post(id = 2, floor = 2, authorId = 20)),
            latestPosts = listOf(
                post(id = 4, floor = 4, authorId = 40),
                post(id = 3, floor = 3, authorId = 30),
            ),
            sortType = ThreadSortType.BY_DESC,
            hasPrevious = true,
        )

        assertEquals(2, state.threadListIndexOf(latestPostListKey(4)))
        assertEquals(3, state.threadListIndexOf(latestPostListKey(3)))
        assertEquals(6, state.threadListIndexOf(2L))
    }

    private fun threadState(
        firstPost: PostData,
        data: List<PostData> = emptyList(),
        latestPosts: List<PostData>? = null,
        @ThreadSortType sortType: Int = ThreadSortType.DEFAULT,
        hasPrevious: Boolean = false,
    ) = ThreadUiState(
        firstPost = firstPost,
        thread = ThreadInfoData(
            id = 100,
            title = "Test thread",
            collectMarkPid = null,
            firstPostId = firstPost.id,
            like = LikeZero,
            originThreadInfo = null,
            replyNum = data.size,
            simpleForum = Triple(1, "test", null),
            pollInfo = null,
        ),
        data = data,
        latestPosts = latestPosts,
        sortType = sortType,
        pageData = PageData(hasPrevious = hasPrevious),
    )

    private fun post(
        id: Long,
        floor: Int,
        authorId: Long,
        title: String? = null,
        plainText: String = "",
        blocked: Boolean = false,
    ) = PostData(
        id = id,
        author = UserData(
            id = authorId,
            name = "user_$authorId",
            nameShow = "User $authorId",
            showBothName = false,
            avatarUrl = "",
            portrait = "",
            ip = "",
            levelId = 0,
            bawuType = null,
            isLz = authorId == LZ_ID,
        ),
        floor = floor,
        title = title,
        time = 0,
        like = LikeZero,
        blocked = blocked,
        plainText = plainText,
        contentRenders = emptyList(),
        subPosts = null,
        subPostNumber = 0,
    )

    private companion object {
        const val LZ_ID = 10L
    }
}
