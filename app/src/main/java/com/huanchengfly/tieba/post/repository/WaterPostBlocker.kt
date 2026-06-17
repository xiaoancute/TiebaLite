package com.huanchengfly.tieba.post.repository

object WaterPostBlocker {
    private val exactWaterTexts = setOf(
        "+1",
        "+2",
        "+3",
        "dd",
        "cy",
        "mark",
        "up",
        "顶",
        "蹲",
        "插眼",
        "码住",
        "马克",
        "留名",
        "路过",
        "水贴",
        "水帖",
        "经验",
        "经验+3",
        "经验 +3",
    )

    private val compactNoiseRegex = Regex("""^(?:\+\d+|[+.。!！?？,，~～_-]+)$""")
    private val repeatedCharRegex = Regex("""^(.)\1{3,}$""")

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

        return false
    }
}

private const val MAX_WATER_TEXT_LENGTH = 12
