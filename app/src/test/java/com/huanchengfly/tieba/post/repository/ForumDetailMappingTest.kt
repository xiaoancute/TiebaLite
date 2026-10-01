package com.huanchengfly.tieba.post.repository

import com.google.gson.Gson
import com.huanchengfly.tieba.post.api.models.ForumOverviewResponse
import com.huanchengfly.tieba.post.api.models.protos.BawuRoleDes
import com.huanchengfly.tieba.post.api.models.protos.BawuRoleInfoPub
import com.huanchengfly.tieba.post.api.models.protos.BawuTeam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForumDetailMappingTest {
    @Test
    fun `overview accepts string counters and keeps full description and friendship order`() {
        val response = Gson().fromJson(
            """{
                "error_code": "0",
                "forum": {
                    "id": "123", "name": "minecraft", "slogan": "标语",
                    "desc": "第一段\n第二段", "member_num": "42", "post_num": "3000000000"
                },
                "friend_forum": [
                    {"forum_id": "1", "forum_name": "terraria", "avatar": "https://example.com/a.jpg"},
                    {"forum_id": 2, "forum_name": "bilibili"},
                    {"forum_id": 1, "forum_name": " terraria "},
                    {"forum_id": 3, "forum_name": null}
                ]
            }""",
            ForumOverviewResponse::class.java,
        )
        val detail = response.toForumDetail("fallback")
        assertEquals(123L, detail.id)
        assertEquals("第一段\n第二段", detail.intro)
        assertEquals(3_000_000_000L, detail.postCount)
        assertEquals(listOf("terraria", "bilibili"), detail.friendForums.map { it.name })
        assertEquals("", detail.friendForums[1].avatar)
    }

    @Test
    fun `missing optional overview fields do not hide basic information`() {
        val response = Gson().fromJson(
            """{"forum":{"id":123,"name":null,"slogan":null},"friend_forum":null}""",
            ForumOverviewResponse::class.java,
        )
        val detail = response.toForumDetail("minecraft")
        assertEquals("minecraft", detail.name)
        assertEquals("", detail.slogan)
        assertTrue(detail.friendForums.isEmpty())
    }

    @Test
    fun `team preserves all roles and falls back to username for missing nickname`() {
        val admin = BawuRoleInfoPub(user_id = 1, name_show = "吧主甲", user_name = "admin")
        val assistant = BawuRoleInfoPub(user_id = 2, user_name = "小吧乙", portrait = "portrait")
        val team = BawuTeam(bawu_team_list = listOf(
            BawuRoleDes(role_name = "吧主", role_info = listOf(admin, admin)),
            BawuRoleDes(role_name = "小吧主", role_info = listOf(assistant)),
            BawuRoleDes(role_name = "图片小编", role_info = listOf(admin)),
            BawuRoleDes(role_name = "视频小编"),
        ))
        val groups = team.toManagerGroups()
        assertEquals(listOf("吧主", "小吧主", "图片小编"), groups.map { it.name })
        assertEquals(listOf(1L), groups[0].members.map { it.id })
        assertEquals("小吧乙", groups[1].members.single().name)
        assertTrue(groups[1].members.single().avatarUrl.endsWith("/portrait"))
        assertEquals(1L, groups[2].members.single().id)
    }
}
