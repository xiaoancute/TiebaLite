package com.huanchengfly.tieba.post

import com.huanchengfly.tieba.post.utils.CookieParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CookieParserTest {
    @Test
    fun parse_keeps_simple_cookie_pairs() {
        val result = CookieParser.parse("BDUSS=abc; STOKEN=def; BAIDUID=ghi")

        assertEquals("abc", result["BDUSS"])
        assertEquals("def", result["STOKEN"])
        assertEquals("ghi", result["BAIDUID"])
    }

    @Test
    fun parse_keeps_value_segments_after_additional_equals() {
        val result = CookieParser.parse("TOKEN=part1=part2=part3; foo=bar")

        assertEquals("part1=part2=part3", result["TOKEN"])
        assertEquals("bar", result["foo"])
    }

    @Test
    fun parse_ignores_invalid_cookie_segments() {
        val result = CookieParser.parse("BDUSS=abc; invalid; ; STOKEN=def")

        assertEquals("abc", result["BDUSS"])
        assertEquals("def", result["STOKEN"])
        assertNull(result["invalid"])
    }
}
