package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.OriginThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.revival.PublicBrowsePayloadGuard
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageFrom
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object EmptyDataException : TiebaException("帖子页返回空帖子列表") {
    override val code: Int
        get() = -2
}

object PbPageRepository {
    const val ST_TYPE_MENTION = "mention"
    const val ST_TYPE_STORE_THREAD = "store_thread"
    private val ST_TYPES = persistentListOf(ST_TYPE_MENTION, ST_TYPE_STORE_THREAD)

    fun pbPage(
        threadId: Long,
        page: Int = 1,
        postId: Long = 0,
        forumId: Long? = null,
        seeLz: Boolean = false,
        sortType: Int = 0,
        back: Boolean = false,
        from: String = "",
        lastPostId: Long? = null,
    ): Flow<PbPageResponse> =
        TiebaApi.getInstance()
            .pbPageFlow(
                threadId,
                page,
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
                back = back,
                forumId = forumId,
                stType = from.takeIf { ST_TYPES.contains(it) }.orEmpty(),
                mark = if (from == ThreadPageFrom.FROM_STORE) 1 else 0,
                lastPostId = lastPostId
            )
            .map { response ->
                val data = PublicBrowsePayloadGuard.requireThreadPageData(response)
                val forum = requireNotNull(data.forum)
                val thread = requireNotNull(data.thread)
                val threadAuthor = requireNotNull(thread.author)
                val userList = data.user_list
                val postList = data.post_list.map {
                    val author = it.author
                        ?: userList.first { user -> user.id == it.author_id }
                    it.copy(
                        author_id = author.id,
                        author = it.author
                            ?: userList.first { user -> user.id == it.author_id },
                        from_forum = forum,
                        tid = thread.id,
                        sub_post_list = it.sub_post_list?.copy(
                            sub_post_list = it.sub_post_list.sub_post_list.map { subPost ->
                                subPost.copy(
                                    author = subPost.author
                                        ?: userList.first { user -> user.id == subPost.author_id }
                                )
                            }
                        ),
                        origin_thread_info = OriginThreadInfo(
                            author = threadAuthor
                        )
                    )
                }
                val firstPost = postList.firstOrNull { it.floor == 1 }
                    ?: data.first_floor_post?.copy(
                        author_id = threadAuthor.id,
                        author = threadAuthor,
                        from_forum = forum,
                        tid = thread.id,
                        sub_post_list = data.first_floor_post.sub_post_list?.copy(
                            sub_post_list = data.first_floor_post.sub_post_list.sub_post_list.map { subPost ->
                                subPost.copy(
                                    author = subPost.author
                                        ?: userList.first { user -> user.id == subPost.author_id }
                                )
                            }
                        )
                    )

                response.copy(
                    data_ = data.copy(
                        post_list = postList,
                        first_floor_post = firstPost,
                    )
                )
            }
}
