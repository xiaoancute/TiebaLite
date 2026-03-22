package com.huanchengfly.tieba.post.revival

import com.huanchengfly.tieba.post.api.models.RelateForumBean
import com.huanchengfly.tieba.post.api.models.SpecialTopicBean
import com.huanchengfly.tieba.post.api.models.ThreadInfoBean
import com.huanchengfly.tieba.post.api.models.TopicDetailBean
import com.huanchengfly.tieba.post.api.models.TopicInfoBean
import com.huanchengfly.tieba.post.api.models.protos.Error
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponseData
import com.huanchengfly.tieba.post.api.models.protos.frsPage.ForumInfo
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponse
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponseData
import com.huanchengfly.tieba.post.api.models.web.HotMessageListBean
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.repository.EmptyDataException

private const val PUBLIC_BROWSE_PAYLOAD_ERROR_CODE = -1201

class PublicBrowsePayloadException(message: String) : TiebaException(message) {
    override val code: Int = PUBLIC_BROWSE_PAYLOAD_ERROR_CODE
}

data class ForumHeaderPayload(
    val forum: ForumInfo,
    val tbs: String?,
)

data class TopicDetailPayload(
    val topicInfo: TopicInfoBean,
    val relatedForums: List<RelateForumBean>,
    val specialTopics: List<SpecialTopicBean>,
    val relatedThreads: List<ThreadInfoBean>,
    val hasMore: Boolean,
)

data class HotTopicRoute(
    val topicId: String,
    val topicName: String,
)

object PublicBrowsePayloadGuard {
    fun requireForumPageData(response: FrsPageResponse): FrsPageResponseData {
        requireProtoSuccess("吧页", response.error)
        val data = response.data_
            ?: throw PublicBrowsePayloadException("吧页返回缺少 data")
        if (data.page == null) {
            throw PublicBrowsePayloadException("吧页返回缺少 page")
        }
        return data
    }

    fun requireForumHeader(response: FrsPageResponse): ForumHeaderPayload {
        val data = requireForumPageData(response)
        val forum = data.forum
            ?: throw PublicBrowsePayloadException("吧页返回缺少 forum")
        return ForumHeaderPayload(
            forum = forum,
            tbs = data.anti?.tbs?.takeIf { it.isNotBlank() }
        )
    }

    fun requireThreadPageData(response: PbPageResponse): PbPageResponseData {
        requireProtoSuccess("帖子页", response.error)
        val data = response.data_
            ?: throw PublicBrowsePayloadException("帖子页返回缺少 data")
        if (data.post_list.isEmpty()) {
            throw EmptyDataException
        }
        if (data.page == null) {
            throw PublicBrowsePayloadException("帖子页返回缺少 page")
        }
        if (data.thread?.author == null) {
            throw PublicBrowsePayloadException("帖子页返回缺少 thread.author")
        }
        if (data.forum == null) {
            throw PublicBrowsePayloadException("帖子页返回缺少 forum")
        }
        if (data.anti == null) {
            throw PublicBrowsePayloadException("帖子页返回缺少 anti")
        }
        return data
    }

    fun requireTopicDetailPayload(response: TopicDetailBean): TopicDetailPayload {
        if (response.errorCode != 0) {
            throw PublicBrowsePayloadException(
                "话题详情接口返回错误: ${response.errorMsg.takeIf { it.isNotBlank() } ?: response.errorCode}"
            )
        }
        val data = response.data
            ?: throw PublicBrowsePayloadException("话题详情返回缺少 data")
        val topicInfo = data.topicInfo
            ?: throw PublicBrowsePayloadException("话题详情返回缺少 topic_info")
        return TopicDetailPayload(
            topicInfo = topicInfo,
            relatedForums = data.relateForum,
            specialTopics = data.specialTopic,
            relatedThreads = data.relateThread?.threadList.orEmpty().map { it.threadInfo },
            hasMore = data.hasMore
        )
    }

    fun requireHotTopicEntries(response: HotMessageListBean): List<HotMessageListBean.HotMessageRetBean> {
        if (response.errorCode != 0) {
            throw PublicBrowsePayloadException(
                "热议榜接口返回错误: ${response.errorMsg?.takeIf { it.isNotBlank() } ?: response.errorCode}"
            )
        }
        return response.data?.list?.ret.orEmpty()
    }

    fun requireHotTopicRoute(entry: HotMessageListBean.HotMessageRetBean): HotTopicRoute {
        val nestedTopicInfo = entry.topicInfo
        val topicId = entry.mulId?.takeIf { it.isNotBlank() }
            ?: nestedTopicInfo?.topicId?.takeIf { it.isNotBlank() }
            ?: throw PublicBrowsePayloadException("热议榜条目缺少 topic_id")
        val topicName = entry.mulName?.takeIf { it.isNotBlank() }
            ?: nestedTopicInfo?.topicName?.takeIf { it.isNotBlank() }
            ?: throw PublicBrowsePayloadException("热议榜条目缺少 topic_name")
        return HotTopicRoute(topicId, topicName)
    }

    private fun requireProtoSuccess(route: String, error: Error?) {
        val errorCode = error?.error_code ?: 0
        if (errorCode == 0) {
            return
        }
        val message = error?.user_msg
            ?.takeIf { it.isNotBlank() }
            ?: error?.error_msg?.takeIf { it.isNotBlank() }
            ?: errorCode.toString()
        throw PublicBrowsePayloadException("${route}接口返回错误: $message")
    }
}
