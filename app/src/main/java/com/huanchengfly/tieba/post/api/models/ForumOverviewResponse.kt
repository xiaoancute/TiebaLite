package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName

data class ForumOverviewResponse(
    @SerializedName("error_code") val errorCode: Int = 0,
    @SerializedName("error_msg") val errorMsg: String? = null,
    val forum: ForumOverviewInfo? = null,
    @SerializedName("friend_forum") val friendForums: List<FriendForumInfo>? = null,
)

data class ForumOverviewInfo(
    val id: Long = 0,
    val name: String? = null,
    val avatar: String? = null,
    val slogan: String? = null,
    val desc: String? = null,
    @SerializedName("member_num") val memberCount: Long = 0,
    @SerializedName("thread_num") val threadCount: Long = 0,
    @SerializedName("post_num") val postCount: Long = 0,
)

data class FriendForumInfo(
    @SerializedName("forum_id") val id: Long = 0,
    @SerializedName("forum_name") val name: String? = null,
    val avatar: String? = null,
)
