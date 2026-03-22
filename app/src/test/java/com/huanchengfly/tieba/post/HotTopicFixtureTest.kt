package com.huanchengfly.tieba.post

import com.huanchengfly.tieba.post.api.models.TopicDetailBean
import com.huanchengfly.tieba.post.api.models.web.HotMessageListBean
import com.huanchengfly.tieba.post.utils.GsonUtil
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HotTopicFixtureTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun hotTopicListPayloadKeepsMulIdCompatibilityOffline() {
        val payload = loadFixture("fixtures/hot-topic-list-2026-03.json")
        val root = json.parseToJsonElement(payload).jsonObject

        assertEquals(0, root.int("no"))
        val topics = root.requireObject("data").requireObject("list").requireArray("ret")
        assertTrue("hot topic fixture should include at least two topics", topics.size >= 2)
        val firstTopic = topics.first().jsonObject
        assertEquals("28352154", firstTopic.string("mul_id"))
        assertEquals("G2虐爆全场!GEN惨遭碾压", firstTopic.string("mul_name"))
        assertTrue(
            "topic_info should keep current descriptions",
            firstTopic.requireObject("topic_info").string("topic_desc").contains("GEN")
        )

        val bean = GsonUtil.getGson().fromJson(payload, HotMessageListBean::class.java)
        assertEquals(0, bean.errorCode)
        assertEquals("28352154", bean.data.list.ret.first().mulId)
        assertEquals("G2虐爆全场!GEN惨遭碾压", bean.data.list.ret.first().mulName)
        assertTrue(bean.data.list.ret.first().topicInfo.topicDesc.contains("GEN"))
    }

    @Test
    fun topicDetailPayloadParsesOffline() {
        val payload = loadFixture("fixtures/topic-detail-2026-03.json")
        val root = json.parseToJsonElement(payload).jsonObject

        assertEquals(0, root.int("no"))
        assertEquals("success", root.string("error"))
        val data = root.requireObject("data")
        assertEquals("28352154", data.requireObject("topic_info").string("topic_id"))
        assertTrue("topic detail should keep tbs", data.string("tbs").isNotBlank())
        assertTrue("related forums should remain reachable", data.requireArray("relate_forum").isNotEmpty())
        assertEquals(
            0,
            data.requireObject("relate_thread").requireArray("thread_list").size
        )

        val bean = json.decodeFromString<TopicDetailBean>(payload)
        assertEquals("28352154", bean.data.topicInfo.topicId)
        assertEquals("G2虐爆全场!GEN惨遭碾压", bean.data.topicInfo.topicName)
        assertEquals("13075092", bean.data.topicInfo.discussNum)
        assertTrue(bean.data.topicInfo.topicDesc.contains("GEN"))
        assertTrue(bean.data.tbs.isNotBlank())
        assertEquals("抗压背锅", bean.data.relateForum.first().forumName)
        assertTrue(bean.data.relateThread.threadList.isEmpty())
        assertTrue(bean.data.hasMore)
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
