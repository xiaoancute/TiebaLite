package com.huanchengfly.tieba.post.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilTest {
    @Test
    fun avatarPortraitUsesCurrentBdimgDomain() {
        assertEquals(
            "https://himg.bdimg.com/sys/portrait/item/tb.1.demo.avatar?t=1774141263",
            StringUtil.getAvatarUrl("tb.1.demo.avatar?t=1774141263")
        )
    }

    @Test
    fun bigAvatarPortraitUsesCurrentBdimgDomain() {
        assertEquals(
            "https://himg.bdimg.com/sys/portraith/item/tb.1.demo.avatar?t=1774141263",
            StringUtil.getBigAvatarUrl("tb.1.demo.avatar?t=1774141263")
        )
    }

    @Test
    fun fullAvatarUrlPassesThroughUntouched() {
        val fullUrl = "https://gss0.bdstatic.com/6LZ1dD3d1sgCo2Kml5_Y_D3/sys/portrait/item/.jpg"

        assertEquals(fullUrl, StringUtil.getAvatarUrl(fullUrl))
        assertEquals(fullUrl, StringUtil.getBigAvatarUrl(fullUrl))
    }
}
