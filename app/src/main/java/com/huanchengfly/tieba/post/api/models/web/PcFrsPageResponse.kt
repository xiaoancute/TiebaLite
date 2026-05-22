package com.huanchengfly.tieba.post.api.models.web

import com.google.gson.annotations.SerializedName

data class PcFrsPageResponse(
    @SerializedName("error_code")
    val errorCode: Int = 0,
    @SerializedName("error_msg")
    val errorMsg: String = "",
    val forum: PcForum? = null,
    val anti: PcAnti? = null,
    @SerializedName("nav_tab_info")
    val navTabInfo: PcNavTabInfo? = null,
    @SerializedName("frs_common_info")
    val frsCommonInfo: String? = null,
    @SerializedName("frs_tab_default")
    val frsTabDefault: Int? = null,
    @SerializedName("page_data_tab_id")
    val pageDataTabId: Int? = null,
    @SerializedName("page_data")
    val pageData: PcPageData? = null,
    val page: PcPage? = null,
    @SerializedName("has_more")
    val hasMore: Int? = null,
)

data class PcForum(
    val id: Long = 0,
    val name: String = "",
    val avatar: String? = null,
    val slogan: String? = null,
    @SerializedName("is_like")
    val isLike: Int = 0,
    @SerializedName("member_num")
    val memberNum: Int = 0,
    @SerializedName("thread_num")
    val threadNum: Int = 0,
    @SerializedName("post_num")
    val postNum: Long = 0,
)

data class PcAnti(
    val tbs: String? = null,
)

data class PcNavTabInfo(
    val tab: List<PcFrsTab> = emptyList(),
)

data class PcFrsTab(
    @SerializedName("tab_id")
    val tabId: Int = 0,
    @SerializedName("tab_type")
    val tabType: Int = 0,
    @SerializedName("tab_name")
    val tabName: String = "",
    @SerializedName("is_general_tab")
    val isGeneralTab: Int = 0,
)

data class PcPageData(
    @SerializedName("feed_list")
    val feedList: List<PcFeedItem> = emptyList(),
)

data class PcPage(
    @SerializedName("has_more")
    val hasMore: Int = 0,
)

data class PcFeedItem(
    val layout: String? = null,
    val feed: PcFeed? = null,
)

data class PcFeed(
    @SerializedName("business_info_map")
    val businessInfoMap: Map<String, String> = emptyMap(),
    val components: List<PcFeedComponent> = emptyList(),
)

data class PcFeedComponent(
    val component: String? = null,
    @SerializedName("feed_head")
    val feedHead: PcFeedHead? = null,
    @SerializedName("feed_title")
    val feedTitle: PcRichText? = null,
    @SerializedName("feed_abstract")
    val feedAbstract: PcRichText? = null,
    @SerializedName("feed_pic")
    val feedPic: PcFeedPic? = null,
    @SerializedName("feed_social")
    val feedSocial: PcFeedSocial? = null,
)

data class PcFeedHead(
    @SerializedName("main_data")
    val mainData: List<PcHeadTextItem> = emptyList(),
)

data class PcHeadTextItem(
    val text: PcTextInfo? = null,
)

data class PcRichText(
    val data: List<PcRichTextItem> = emptyList(),
)

data class PcRichTextItem(
    @SerializedName("text_info")
    val textInfo: PcTextInfo? = null,
)

data class PcTextInfo(
    val text: String = "",
)

data class PcFeedPic(
    val pics: List<PcPic> = emptyList(),
)

data class PcPic(
    @SerializedName("small_pic_url")
    val smallPicUrl: String? = null,
    @SerializedName("big_pic_url")
    val bigPicUrl: String? = null,
    @SerializedName("origin_pic_url")
    val originPicUrl: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    @SerializedName("is_long_pic")
    val isLongPic: Int = 0,
)

data class PcFeedSocial(
    val tid: Long = 0,
    val fid: Long = 0,
    @SerializedName("first_post_id")
    val firstPostId: Long = 0,
    @SerializedName("comment_num")
    val commentNum: Int = 0,
    @SerializedName("share_num")
    val shareNum: Long = 0,
    val agree: PcAgree? = null,
)

data class PcAgree(
    @SerializedName("has_agree")
    val hasAgree: Int = 0,
    @SerializedName("agree_num")
    val agreeNum: Long = 0,
)
