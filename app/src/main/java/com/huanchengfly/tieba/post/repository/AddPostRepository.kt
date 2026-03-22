package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.emitGlobalEventSuspend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

object AddPostRepository {
    fun addPost(
        content: String,
        forumId: Long,
        forumName: String,
        threadId: Long,
        tbs: String? = null,
        nameShow: String? = null,
        postId: Long? = null,
        subPostId: Long? = null,
        replyUserId: Long? = null
    ): Flow<AddPostResponse> =
        TiebaApi.getInstance()
            .addPostFlow(
                content,
                forumId.toString(),
                forumName,
                threadId.toString(),
                tbs,
                nameShow,
                postId?.toString(),
                subPostId?.toString(),
                replyUserId?.toString()
            )
            .onEach {
                val newPostId = checkNotNull(it.data_?.pid?.toLongOrNull())
                if (postId != null) {
                    emitGlobalEventSuspend(
                        GlobalEvent.ReplySuccess(
                            threadId,
                            postId,
                            postId,
                            subPostId,
                            newPostId
                        )
                    )
                } else {
                    emitGlobalEventSuspend(GlobalEvent.ReplySuccess(threadId, newPostId))
                }
            }
}
