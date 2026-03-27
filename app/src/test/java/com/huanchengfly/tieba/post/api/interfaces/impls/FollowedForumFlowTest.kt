package com.huanchengfly.tieba.post.api.interfaces.impls

import com.huanchengfly.tieba.post.api.models.protos.forumGuide.LikeForum
import com.huanchengfly.tieba.post.api.models.web.ForumHomeData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FollowedForumFlowTest {
    @Test
    fun collectAllForumHomeItemsRequestsEveryPageUntilTheLastPage() = runBlocking {
        val requestedPages = mutableListOf<Int>()

        val items = collectAllForumHomeItems { page: Int ->
            requestedPages.add(page)
            when (page) {
                0 -> forumHomePage(
                    currentPage = 0,
                    totalPage = 3,
                    forums = listOf(homeForum(forumId = 10L), homeForum(forumId = 20L))
                )
                1 -> forumHomePage(
                    currentPage = 1,
                    totalPage = 3,
                    forums = listOf(homeForum(forumId = 30L))
                )
                2 -> forumHomePage(
                    currentPage = 2,
                    totalPage = 3,
                    forums = listOf(homeForum(forumId = 40L))
                )
                else -> error("unexpected page $page")
            }
        }

        assertEquals(listOf(0, 1, 2), requestedPages)
        assertEquals(listOf(10L, 20L, 30L, 40L), items.map { it.forumId })
    }

    @Test
    fun mergeFollowedForumsKeepsPagedOrderAndOverlaysGuideDetails() {
        val merged = mergeFollowedForums(
            pagedForums = listOf(
                homeForum(forumId = 10L, forumName = "Home 10", avatar = "home-10", levelId = 1, hotNum = 11),
                homeForum(forumId = 20L, forumName = "Home 20", avatar = "home-20", levelId = 2, hotNum = 22),
                homeForum(forumId = 30L, forumName = "Home 30", avatar = "home-30", levelId = 3, hotNum = 33),
            ),
            guideForums = listOf(
                guideForum(forumId = 20L, forumName = "Guide 20", avatar = "guide-20", levelId = 8, hotNum = 88, isSign = 1),
                guideForum(forumId = 40L, forumName = "Guide 40", avatar = "guide-40", levelId = 9, hotNum = 99, isSign = 0),
            )
        )

        assertEquals(listOf(10L, 20L, 30L, 40L), merged.map { it.forumId })
        assertEquals(false, merged[0].isSign)
        assertEquals("home-10", merged[0].avatar)
        assertEquals("Guide 20", merged[1].forumName)
        assertEquals("guide-20", merged[1].avatar)
        assertEquals(true, merged[1].isSign)
        assertEquals(8, merged[1].levelId)
        assertEquals(88, merged[1].hotNum)
        assertEquals("Guide 40", merged[3].forumName)
    }

    private fun forumHomePage(
        currentPage: Int,
        totalPage: Int,
        forums: List<ForumHomeData.LikeForum.ListItem>,
    ) = ForumHomeData.LikeForum(
        list = forums,
        page = ForumHomeData.LikeForum.Page(
            currentPage = currentPage,
            totalPage = totalPage
        )
    )

    private fun homeForum(
        forumId: Long,
        forumName: String = "Forum $forumId",
        avatar: String = "avatar-$forumId",
        levelId: Int = 1,
        hotNum: Long = 10L,
    ) = ForumHomeData.LikeForum.ListItem(
        avatar = avatar,
        forumId = forumId,
        forumName = forumName,
        hotNum = hotNum,
        isBrandForum = 0,
        levelId = levelId
    )

    private fun guideForum(
        forumId: Long,
        forumName: String,
        avatar: String,
        levelId: Int,
        hotNum: Int,
        isSign: Int,
    ) = LikeForum(
        forum_id = forumId,
        forum_name = forumName,
        avatar = avatar,
        hot_num = hotNum,
        level_id = levelId,
        is_sign = isSign
    )
}
