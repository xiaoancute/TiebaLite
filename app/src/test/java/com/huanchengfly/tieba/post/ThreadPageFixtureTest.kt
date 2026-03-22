package com.huanchengfly.tieba.post

import com.huanchengfly.tieba.post.api.models.ThreadContentBean
import com.huanchengfly.tieba.post.utils.GsonUtil
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreadPageFixtureTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun signedThreadPayloadParsesOffline() {
        val payload = loadFixture("fixtures/thread-page-signed-2026-03.json")
        val root = json.parseToJsonElement(payload).jsonObject

        assertEquals(0, root.int("error_code"))
        assertEquals("原神", root.requireObject("forum").string("name"))
        assertTrue("thread fixture should carry anti tbs", root.requireObject("anti").string("tbs").isNotBlank())
        assertEquals(
            "原神版本预言家内容征集大赛——第一届",
            root.requireObject("thread").string("title")
        )

        val posts = root.requireArray("post_list")
        assertTrue("thread fixture should include at least two posts", posts.size >= 2)
        assertEquals("0", posts[0].jsonObject.requireArray("content")[0].jsonObject.string("type"))
        assertTrue(
            "reply payload should keep tieba links",
            posts[1].jsonObject.requireArray("content")[0].jsonObject.string("link")
                .contains("tieba.baidu.com/p/")
        )

        val threadBean = GsonUtil.getGson().fromJson(payload, ThreadContentBean::class.java)
        assertEquals("原神", threadBean.forum?.name)
        assertEquals("1", threadBean.page?.currentPage)
        assertTrue("thread tbs should be present", !threadBean.anti?.tbs.isNullOrBlank())
        assertEquals("贴吧游戏", threadBean.thread?.author?.name)

        val postList = threadBean.postList.orEmpty()
        assertTrue("parsed post list should keep two sample posts", postList.size >= 2)
        val firstPost = postList.first()
        assertEquals("1", firstPost.floor)
        assertTrue(
            "first post should keep activity body text",
            firstPost.content.orEmpty().firstOrNull()?.text.orEmpty().contains("活动详情如下")
        )

        val secondPost = postList[1]
        val replyContent = secondPost.content.orEmpty().firstOrNull()
        assertEquals("2", secondPost.floor)
        assertEquals("1", replyContent?.type)
        assertTrue(
            "reply post should keep in-app tieba jump links",
            replyContent?.link.orEmpty().contains("tieba.baidu.com/p/10547704116")
        )

        val authorMap = threadBean.userList.orEmpty().associateBy { it.id }
        assertEquals("贴吧游戏", authorMap[firstPost.authorId]?.nameShow)
        assertEquals("剑客长弓", authorMap[secondPost.authorId]?.nameShow)
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
