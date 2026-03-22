package com.huanchengfly.tieba.post

import com.huanchengfly.tieba.post.ui.page.webview.LinkRoutingDecision
import com.huanchengfly.tieba.post.ui.page.webview.resolveAppLinkRouting
import com.huanchengfly.tieba.post.ui.page.webview.resolveWebViewLinkRouting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkRoutingTest {
    @Test
    fun appRoutingKeepsTiebaThreadLinksInApp() {
        val decision = resolveAppLinkRouting(
            "https://tieba.baidu.com/p/10547704116?see_lz=1",
            useWebView = false
        )

        assertEquals(LinkRoutingDecision.OpenThread(10547704116), decision)
    }

    @Test
    fun appRoutingSendsExternalHttpLinksToBrowserEvenWhenWebViewEnabled() {
        val decision = resolveAppLinkRouting(
            "http://example.com/readme",
            useWebView = true
        )

        assertTrue(decision is LinkRoutingDecision.OpenExternal)
        assertEquals("http://example.com/readme", (decision as LinkRoutingDecision.OpenExternal).url)
    }

    @Test
    fun appRoutingKeepsExternalHttpsLinksInWebViewWhenEnabled() {
        val decision = resolveAppLinkRouting(
            "https://example.com/readme",
            useWebView = true
        )

        assertEquals(
            LinkRoutingDecision.OpenWebView("https://example.com/readme"),
            decision
        )
    }

    @Test
    fun appRoutingResolvesCheckUrlRedirectsBeforeDeciding() {
        val decision = resolveAppLinkRouting(
            "https://tieba.baidu.com/mo/q/checkurl?url=https://example.com/docs",
            useWebView = false
        )

        assertTrue(decision is LinkRoutingDecision.OpenExternal)
        assertEquals("https://example.com/docs", (decision as LinkRoutingDecision.OpenExternal).url)
    }

    @Test
    fun webViewRoutingTurnsForumLinksIntoNativeForumNavigation() {
        val decision = resolveWebViewLinkRouting(
            "https://tieba.baidu.com/f?kw=%E5%8E%9F%E7%A5%9E",
            useWebView = false
        )

        assertEquals(LinkRoutingDecision.OpenForum("原神"), decision)
    }

    @Test
    fun webViewRoutingTurnsMoThreadLinksIntoNativeThreadNavigation() {
        val decision = resolveWebViewLinkRouting(
            "https://tieba.baidu.com/mo/q/m?kz=10547704116",
            useWebView = false
        )

        assertEquals(LinkRoutingDecision.OpenThread(10547704116), decision)
    }

    @Test
    fun webViewRoutingKeepsExternalHttpsInsideWebViewWhenEnabled() {
        val decision = resolveWebViewLinkRouting(
            "https://example.com/readme",
            useWebView = true
        )

        assertEquals(LinkRoutingDecision.AllowWebView, decision)
    }

    @Test
    fun webViewRoutingSendsExternalHttpToBrowser() {
        val decision = resolveWebViewLinkRouting(
            "http://example.com/readme",
            useWebView = true
        )

        assertTrue(decision is LinkRoutingDecision.OpenExternal)
        assertEquals("http://example.com/readme", (decision as LinkRoutingDecision.OpenExternal).url)
    }

    @Test
    fun webViewRoutingLaunchesThirdPartySchemes() {
        val decision = resolveWebViewLinkRouting(
            "intent://scan/#Intent;scheme=weixin;package=com.tencent.mm;end",
            useWebView = false
        )

        assertTrue(decision is LinkRoutingDecision.LaunchThirdParty)
        assertTrue((decision as LinkRoutingDecision.LaunchThirdParty).url.startsWith("intent://"))
    }
}
