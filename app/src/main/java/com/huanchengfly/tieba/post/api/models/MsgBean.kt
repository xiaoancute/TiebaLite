package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean
import com.huanchengfly.tieba.post.models.ErrorBean

class MsgBean : ErrorBean() {
    val message: MessageBean? = null

    inner class MessageBean : BaseBean() {
        @SerializedName("replyme")
        val replyMe: String? = null

        @SerializedName("atme")
        val atMe: String? = null

        @SerializedName(value = "agreeme", alternate = ["zanme", "likeme", "goodme"])
        val agreeMe: String? = null

        val fans: String? = null

    }
}
