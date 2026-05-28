package com.huanchengfly.tieba.post.ui.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.VideoInfo
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.ui.models.explore.Dislike

enum class ThreadTimeType {
    DEFAULT, PUBLISH, REPLY
}

/**
 * Ui Model of ThreadInfo
 * */
@Immutable
/*data */class ThreadItem(
    val id: Long = -1,
    val firstPostId: Long = -1,
    val author: Author,
    val blocked: Boolean = false,
    val content: AnnotatedString? = null,
    val title: String,
    val isTop: Boolean = false,
    val lastTimeMill: Long,
    val timeType: ThreadTimeType = ThreadTimeType.DEFAULT,
    val like: Like = LikeZero,
    val hotNum: Int = 0,
    val replyNum: Int = 0,
    val shareNum: Long = 0,
    val medias: List<Media>? = null,
    val video: ImmutableHolder<VideoInfo>? = null,
    val originThreadInfo: ImmutableHolder<OriginThreadInfo>? = null,
    val simpleForum: SimpleForum,
    val dislikeResource: List<Dislike>? = null,
) {

    val liked: Boolean
        get() = like.liked

    fun copy(
        author: Author = this.author,
        blocked: Boolean = this.blocked,
        content: AnnotatedString? = this.content,
        title: String = this.title,
        lastTimeMill: Long = this.lastTimeMill,
        timeType: ThreadTimeType = this.timeType,
        like: Like = this.like,
        hotNum: Int = this.hotNum,
        replyNum: Int = this.replyNum,
        shareNum: Long = this.shareNum,
        medias: List<Media>? = this.medias,
        video: ImmutableHolder<VideoInfo>? = this.video,
        originThreadInfo: ImmutableHolder<OriginThreadInfo>? = this.originThreadInfo,
        simpleForum: SimpleForum = this.simpleForum,
        dislikeResource: List<Dislike>? = this.dislikeResource,
    ) = ThreadItem(
        id = this.id,
        firstPostId = this.firstPostId,
        author = author,
        blocked = blocked,
        content = content,
        title = title,
        isTop = this.isTop,
        lastTimeMill = lastTimeMill,
        timeType = timeType,
        like = like,
        hotNum = hotNum,
        replyNum = replyNum,
        shareNum = shareNum,
        medias = medias,
        video = video,
        originThreadInfo = originThreadInfo,
        simpleForum = simpleForum,
        dislikeResource = dislikeResource,
    )
}
