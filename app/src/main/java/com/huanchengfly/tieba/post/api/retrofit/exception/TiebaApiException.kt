package com.huanchengfly.tieba.post.api.retrofit.exception

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.CommonResponse

class TiebaApiException(
    private val commonResponse: CommonResponse
) : TiebaException(
    commonResponse.errorMsg.takeIf { it.isNotEmpty() }
        ?: App.INSTANCE.getString(R.string.error_unknown)
) {
    override val code: Int
        get() = commonResponse.errorCode

    override fun toString(): String {
        return "TiebaApiException(code=$code, message=$message, commonResponse=$commonResponse)"
    }
}
