package com.huanchengfly.tieba.post.repository

object WaterPostBlocker {
    private val exactWaterTexts = setOf(
        "+1",
        "+2",
        "+3",
        "dd",
        "cy",
        "cy一下",
        "mark",
        "mark一下",
        "up",
        "m",
        "顶",
        "顶一下",
        "顶顶",
        "蹲",
        "蹲蹲",
        "蹲一下",
        "插眼",
        "插个眼",
        "码住",
        "码",
        "马克",
        "马克一下",
        "留名",
        "留爪",
        "路过",
        "前排",
        "前排围观",
        "坐等",
        "围观",
        "吃瓜",
        "收藏",
        "收藏了",
        "已收藏",
        "水贴",
        "水帖",
        "经验",
        "经验+3",
        "经验 +3",
    )

    private val compactNoiseRegex = Regex("""^(?:\+\d+|[+.。!！?？,，~～_-]+)$""")
    private val repeatedCharRegex = Regex("""^(.)\1{3,}$""")
    private val repeatedWaterTokenRegex = Regex("""^(?:顶|蹲|码|看|m|mark|马克|cy|dd){2,}$""")

    fun isWaterPost(vararg contents: String): Boolean {
        return contents.any(::isWaterPost)
    }

    fun isWaterPost(content: String): Boolean {
        val text = content.trim()
        if (text.isEmpty() || text.length > MAX_WATER_TEXT_LENGTH) return false

        val compact = text
            .lowercase()
            .replace(Regex("""\s+"""), "")

        if (compact in exactWaterTexts) return true
        if (compact.matches(compactNoiseRegex) && compact.length >= 2) return true
        if (compact.matches(repeatedCharRegex)) return true
        if (compact.matches(repeatedWaterTokenRegex)) return true

        return false
    }
}

private const val MAX_WATER_TEXT_LENGTH = 12
