package com.huanchengfly.tieba.post.api.models

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

data class FollowListBean(
    @SerializedName("pn")
    val pageNum: Int = 1,

    @SerializedName("has_more")
    val hasMore: Int = 0,

    @SerializedName("total_follow_num")
    val totalFollowNum: Int = 0,

    @SerializedName("tips_text")
    val tipsText: String? = null,

    @SerializedName("follow_list")
    val followList: List<FollowUserBean> = emptyList(),
) : BaseBean() {

    @Immutable
    data class FollowUserBean(
        val id: Long = 0,
        val name: String? = null,

        @SerializedName("name_show")
        val nameShow: String? = null,

        val intro: String? = null,
        val portrait: String? = null,

        @SerializedName("has_concerned")
        val hasConcerned: Int = 0,
    )
}
