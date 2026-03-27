package com.huanchengfly.tieba.post.api.models

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UploadPictureResultBeanTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun chunkUploadPayloadAllowsMissingFinalImageFields() {
        val payload = """
            {
              "error_code": "0",
              "error_msg": "",
              "chunkNo": "1"
            }
        """.trimIndent()

        val bean = json.decodeFromString<UploadPictureResultBean>(payload)

        assertEquals("0", bean.errorCode)
        assertEquals("", bean.errorMsg)
        assertEquals("1", bean.chunkNo)
        assertNull(bean.resourceId)
        assertNull(bean.picId)
        assertNull(bean.picInfo)
    }
}
