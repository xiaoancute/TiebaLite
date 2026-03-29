package com.huanchengfly.tieba.post.ui.page.reading

import com.google.gson.Gson
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.models.ThreadHistoryInfoBean
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.utils.HistoryUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReadingWorkbenchTargetsTest {
    @Test
    fun `build history reading target keeps thread forum metadata from extras`() {
        val result = buildHistoryReadingTargetCandidate(
            History(
                title = "今晚发布会",
                data = "12345",
                type = HistoryUtil.TYPE_THREAD,
                username = "楼主A",
                avatar = "avatar://thread",
                extras = Gson().toJson(ThreadHistoryInfoBean(forumName = "数码吧")),
            )
        ) as ReadingWorkbenchTargetCandidate.Thread

        assertEquals(12345L, result.threadId)
        assertEquals("今晚发布会", result.title)
        assertEquals("数码吧", result.forumName)
        assertEquals("楼主A", result.username)
        assertEquals("avatar://thread", result.avatar)
    }

    @Test
    fun `build history reading target drops invalid history payload`() {
        val result = buildHistoryReadingTargetCandidate(
            History(
                title = "坏数据",
                data = "not-a-thread-id",
                type = HistoryUtil.TYPE_THREAD,
            )
        )

        assertNull(result)
    }

    @Test
    fun `build search thread reading target falls back to nested titles`() {
        val result = buildSearchThreadReadingTargetCandidate(
            SearchThreadBean.ThreadInfoBean(
                tid = "67890",
                pid = "1",
                title = "",
                content = "搜索摘要",
                time = "1710000000",
                modifiedTime = 1L,
                postNum = "3",
                likeNum = "4",
                shareNum = "5",
                forumId = "99",
                forumName = "动漫吧",
                user = SearchThreadBean.UserInfoBean(
                    userName = "吧友甲",
                    showNickname = "吧友甲",
                    userId = "100",
                    portrait = "tb.1.demo",
                ),
                type = 0,
                forumInfo = SearchThreadBean.ForumInfo(
                    forumName = "动漫吧",
                    avatar = "forum-avatar"
                ),
                mainPost = SearchThreadBean.MainPost(
                    title = "主贴标题",
                    content = "主贴内容",
                    tid = 67890L,
                    user = SearchThreadBean.UserInfoBean(
                        userName = "吧友甲",
                        showNickname = "吧友甲",
                        userId = "100",
                        portrait = "tb.1.demo",
                    ),
                    likeNum = "1",
                    shareNum = "2",
                    postNum = "3",
                ),
            )
        ) as ReadingWorkbenchTargetCandidate.Thread

        assertEquals(67890L, result.threadId)
        assertEquals("主贴标题", result.title)
        assertEquals("动漫吧", result.forumName)
    }

    @Test
    fun `build search forum reading target requires usable forum name`() {
        val result = buildSearchForumReadingTargetCandidate(
            SearchForumBean.ForumInfoBean(
                forumName = "   ",
                avatar = "forum-avatar",
            )
        )

        assertNull(result)
    }
}
