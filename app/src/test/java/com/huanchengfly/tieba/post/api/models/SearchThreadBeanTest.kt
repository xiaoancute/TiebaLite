package com.huanchengfly.tieba.post.api.models

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SearchThreadBeanTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun `error payload without data still deserializes`() {
        val payload = """
            {
              "no": 110001,
              "error": "unknown error",
              "info": []
            }
        """.trimIndent()

        val result = json.decodeFromString(SearchThreadBean.serializer(), payload)

        assertEquals(110001, result.errorCode)
        assertEquals("unknown error", result.errorMsg)
        assertNull(result.data)
    }

    @Test
    fun `success payload with empty post list still deserializes`() {
        val payload = """
            {
              "no": 0,
              "error": "success",
              "data": {
                "has_more": 0,
                "current_page": 1,
                "post_list": []
              }
            }
        """.trimIndent()

        val result = json.decodeFromString(SearchThreadBean.serializer(), payload)

        assertEquals(0, result.errorCode)
        assertNotNull(result.data)
        assertEquals(emptyList<SearchThreadBean.ThreadInfoBean>(), result.data?.postList)
    }
}
