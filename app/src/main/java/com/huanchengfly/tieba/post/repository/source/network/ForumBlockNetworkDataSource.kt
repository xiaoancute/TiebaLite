package com.huanchengfly.tieba.post.repository.source.network

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.protos.Error
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaApiException
import com.huanchengfly.tieba.post.arch.firstOrThrow
import com.huanchengfly.tieba.post.models.DislikeBean

data class OfficialBlockForum(
    val id: Long,
    val name: String,
)

object ForumBlockNetworkDataSource {
    private const val PAGE_SIZE = 100
    private const val FORUM_DISLIKE_ID = "7"

    suspend fun loadDislikeForums(): List<OfficialBlockForum> {
        val result = mutableListOf<OfficialBlockForum>()
        var page = 1
        var hasMore: Int
        do {
            val data = TiebaApi.getInstance()
                .getDislikeListFlow(page = page, pageSize = PAGE_SIZE)
                .firstOrThrow()
                .run {
                    data_ ?: throw TiebaApiException(this.error.commonResponse)
                }
            result += data.forum_list.mapNotNull { forum ->
                forum.forum_name.takeIf { it.isNotBlank() }?.let { name ->
                    OfficialBlockForum(id = forum.forum_id, name = name)
                }
            }
            hasMore = data.has_more
            page++
        } while (hasMore == 1)
        return result
    }

    suspend fun submitDislikeForum(forumId: Long) {
        TiebaApi.getInstance()
            .submitDislikeFlow(
                DislikeBean(
                    threadId = "1",
                    dislikeIds = FORUM_DISLIKE_ID,
                    forumId = forumId.toString(),
                    clickTime = System.currentTimeMillis(),
                    extra = "",
                )
            )
            .firstOrThrow()
            .checkSuccess()
    }

    suspend fun cancelDislikeForum(forumId: Long) {
        TiebaApi.getInstance()
            .submitCancelDislikeForumFlow(forumId)
            .firstOrThrow()
            .checkSuccess()
    }

    private fun CommonResponse.checkSuccess() {
        if (errorCode != 0) throw TiebaApiException(this)
    }

    private val Error?.commonResponse
        get() = CommonResponse(
            errorCode = this?.error_code ?: com.huanchengfly.tieba.post.api.Error.ERROR_UNKNOWN,
            errorMsg = this?.error_msg.orEmpty()
        )
}
