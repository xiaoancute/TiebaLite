package com.huanchengfly.tieba.post.api.models

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

data class FansListBean(
    val page: PageBean = PageBean(),

    @SerializedName("user_list")
    val userList: List<FanUserBean> = emptyList(),
) : BaseBean() {

    data class PageBean(
        @SerializedName("has_more")
        val hasMore: Int = 0,
    )

    @Immutable
    data class FanUserBean(
        val id: Long = 0,
        val name: String? = null,

        @SerializedName("name_show")
        val nameShow: String? = null,

        val intro: String? = null,
        val portrait: String? = null,

        @SerializedName("follow_from")
        val followFrom: String? = null,

        @SerializedName("has_concerned")
        val hasConcerned: Int = 0,
    )
}
