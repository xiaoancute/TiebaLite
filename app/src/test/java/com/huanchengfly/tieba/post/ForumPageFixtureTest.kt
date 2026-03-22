package com.huanchengfly.tieba.post

import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.utils.GsonUtil
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForumPageFixtureTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun signedMiniForumPayloadParsesOffline() {
        val payload = loadFixture("fixtures/forum-page-signed-2026-03.json")
        val root = json.parseToJsonElement(payload).jsonObject

        assertEquals(0, root.int("error_code"))
        assertEquals("原神", root.requireObject("forum").string("name"))
        assertTrue("fixture should include forum threads", root.requireArray("thread_list").isNotEmpty())
        assertTrue("fixture should include forum users", root.requireArray("user_list").isNotEmpty())
        assertTrue(
            "fixture should keep the current tab layout",
            root.requireArray("frs_tab_info").any {
                it.jsonObject["tab_name"]?.jsonPrimitive?.content == "看贴"
            }
        )

        val commonInfo = json.parseToJsonElement(root.string("frs_common_info")).jsonObject
        assertEquals("原神", commonInfo.requireObject("forum").string("name"))
        assertTrue(
            "frs_common_info should still carry moderation reasons",
            commonInfo.requireObject("anti").requireArray("del_thread_text").isNotEmpty()
        )

        val forumPageBean = GsonUtil.getGson().fromJson(payload, ForumPageBean::class.java)
        assertEquals("原神", forumPageBean.forum?.name)
        assertEquals("1", forumPageBean.page?.currentPage)
        assertEquals("0", forumPageBean.anti?.ifPost)
        assertTrue("forum tbs should be present", !forumPageBean.anti?.tbs.isNullOrBlank())

        val threadList = forumPageBean.threadList.orEmpty()
        assertTrue("parsed thread list should keep two sample threads", threadList.size >= 2)
        val firstThread = threadList.first()
        assertEquals("原神版本预言家内容征集大赛——第一届", firstThread.title)
        assertTrue(
            "first thread should keep abstract text",
            firstThread.getAbstractString().orEmpty().contains("活动时间")
        )

        val secondThread = threadList[1]
        assertTrue("second thread should keep media entries", secondThread.media.orEmpty().isNotEmpty())
        assertEquals("3", secondThread.media.orEmpty().first().type)
        assertTrue(
            "second thread should keep text abstracts",
            secondThread.getAbstractString().orEmpty().contains("复刻")
        )

        val authorMap = forumPageBean.userList.orEmpty().associateBy { it.id }
        assertEquals("贴吧用户_0895PyS", authorMap[firstThread.authorId]?.nameShow)
        assertEquals("张恒三代", authorMap[secondThread.authorId]?.nameShow)
    }

    private fun loadFixture(path: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(path)) {
            "missing test fixture: $path"
        }.bufferedReader().use { it.readText() }

    private fun JsonObject.requireObject(key: String): JsonObject =
        this[key]?.jsonObject ?: error("missing object: $key in ${toString().take(500)}")

    private fun JsonObject.requireArray(key: String) =
        this[key]?.jsonArray ?: error("missing array: $key in ${toString().take(500)}")

    private fun JsonObject.string(key: String): String =
        this[key]?.jsonPrimitive?.content ?: error("missing string: $key in ${toString().take(500)}")

    private fun JsonObject.int(key: String): Int =
        this[key]?.jsonPrimitive?.content?.toIntOrNull()
            ?: error("missing int: $key in ${toString().take(500)}")
}
