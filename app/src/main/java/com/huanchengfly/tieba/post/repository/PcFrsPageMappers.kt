package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.web.PcFeed
import com.huanchengfly.tieba.post.api.models.web.PcFrsPageResponse
import com.huanchengfly.tieba.post.api.models.web.PcFrsTab
import com.huanchengfly.tieba.post.toMD5
import com.huanchengfly.tieba.post.ui.models.Author
import com.huanchengfly.tieba.post.ui.models.Like
import com.huanchengfly.tieba.post.ui.models.LikeZero
import com.huanchengfly.tieba.post.ui.models.SimpleForum
import com.huanchengfly.tieba.post.ui.models.ThreadItem
import com.huanchengfly.tieba.post.ui.models.ThreadItemList
import com.huanchengfly.tieba.post.ui.models.ThreadTimeType
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
import com.huanchengfly.tieba.post.ui.models.settings.ForumSortType
import com.huanchengfly.tieba.post.ui.widgets.compose.buildThreadContent
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.StringUtil

private const val PC_SIGN_SECRET = "36770b1f34c9bbf2e7d1a99d2b82fa9e"

internal fun Map<String, String>.withPcSign(): Map<String, String> {
    val signSource = entries
        .sortedBy { it.key }
        .joinToString(separator = "") { "${it.key}=${it.value}" }
    return this + ("sign" to (signSource + PC_SIGN_SECRET).toMD5().lowercase())
}

internal fun PcFrsPageResponse.toNavTabs(): List<NavTab> =
    navTabInfo?.tab.orEmpty().toNavTabs(defaultTabId = pageDataTabId ?: frsTabDefault)

internal fun List<PcFrsTab>.toNavTabs(defaultTabId: Int? = null): List<NavTab> {
    if (isEmpty()) return listOf(NavTab.Fallback)
    return mapIndexed { index, tab ->
        NavTab(
            tabId = tab.tabId,
            tabName = tab.tabName,
            tabType = tab.tabType,
            isDefault = defaultTabId?.let { it == tab.tabId } ?: (index == 0),
            isGeneralTab = tab.isGeneralTab == 1,
        )
    }
}

internal suspend fun PcFrsPageResponse.toThreadItemList(
    tab: NavTab,
    @ForumSortType sortType: Int,
    showBothName: Boolean,
    isBlocked: suspend (uid: Long, content: Array<String>) -> Boolean,
): ThreadItemList {
    val threads = pageData
        ?.feedList
        .orEmpty()
        .mapNotNull { item -> item.feed?.toThreadItem(tab, sortType, showBothName, isBlocked) }
        .distinctBy { it.id }
    return ThreadItemList(
        threads = threads,
        threadIds = emptyList(),
        hasMore = hasMore == 1 || page?.hasMore == 1,
    )
}

private suspend fun PcFeed.toThreadItem(
    tab: NavTab,
    @ForumSortType sortType: Int,
    showBothName: Boolean,
    isBlocked: suspend (uid: Long, content: Array<String>) -> Boolean,
): ThreadItem? {
    val social = components.firstNotNullOfOrNull { it.feedSocial }
    val tid = businessInfoMap["thread_id"]?.toLongOrNull() ?: social?.tid ?: return null
    val title = businessInfoMap["title"]
        ?: components.firstNotNullOfOrNull { it.feedTitle?.plainText() }
        ?: return null
    val abstractText = businessInfoMap["abstract"]
        ?: components.firstNotNullOfOrNull { it.feedAbstract?.plainText() }
        ?: ""
    val authorId = businessInfoMap["user_id"]?.toLongOrNull() ?: 0
    val authorName = components.firstNotNullOfOrNull {
        it.feedHead?.mainData?.firstNotNullOfOrNull { item -> item.text?.text?.takeIf(String::isNotBlank) }
    }.orEmpty()
    val portrait = businessInfoMap["portrait"].orEmpty()
    val forumId = businessInfoMap["forum_id"]?.toLongOrNull() ?: social?.fid ?: 0
    val forumName = businessInfoMap["forum_name"].orEmpty()
    val forumAvatar = businessInfoMap["forum_avatar"]
    val tabName = tab.tabName.takeIf { tab.isGeneralTab }
    val createTime = businessInfoMap["create_time"]?.toLongOrNull() ?: 0
    val replyTime = businessInfoMap["last_time_int"]?.toLongOrNull()
        ?: businessInfoMap["last_time"]?.toLongOrNull()
        ?: 0
    val timeType = if (sortType == ForumSortType.BY_SEND) ThreadTimeType.PUBLISH else ThreadTimeType.REPLY
    val displayTime = if (timeType == ThreadTimeType.PUBLISH) createTime else replyTime.takeIf { it > 0 } ?: createTime
    val medias = components
        .flatMap { it.feedPic?.pics.orEmpty() }
        .map {
            val originPic = it.originPicUrl ?: it.bigPicUrl ?: it.smallPicUrl.orEmpty()
            Media(
                type = 3,
                bigPic = it.smallPicUrl ?: it.bigPicUrl.orEmpty(),
                srcPic = originPic,
                originPic = originPic,
                width = it.width,
                height = it.height,
                isLongPic = it.isLongPic,
            )
        }
        .takeUnless { it.isEmpty() }

    return ThreadItem(
        id = tid,
        firstPostId = social?.firstPostId ?: -1,
        author = Author(
            id = authorId,
            name = StringUtil.getUserNameString(showBothName, authorName, authorName),
            avatarUrl = StringUtil.getAvatarUrl(portrait),
        ),
        blocked = isBlocked(authorId, arrayOf(title, abstractText)),
        content = buildThreadContent(title, abstractText, tabName, isGood = businessInfoMap["is_good"] == "1"),
        title = title,
        lastTimeMill = DateTimeUtils.fixTimestamp(displayTime),
        timeType = timeType,
        like = social?.agree?.let { Like(liked = it.hasAgree == 1, count = it.agreeNum) } ?: LikeZero,
        hotNum = businessInfoMap["view_num"]?.toIntOrNull() ?: 0,
        replyNum = social?.commentNum ?: 0,
        shareNum = social?.shareNum ?: 0,
        medias = medias,
        simpleForum = SimpleForum(forumId, forumName, forumAvatar),
    )
}

private fun com.huanchengfly.tieba.post.api.models.web.PcRichText.plainText(): String =
    data.joinToString(separator = "") { it.textInfo?.text.orEmpty() }.trim()
