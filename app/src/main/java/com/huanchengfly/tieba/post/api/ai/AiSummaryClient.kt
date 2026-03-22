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
        "你是一个帖子总结助手。请根据以下贴吧帖子内容，按以下四个维度生成结构化中文摘要：\n\n" +
                "## 楼主观点\n" +
                "概括楼主的核心表达和立场。\n\n" +
                "## 争议焦点\n" +
                "如果存在分歧或争论，列出主要争议点；如果没有争议，说明整体倾向。\n\n" +
                "## 讨论脉络\n" +
                "按讨论发展顺序，概括关键转折和重要回复。\n\n" +
                "## 结论\n" +
                "总结讨论的最终走向或共识。\n\n" +
                "每个维度控制在 2-3 句话，总体不超过 400 字。使用上述 ## 标题格式输出。"

    private const val MAX_CONTENT_LENGTH = 6000

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
        val max_tokens: Int = 1024,
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
