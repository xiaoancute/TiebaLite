package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import org.intellij.lang.annotations.RegExp

object BilibiliUtil {
    private const val URL_TAG = "url"
    private const val VIDEO_URL_PREFIX = "https://www.bilibili.com/video/"
    private const val ARTICLE_URL_PREFIX = "https://www.bilibili.com/read/"
    private const val AUDIO_URL_PREFIX = "https://www.bilibili.com/audio/"

    @RegExp
    const val REGEX_BV = "BV([a-zA-Z0-9]{10})"

    @RegExp
    const val REGEX_AV = "av([0-9]{1,})"

    @RegExp
    const val REGEX_CV = "cv([0-9]{1,})"

    @RegExp
    const val REGEX_AU = "au([0-9]{1,})"

    private data class VideoNumberPattern(
        val regex: Regex,
        val urlPrefix: String,
    )

    private val patterns = listOf(
        VideoNumberPattern(REGEX_BV.toRegex(), VIDEO_URL_PREFIX),
        VideoNumberPattern(REGEX_AV.toRegex(), VIDEO_URL_PREFIX),
        VideoNumberPattern(REGEX_CV.toRegex(), ARTICLE_URL_PREFIX),
        VideoNumberPattern(REGEX_AU.toRegex(), AUDIO_URL_PREFIX),
    )

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun replaceVideoNumberSpan(
        context: Context,
        source: CharSequence?
    ): SpannableString {
        if (source == null) {
            return SpannableString("")
        }
        return if (source is SpannableString) {
            source
        } else {
            SpannableString(source)
        }.also {
            patterns.forEach { pattern ->
                replace(
                    regex = pattern.regex.pattern,
                    source = it,
                    urlPrefix = pattern.urlPrefix
                )
            }
        }
    }

    fun annotateVideoNumbers(source: AnnotatedString): AnnotatedString {
        if (source.text.isEmpty()) {
            return source
        }
        return buildAnnotatedString {
            append(source)
            patterns.forEach { pattern ->
                pattern.regex.findAll(source.text).forEach { match ->
                    val start = match.range.first
                    val end = match.range.last + 1
                    if (source.getStringAnnotations(tag = URL_TAG, start = start, end = end).isEmpty()) {
                        addStringAnnotation(
                            tag = URL_TAG,
                            annotation = "${pattern.urlPrefix}${match.value}",
                            start = start,
                            end = end
                        )
                    }
                }
            }
        }
    }

    private fun replace(
        regex: String,
        source: SpannableString,
        urlPrefix: String = VIDEO_URL_PREFIX
    ): CharSequence {
        return try {
            val pattern = regex.toRegex()
            pattern.findAll(source).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                if (source.getSpans(start, end, URLSpan::class.java).isEmpty()) {
                    source.setSpan(
                        URLSpan("$urlPrefix${match.value}"),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            source
        } catch (e: Exception) {
            e.printStackTrace()
            source
        }
    }
}
