package com.huanchengfly.tieba.post.api.retrofit.exception

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import java.io.IOException

abstract class TiebaException(message: String) : IOException(message) {
    abstract val code: Int

    override fun toString(): String {
        return "TiebaException(code=$code, message=$message)"
    }
}

object TiebaUnknownException : TiebaException(App.INSTANCE.getString(R.string.error_unknown)) {
    override val code: Int
        get() = -1
}
