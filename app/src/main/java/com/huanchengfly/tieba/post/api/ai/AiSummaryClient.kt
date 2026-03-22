package com.huanchengfly.tieba.post.api.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class PostText(
    val floor: Int,
    val author: String,
    val content: String,
)

object AiSummaryClient {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private const val SYSTEM_PROMPT =
        "你是一个帖子总结助手。请根据以下贴吧帖子内容生成简洁的中文摘要，包括：\n" +
                "1. 帖子主题\n" +
                "2. 主要观点和讨论方向\n" +
                "3. 关键结论或共识（如有）\n" +
                "控制在 200 字以内。"

    private const val MAX_CONTENT_LENGTH = 4000

    @Serializable
    private data class ChatMessage(
        val role: String,
        val content: String,
    )

    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<ChatMessage>,
        val temperature: Float = 0.3f,
        val max_tokens: Int = 512,
    )

    fun buildPostsContent(title: String, posts: List<PostText>): String {
        val sb = StringBuilder()
        sb.appendLine("帖子标题：$title")
        sb.appendLine()
        for (post in posts) {
            val line = "${post.floor}楼 [${post.author}]：${post.content}"
            if (sb.length + line.length > MAX_CONTENT_LENGTH) break
            sb.appendLine(line)
        }
        return sb.toString()
    }

    suspend fun summarize(
        baseUrl: String,
        apiKey: String,
        model: String,
        title: String,
        posts: List<PostText>,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val userContent = buildPostsContent(title, posts)
            val request = ChatRequest(
                model = model,
                messages = listOf(
                    ChatMessage("system", SYSTEM_PROMPT),
                    ChatMessage("user", userContent),
                ),
            )
            val requestJson = json.encodeToString(ChatRequest.serializer(), request)

            val url = baseUrl.trimEnd('/') + "/v1/chat/completions"
            val httpRequest = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(httpRequest).execute()
            if (!response.isSuccessful) {
                throw RuntimeException("HTTP ${response.code}: ${response.body?.string()?.take(200)}")
            }

            val body = response.body?.string()
                ?: throw RuntimeException("Empty response body")
            val jsonElement = json.parseToJsonElement(body)
            jsonElement.jsonObject["choices"]!!
                .jsonArray[0]
                .jsonObject["message"]!!
                .jsonObject["content"]!!
                .jsonPrimitive.content
        }
    }

    suspend fun testConnection(
        baseUrl: String,
        apiKey: String,
        model: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = ChatRequest(
                model = model,
                messages = listOf(
                    ChatMessage("user", "Hi, reply with OK"),
                ),
                max_tokens = 10,
            )
            val requestJson = json.encodeToString(ChatRequest.serializer(), request)

            val url = baseUrl.trimEnd('/') + "/v1/chat/completions"
            val httpRequest = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(httpRequest).execute()
            if (!response.isSuccessful) {
                throw RuntimeException("HTTP ${response.code}: ${response.body?.string()?.take(200)}")
            }
            "OK"
        }
    }
}
