package com.huanchengfly.tieba.post.ui.page.reply

internal fun buildTiebaWebReplyUrl(threadId: Long, postId: Long?): String {
    val baseUrl = "https://tieba.baidu.com/p/$threadId"
    return if (postId != null && postId > 0) {
        "$baseUrl?pid=$postId"
    } else {
        baseUrl
    }
}
