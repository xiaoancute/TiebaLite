package com.huanchengfly.tieba.post.ui.page.webview

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

internal sealed interface LinkRoutingDecision {
    data object Ignore : LinkRoutingDecision
    data object AllowWebView : LinkRoutingDecision
    data object UnsupportedTiebaClientAction : LinkRoutingDecision
    data class OpenThread(val threadId: Long) : LinkRoutingDecision
    data class OpenForum(val forumName: String) : LinkRoutingDecision
    data class OpenWebView(val url: String) : LinkRoutingDecision
    data class OpenExternal(val url: String) : LinkRoutingDecision
    data class LaunchThirdParty(val url: String) : LinkRoutingDecision
}

internal fun resolveAppLinkRouting(
    url: String,
    useWebView: Boolean,
): LinkRoutingDecision = resolveAppLinkRouting(parseLink(url), useWebView)

internal fun resolveWebViewLinkRouting(
    url: String,
    useWebView: Boolean,
): LinkRoutingDecision = resolveWebViewLinkRouting(parseLink(url), useWebView)

private fun resolveAppLinkRouting(
    parsedLink: ParsedLink?,
    useWebView: Boolean,
): LinkRoutingDecision {
    val parsed = parsedLink ?: return LinkRoutingDecision.Ignore
    val scheme = parsed.scheme?.lowercase() ?: return LinkRoutingDecision.Ignore
    if (scheme.equals("tiebaclient", ignoreCase = true)) {
        return when (parsed.queryParameter("action")) {
            "preview_file" -> {
                val realUrl = parsed.queryParameter("url")
                if (realUrl.isNullOrEmpty()) {
                    LinkRoutingDecision.Ignore
                } else {
                    resolveAppLinkRouting(realUrl, useWebView)
                }
            }

            else -> LinkRoutingDecision.UnsupportedTiebaClientAction
        }
    }
    if (!scheme.startsWith("http")) {
        return LinkRoutingDecision.LaunchThirdParty(parsed.rawUrl)
    }
    val host = parsed.host?.lowercase() ?: return LinkRoutingDecision.Ignore
    val path = parsed.path ?: return LinkRoutingDecision.Ignore
    if (path.contains("android_asset")) {
        return LinkRoutingDecision.Ignore
    }
    if (path == "/mo/q/checkurl") {
        val realUrl = parsed.queryParameter("url")
            ?.replace("http://https://", "https://")
            .orEmpty()
        return if (realUrl.isBlank()) {
            LinkRoutingDecision.Ignore
        } else {
            resolveAppLinkRouting(realUrl, useWebView)
        }
    }
    resolveTiebaRouting(host, path, parsed)?.let {
        return it
    }
    val isTiebaLink =
        isInternalHost(host)
    return when {
        shouldOpenExternallyFromRouting(scheme, host) ->
            LinkRoutingDecision.OpenExternal(parsed.rawUrl)

        isTiebaLink || useWebView ->
            LinkRoutingDecision.OpenWebView(parsed.rawUrl)

        else ->
            LinkRoutingDecision.OpenExternal(parsed.rawUrl)
    }
}

private fun resolveWebViewLinkRouting(
    parsedLink: ParsedLink?,
    useWebView: Boolean,
): LinkRoutingDecision {
    val scheme = parsedLink?.scheme?.lowercase() ?: return LinkRoutingDecision.AllowWebView
    val host = parsedLink.host?.lowercase() ?: return LinkRoutingDecision.AllowWebView
    val path = parsedLink.path?.lowercase() ?: return LinkRoutingDecision.AllowWebView
    if (path == "/mo/q/checkurl") {
        val realUrl = parsedLink.queryParameter("url")
            ?.replace("http://https://", "https://")
            .orEmpty()
        return if (realUrl.isBlank()) {
            LinkRoutingDecision.AllowWebView
        } else {
            resolveWebViewLinkRouting(realUrl, useWebView)
        }
    }
    val isHttp = scheme.startsWith("http")
    val isTieba = isTiebaHost(host)
    val isInternal = isInternalHost(host)
    return when {
        isHttp && isTieba -> {
            resolveTiebaRouting(host, path, parsedLink) ?: LinkRoutingDecision.AllowWebView
        }

        isHttp && !isInternal -> {
            when {
                shouldOpenExternallyFromRouting(scheme, host) ->
                    LinkRoutingDecision.OpenExternal(parsedLink.rawUrl)

                useWebView -> LinkRoutingDecision.AllowWebView
                else -> LinkRoutingDecision.OpenExternal(parsedLink.rawUrl)
            }
        }

        !isHttp -> LinkRoutingDecision.LaunchThirdParty(parsedLink.rawUrl)
        else -> LinkRoutingDecision.AllowWebView
    }
}

private fun resolveTiebaRouting(
    host: String,
    path: String,
    parsedLink: ParsedLink,
): LinkRoutingDecision? {
    if (!isTiebaHost(host)) {
        return null
    }
    if (path == "/f" || path == "/mo/q/m") {
        val forumName = parsedLink.queryParameter("kw") ?: parsedLink.queryParameter("word")
        val threadId = parsedLink.queryParameter("kz")?.toLongOrNull()
        return when {
            threadId != null -> LinkRoutingDecision.OpenThread(threadId)
            forumName != null -> LinkRoutingDecision.OpenForum(forumName)
            else -> null
        }
    }
    if (path.startsWith("/p/")) {
        val threadId = path.substring(3).toLongOrNull()
        if (threadId != null) {
            return LinkRoutingDecision.OpenThread(threadId)
        }
    }
    return null
}

private fun shouldOpenExternallyFromRouting(
    scheme: String?,
    host: String?,
): Boolean {
    val normalizedScheme = scheme?.lowercase() ?: return false
    val normalizedHost = host?.lowercase() ?: return false
    return normalizedScheme == "http" && !isInternalHost(normalizedHost)
}

private data class ParsedLink(
    val rawUrl: String,
    val scheme: String?,
    val host: String?,
    val path: String?,
    val queryParameters: Map<String, List<String>>,
) {
    fun queryParameter(name: String): String? = queryParameters[name]?.firstOrNull()
}

private fun parseLink(url: String): ParsedLink? = runCatching {
    val uri = URI(url)
    ParsedLink(
        rawUrl = url,
        scheme = uri.scheme,
        host = uri.host,
        path = uri.path,
        queryParameters = parseQueryParameters(uri.rawQuery)
    )
}.getOrNull()

private fun parseQueryParameters(rawQuery: String?): Map<String, List<String>> {
    if (rawQuery.isNullOrBlank()) {
        return emptyMap()
    }
    return rawQuery.split("&")
        .filter { it.isNotEmpty() }
        .map { segment ->
            val separatorIndex = segment.indexOf('=')
            if (separatorIndex >= 0) {
                val key = segment.substring(0, separatorIndex)
                val value = segment.substring(separatorIndex + 1)
                decodeQueryComponent(key) to decodeQueryComponent(value)
            } else {
                decodeQueryComponent(segment) to ""
            }
        }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
}

private fun decodeQueryComponent(component: String): String =
    URLDecoder.decode(component, StandardCharsets.UTF_8)
