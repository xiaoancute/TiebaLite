package com.huanchengfly.tieba.post

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest
import java.time.Duration
import java.time.LocalDate

class PublicBrowseLiveSmokeTest {
    private val client = OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(30))
        .connectTimeout(Duration.ofSeconds(30))
        .readTimeout(Duration.ofSeconds(30))
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun searchAndTopicPublicEndpointsStayReadable() {
        val searchForum = getJson(
            "https://tieba.baidu.com/mo/q/search/forum?word=${urlEncode(SAMPLE_FORUM)}"
        )
        assertEquals("success", searchForum.string("error"))
        assertEquals(0, searchForum.int("no"))
        assertEquals(
            SAMPLE_FORUM,
            searchForum.requireObject("data")
                .requireObject("exactMatch")
                .string("forum_name")
        )

        val searchThread = getJson(
            "https://tieba.baidu.com/mo/q/search/thread?word=${urlEncode(SAMPLE_FORUM)}&pn=1&st=0&tt=1&ct=1"
        )
        assertEquals("success", searchThread.string("error"))
        assertEquals(0, searchThread.int("no"))
        assertTrue(
            "search thread should return at least one post",
            searchThread.requireObject("data").requireArray("post_list").isNotEmpty()
        )

        val topicList = getJson("https://tieba.baidu.com/mo/q/hotMessage/list?fr=newwise")
        assertEquals(0, topicList.int("no"))
        val topics = topicList["data"]?.jsonObject
            ?.get("list")?.jsonObject
            ?.get("ret")?.jsonArray
            ?: findFirstNonEmptyArray(topicList, "topic_list", "topicList")
        assertTrue("topic list should not be empty", topics.isNotEmpty())
        val firstTopic = topics.first().jsonObject
        val nestedTopicInfo = firstTopic["topic_info"]?.jsonObject
        val topicId = firstTopic.stringOrNull("mul_id")
            ?: nestedTopicInfo?.stringOrNull("topic_id")
            ?: firstTopic.stringOrNull("topic_id")
            ?: firstTopic.stringOrNull("topicId")
            ?: error("topic_id missing in hot topic payload")
        val topicName = firstTopic.stringOrNull("mul_name")
            ?: nestedTopicInfo?.stringOrNull("topic_name")
            ?: firstTopic.stringOrNull("topic_name")
            ?: firstTopic.stringOrNull("topicName")
            ?: error("topic_name missing in hot topic payload")

        val topicDetail = getJson(
            "https://tieba.baidu.com/mo/q/newtopic/topicDetail?" +
                "topic_id=${urlEncode(topicId)}&topic_name=${urlEncode(topicName)}&is_new=0&is_share=1&pn=1&rn=10&offset=0&derivative_to_pic_id="
        )
        assertEquals("success", topicDetail.string("error"))
        assertEquals(0, topicDetail.int("no"))
        assertEquals(
            topicId,
            topicDetail.requireObject("data").requireObject("topic_info").string("topic_id")
        )
    }

    @Test
    fun signedForumAndThreadEndpointsStayReadable() {
        val forumPage = postSignedMiniForumPage(SAMPLE_FORUM)
        assertTrue(
            "forum page should not report an error: ${forumPage.compact()}",
            forumPage.intOrNull("error_code") == null || forumPage.int("error_code") == 0
        )
        assertEquals(1, forumPage.requireObject("page").int("current_page"))
        assertTrue(
            "forum payload should include forum metadata",
            forumPage.string("frs_common_info").contains("\"name\":\"$SAMPLE_FORUM\"")
        )

        val threadId = getJson(
            "https://tieba.baidu.com/mo/q/search/thread?word=${urlEncode(SAMPLE_FORUM)}&pn=1&st=0&tt=1&ct=1"
        ).requireObject("data").requireArray("post_list").first().jsonObject.string("tid")

        val threadPage = postSignedOfficialThreadPage(threadId)
        assertEquals(0, threadPage.int("error_code"))
        assertTrue(
            "thread page should include forum data",
            threadPage.requireObject("forum").string("name").isNotBlank()
        )
        assertTrue(
            "thread page should include anti data",
            threadPage.requireObject("anti").string("tbs").isNotBlank()
        )
        assertTrue(
            "thread page should include post list",
            threadPage.requireArray("post_list").isNotEmpty()
        )
    }

    private fun getJson(url: String): JsonObject =
        execute(
            Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", WEB_USER_AGENT)
                .build()
        )

    private fun postSignedMiniForumPage(forumName: String): JsonObject {
        val timestamp = System.currentTimeMillis().toString()
        val params = linkedMapOf(
            "_client_type" to "2",
            "_client_version" to "7.2.0.0",
            "_os_version" to "34",
            "_phone_imei" to MINI_IMEI,
            "cuid" to MINI_CUID,
            "cuid_galaxy2" to MINI_CUID,
            "from" to "1021636m",
            "kw" to forumName,
            "model" to DEVICE_MODEL,
            "net_type" to "1",
            "pn" to "1",
            "q_type" to "2",
            "rn" to "20",
            "scr_dip" to "3.0",
            "scr_h" to "2400",
            "scr_w" to "1080",
            "sort_type" to "1",
            "stErrorNums" to "1",
            "stMethod" to "1",
            "stMode" to "1",
            "stSize" to "600",
            "stTime" to "200",
            "stTimesNum" to "1",
            "st_type" to "tb_forumlist",
            "subapp_type" to "mini",
            "timestamp" to timestamp,
            "with_group" to "0",
        )
        return execute(
            Request.Builder()
                .url("https://c.tieba.baidu.com/c/f/frs/page")
                .post(buildSignedForm(params))
                .header("User-Agent", "bdtb for Android 7.2.0.0")
                .header("Charset", "UTF-8")
                .header("client_type", "2")
                .header("Pragma", "no-cache")
                .header("Cookie", "ka=open")
                .header("cuid", MINI_CUID)
                .header("cuid_galaxy2", MINI_CUID)
                .build()
        )
    }

    private fun postSignedOfficialThreadPage(threadId: String): JsonObject {
        val timestamp = System.currentTimeMillis().toString()
        val params = linkedMapOf(
            "_client_type" to "2",
            "_client_version" to "12.25.1.0",
            "_os_version" to "34",
            "_phone_imei" to MINI_IMEI,
            "active_timestamp" to "1774100000",
            "android_id" to "MDAwMDAwMDAwMDAwMDAw",
            "back" to "0",
            "brand" to "google",
            "c3_aid" to "A00-DUMMY-AID",
            "cmode" to "1",
            "cuid" to MINI_CUID,
            "cuid_galaxy2" to MINI_CUID,
            "cuid_gid" to "",
            "event_day" to currentEventDay(),
            "extra" to "",
            "first_install_time" to "1700000000000",
            "floor_rn" to "3",
            "framework_ver" to "3340042",
            "from" to "tieba",
            "is_teenager" to "0",
            "kz" to threadId,
            "last_update_time" to "1700000000000",
            "lz" to "0",
            "mac" to "02:00:00:00:00:00",
            "mark" to "0",
            "model" to DEVICE_MODEL,
            "net_type" to "1",
            "pn" to "1",
            "rn" to "30",
            "scr_dip" to "3.0",
            "scr_h" to "2400",
            "scr_w" to "1080",
            "sdk_ver" to "2.34.0",
            "start_scheme" to "",
            "start_type" to "1",
            "stErrorNums" to "1",
            "stMethod" to "1",
            "stMode" to "1",
            "stSize" to "600",
            "stTime" to "200",
            "stTimesNum" to "1",
            "st_type" to "tb_frslist",
            "swan_game_ver" to "1038000",
            "timestamp" to timestamp,
            "with_floor" to "1",
        )
        return execute(
            Request.Builder()
                .url("https://c.tieba.baidu.com/c/f/pb/page")
                .post(buildSignedForm(params))
                .header("User-Agent", "bdtb for Android 12.25.1.0")
                .header("Charset", "UTF-8")
                .header("client_type", "2")
                .header("Pragma", "no-cache")
                .header(
                    "Cookie",
                    "CUID=$MINI_CUID;ka=open;TBBRAND=$DEVICE_MODEL;BAIDUID=dummybaiduid;"
                )
                .header("cuid", MINI_CUID)
                .header("cuid_galaxy2", MINI_CUID)
                .header("cuid_gid", "")
                .header("c3_aid", "A00-DUMMY-AID")
                .header("client_logid", "1774100000")
                .build()
        )
    }

    private fun buildSignedForm(params: Map<String, String>): FormBody {
        val signInput = params.entries
            .map { "${it.key}=${it.value}" }
            .sorted()
            .joinToString(separator = "") +
            SIGN_SECRET
        val sign = signInput.md5()
        return FormBody.Builder().apply {
            params.forEach { (key, value) -> add(key, value) }
            add("sign", sign)
        }.build()
    }

    private fun execute(request: Request): JsonObject {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            assertTrue(
                "HTTP ${response.code} for ${request.url}: ${body.take(300)}",
                response.isSuccessful
            )
            return json.parseToJsonElement(body).jsonObject
        }
    }

    private fun findFirstNonEmptyArray(root: JsonObject, vararg keys: String): JsonArray {
        val queue = ArrayDeque<JsonElement>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            when (val element = queue.removeFirst()) {
                is JsonObject -> {
                    keys.forEach { key ->
                        val value = element[key]
                        if (value is JsonArray && value.isNotEmpty()) {
                            return value
                        }
                    }
                    element.values.forEach(queue::addLast)
                }

                is JsonArray -> element.forEach(queue::addLast)
                else -> Unit
            }
        }
        return JsonArray(emptyList())
    }

    private fun JsonObject.requireObject(key: String): JsonObject =
        this[key]?.jsonObject ?: error("missing object: $key in ${compact()}")

    private fun JsonObject.requireArray(key: String): JsonArray =
        this[key]?.jsonArray ?: error("missing array: $key in ${compact()}")

    private fun JsonObject.string(key: String): String =
        this[key]?.jsonPrimitive?.content ?: error("missing string: $key in ${compact()}")

    private fun JsonObject.stringOrNull(key: String): String? =
        this[key]?.jsonPrimitive?.content

    private fun JsonObject.int(key: String): Int =
        this[key]?.jsonPrimitive?.content?.toIntOrNull()
            ?: error("missing int: $key in ${compact()}")

    private fun JsonObject.intOrNull(key: String): Int? =
        this[key]?.jsonPrimitive?.content?.toIntOrNull()

    private fun JsonObject.compact(): String = toString().take(500)

    private fun String.md5(): String =
        MessageDigest.getInstance("MD5")
            .digest(toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun urlEncode(value: String): String = java.net.URLEncoder.encode(value, "UTF-8")

    private fun currentEventDay(): String {
        val now = LocalDate.now()
        return "${now.year}${now.monthValue}${"%02d".format(now.dayOfMonth)}"
    }

    companion object {
        private const val WEB_USER_AGENT = "Mozilla/5.0"
        private const val SAMPLE_FORUM = "原神"
        private const val DEVICE_MODEL = "Pixel 7"
        private const val MINI_IMEI = "864100000000000"
        private const val MINI_CUID = "A1B2C3D4E5F60718293A4B5C6D7E8F90|000000000000001"
        private const val SIGN_SECRET = "tiebaclient!!!"
    }
}
