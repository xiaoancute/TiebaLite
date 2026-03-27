package com.huanchengfly.tieba.post.ui.page.thread

import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.plainText

internal fun buildPostCopyText(post: Post): String {
    val contentText = post.content.plainText
    return if (post.floor <= 1 && post.title.isNotBlank() && post.is_ntitle != 1) {
        "${post.title}\n$contentText"
    } else {
        contentText
    }
}
