package com.huanchengfly.tieba.post.repository.source.network

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.Error
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.LikeForumResultBean
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.api.models.protos.RecommendForumInfo
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.forumRuleDetail.ForumRuleDetailResponseData
import com.huanchengfly.tieba.post.api.models.protos.frsPage.FrsPageResponseData
import com.huanchengfly.tieba.post.api.models.protos.threadList.ThreadListResponseData
import com.huanchengfly.tieba.post.api.models.web.PcFrsPageResponse
import com.huanchengfly.tieba.post.api.retrofit.RetrofitTiebaApi
import com.huanchengfly.tieba.post.api.retrofit.exception.NoConnectivityException
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaApiException
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.api.retrofit.interceptors.ConnectivityInterceptor
import com.huanchengfly.tieba.post.api.urlEncode
import com.huanchengfly.tieba.post.arch.firstOrThrow
import com.huanchengfly.tieba.post.repository.withPcSign
import com.huanchengfly.tieba.post.repository.source.network.ExploreNetworkDataSource.commonResponse
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext

object ForumNetworkDataSource {

    private val threadFilter: (ThreadInfo) -> Boolean = {
        it.ala_info == null &&  // 去他妈的直播
        it.forumInfo != null    // 去他妈的跨吧广告帖
    }

    @Throws(NoConnectivityException::class, TiebaException::class)
    suspend fun loadForumDetail(forumId: Long): RecommendForumInfo {
        return TiebaApi.getInstance()
            .getForumDetailFlow(forumId)
            .catch { throw ConnectivityInterceptor.wrapException(it) }
            .firstOrThrow()
            .run {
                data_?.forum_info ?: throw TiebaApiException(this.error.commonResponse)
            }
    }

    @Throws(NoConnectivityException::class, TiebaException::class)
    suspend fun frsPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        tabId: Int,
        isEssence: Boolean,
        subClassifyId: Int?,
    ): FrsPageResponseData {
        val response = TiebaApi.getInstance()
            .frsPage(forumName, page, loadType, sortType, tabId, isEssence, subClassifyId)
            .catch { throw ConnectivityInterceptor.wrapException(it) }
            .firstOrThrow()
        if (response.data_?.forum == null) throw TiebaApiException(response.error.commonResponse)

        return withContext(Dispatchers.Default) {
            response.data_.thread_list
                .filter(threadFilter)
                .addUsers(response.data_.user_list)
                .let { new ->
                    response.data_.copy(thread_list = new)
                }
        }
    }

    @Throws(NoConnectivityException::class, TiebaException::class)
    suspend fun pcFrsPage(
        forumName: String,
        forumId: Long,
        page: Int,
        sortType: Int,
        tab: NavTab,
        subClassifyId: Int?,
        tbs: String?,
        frsCommonInfo: String?,
    ): PcFrsPageResponse {
        val response = if (tab.isGeneralTab) {
            val commonInfo = frsCommonInfo ?: pcForumBootstrap(forumName, page = 1, tbs = tbs).frsCommonInfo.orEmpty()
            RetrofitTiebaApi.WEB_TIEBA_API.generalTabListPcFlow(
                fields = pcGeneralTabFields(forumName, forumId, page, sortType, tab, tbs, commonInfo),
                referer = pcReferer(forumName, tab.tabId),
            )
        } else {
            RetrofitTiebaApi.WEB_TIEBA_API.frsPagePcFlow(
                fields = pcPageFields(
                    forumName = forumName,
                    forumId = forumId,
                    page = page,
                    sortType = if (tab.isHot) tab.pcSortType else sortType,
                    tab = tab,
                    subClassifyId = subClassifyId,
                    tbs = tbs,
                ),
                referer = pcReferer(forumName, tab.tabId),
            )
        }
            .catch { throw ConnectivityInterceptor.wrapException(it) }
            .firstOrThrow()
        if (response.errorCode != 0) throw TiebaApiException(response.commonResponse)
        return response
    }

    @Throws(NoConnectivityException::class, TiebaException::class)
    suspend fun pcForumBootstrap(
        forumName: String,
        page: Int,
        tbs: String?,
    ): PcFrsPageResponse {
        val response = RetrofitTiebaApi.WEB_TIEBA_API.frsPagePcFlow(
            fields = pcBootstrapFields(forumName, page, tbs),
            referer = pcReferer(forumName, tabId = null),
        )
            .catch { throw ConnectivityInterceptor.wrapException(it) }
            .firstOrThrow()
        if (response.errorCode != 0) throw TiebaApiException(response.commonResponse)
        return response
    }

    @Throws(NoConnectivityException::class, TiebaException::class)
    suspend fun loadThread(
        forumId: Long,
        forumName: String,
        page: Int,
        sortType: Int,
        threadIds: List<Long>,
    ): ThreadListResponseData {
        val threadId = threadIds.joinToString(separator = ",") { "$it" }
        val response = TiebaApi.getInstance()
            .threadList(forumId, forumName, page, sortType, threadId)
            .catch { throw ConnectivityInterceptor.wrapException(it) }
            .firstOrThrow()
        if (response.data_?.thread_list == null) throw TiebaApiException(response.error.commonResponse)

        return withContext(Dispatchers.Default) {
            response.data_.thread_list
                .filter(threadFilter)
                .addUsers(response.data_.user_list)
                .let { new ->
                    response.data_.copy(thread_list = new)
                }
        }
    }

    private fun pcBootstrapFields(forumName: String, page: Int, tbs: String?): Map<String, String> =
        linkedMapOf(
            "kw" to forumName.urlEncode(),
            "pn" to page.toString(),
            "sort_type" to "-1",
            "is_newfrs" to "1",
            "is_newfeed" to "1",
            "rn" to "30",
            "rn_need" to "10",
            "tbs" to tbs.orEmpty(),
            "subapp_type" to "pc",
            "_client_type" to "20",
        ).withPcSign()

    private fun pcPageFields(
        forumName: String,
        forumId: Long,
        page: Int,
        sortType: Int,
        tab: NavTab,
        subClassifyId: Int?,
        tbs: String?,
    ): Map<String, String> =
        linkedMapOf(
            "kw" to forumName.urlEncode(),
            "pn" to page.toString(),
            "is_good" to if (tab.isEssence) "1" else "0",
            "cid" to if (tab.isEssence) (subClassifyId ?: 0).takeIf { it != 0 }?.toString().orEmpty() else "",
            "sort_type" to sortType.toString(),
            "tab_id" to tab.tabId.toString(),
            "tab_type" to tab.tabType.toString(),
            "tab_name" to tab.tabName,
            "forum_id" to forumId.toString(),
            "is_newfrs" to "1",
            "is_newfeed" to "1",
            "rn" to "30",
            "rn_need" to "10",
            "tbs" to tbs.orEmpty(),
            "subapp_type" to "pc",
            "_client_type" to "20",
        ).withPcSign()

    private fun pcGeneralTabFields(
        forumName: String,
        forumId: Long,
        page: Int,
        sortType: Int,
        tab: NavTab,
        tbs: String?,
        frsCommonInfo: String,
    ): Map<String, String> =
        linkedMapOf(
            "pn" to page.toString(),
            "forum_id" to forumId.toString(),
            "forum_name" to forumName,
            "frs_common_info" to frsCommonInfo,
            "tab_id" to tab.tabId.toString(),
            "tab_name" to tab.tabName,
            "tab_type" to tab.tabType.toString(),
            "sort_type" to sortType.toString(),
            "partition_type" to "",
            "is_video_doublerow" to "0",
            "is_newfrs" to "1",
            "is_newfeed" to "1",
            "is_general_tab" to "1",
            "rn" to "10",
            "tbs" to tbs.orEmpty(),
            "subapp_type" to "pc",
            "_client_type" to "20",
        ).withPcSign()

    private fun pcReferer(forumName: String, tabId: Int?): String {
        val tabQuery = tabId?.let { "&tab=$it" }.orEmpty()
        return "https://tieba.baidu.com/f?kw=${forumName.urlEncode()}$tabQuery"
    }

    @Throws(NoConnectivityException::class, TiebaException::class)
    suspend fun loadForumRule(forumId: Long): ForumRuleDetailResponseData {
        return TiebaApi.getInstance()
            .forumRuleDetailFlow(forumId)
            .catch { throw ConnectivityInterceptor.wrapException(it) }
            .firstOrThrow()
            .run {
                data_ ?: throw TiebaApiException(commonResponse = error.commonResponse)
            }
    }

    @Throws(NoConnectivityException::class, TiebaException::class)
    suspend fun dislike(forumId: Long, forumName: String, tbs: String) {
        TiebaApi.getInstance()
            .unlikeForumFlow(forumId = forumId.toString(), forumName = forumName, tbs = tbs)
            .firstOrThrow()
            .let {
                if (it.errorCode != 0) throw TiebaApiException(commonResponse = it)
            }
    }

    @Throws(NoConnectivityException::class, TiebaException::class)
    suspend fun like(forumId: Long, forumName: String, tbs: String): LikeForumResultBean.Info {
        return TiebaApi.getInstance()
            .likeForumFlow(forumId = forumId.toString(), forumName, tbs = tbs)
            .firstOrThrow()
            .info
    }

    @Throws(NoConnectivityException::class, TiebaException::class)
    suspend fun forumSignIn(forumId: Long, forumName: String, tbs: String): SignResultBean.UserInfo {
        val response = TiebaApi.getInstance()
            .signFlow(forumId = forumId.toString(), forumName, tbs = tbs)
            .firstOrThrow()

        val info = response.userInfo ?: throw TiebaException(message = response.errorMsg)
        if (info.signBonusPoint == null || info.userSignRank == null) {
            throw TiebaException("Invalid SignIn data")
        }
        return info
    }
}

private val PcFrsPageResponse.commonResponse: CommonResponse
    get() = CommonResponse(
        errorCode = errorCode.takeUnless { it == 0 } ?: Error.ERROR_UNKNOWN,
        errorMsg = errorMsg,
    )

private fun List<ThreadInfo>.addUsers(userList: List<User>): List<ThreadInfo> {
    if (isEmpty()) return this
    val userMap = userList.associateBy { it.id }
    return map { thread ->
        val user = userMap[thread.authorId]
        val fallback = thread.author

        thread.copy(
            author = user?.copy(
                name = user.name.takeUnless { it.isBlank() } ?: fallback?.name.orEmpty(),
                nameShow = user.nameShow.takeUnless { it.isBlank() } ?: fallback?.nameShow.orEmpty()
            ) ?: fallback
        )
    }
}
