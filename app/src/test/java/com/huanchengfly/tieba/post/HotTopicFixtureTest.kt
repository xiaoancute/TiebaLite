package com.huanchengfly.tieba.post

import com.huanchengfly.tieba.post.api.models.TopicDetailBean
import com.huanchengfly.tieba.post.api.models.web.HotMessageListBean
import com.huanchengfly.tieba.post.revival.PublicBrowsePayloadGuard
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

        val payloadData = PublicBrowsePayloadGuard.requireTopicDetailPayload(
            json.decodeFromString<TopicDetailBean>(payload)
        )
        assertEquals("28352154", payloadData.topicInfo.topicId)
        assertEquals("G2虐爆全场!GEN惨遭碾压", payloadData.topicInfo.topicName)
        assertEquals("13075092", payloadData.topicInfo.discussNum)
        assertTrue(payloadData.topicInfo.topicDesc.contains("GEN"))
        assertEquals("抗压背锅", payloadData.relatedForums.first().forumName)
        assertTrue(payloadData.relatedThreads.isEmpty())
        assertTrue(payloadData.hasMore)
    }

    @Test
    fun topicDetailPayloadParsesTopicRankIndexWhenPresent() {
        val payload = """
            {
              "no": 0,
              "error": "success",
              "data": {
                "topic_info": {
                  "topic_id": "1",
                  "topic_name": "测试话题",
                  "candle": "0",
                  "topic_desc": "desc",
                  "discuss_num": 42,
                  "topic_image": "",
                  "share_title": "",
                  "share_pic": "",
                  "is_video_topic": 0,
                  "idx_num": 7
                },
                "user": {
                  "is_login": false,
                  "id": 0,
                  "uid": 0,
                  "name": "",
                  "name_show": "",
                  "portrait": ""
                },
                "tbs": "",
                "relate_forum": [],
                "special_topic": [],
                "relate_thread": {
                  "thread_list": []
                },
                "has_more": false
              }
            }
        """.trimIndent()

        val bean = json.decodeFromString<TopicDetailBean>(payload)

        assertEquals(7, bean.data?.topicInfo?.idxNum)
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
