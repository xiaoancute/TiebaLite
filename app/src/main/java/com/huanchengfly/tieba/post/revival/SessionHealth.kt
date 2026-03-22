package com.huanchengfly.tieba.post.revival

import android.content.Context
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.utils.CookieParser

enum class SessionHealthStatus {
    LoggedOut,
    Complete,
    CookieIncomplete,
    WebOnly,
    AccountIncomplete,
}

data class SessionHealth(
    val status: SessionHealthStatus,
    val missingCookies: List<String> = emptyList(),
    val missingAccountFields: List<String> = emptyList(),
) {
    val isComplete: Boolean
        get() = status == SessionHealthStatus.Complete

    fun toDisplayText(context: Context): String {
        val baseText = when (status) {
            SessionHealthStatus.LoggedOut -> context.getString(R.string.session_health_logged_out)
            SessionHealthStatus.Complete -> context.getString(R.string.session_health_complete)
            SessionHealthStatus.CookieIncomplete -> {
                context.getString(R.string.session_health_cookie_incomplete)
            }

            SessionHealthStatus.WebOnly -> context.getString(R.string.session_health_web_only)
            SessionHealthStatus.AccountIncomplete -> {
                context.getString(R.string.session_health_account_incomplete)
            }
        }
        val missingFields = (missingCookies + missingAccountFields).distinct()
        return if (missingFields.isEmpty()) {
            baseText
        } else {
            context.getString(
                R.string.text_session_health_missing,
                baseText,
                missingFields.joinToString(", ")
            )
        }
    }
}

object SessionHealthEvaluator {
    private val requiredCookies = listOf("BDUSS", "STOKEN")

    fun fromCookie(cookie: String?): SessionHealth {
        if (cookie.isNullOrBlank()) {
            return SessionHealth(
                status = SessionHealthStatus.CookieIncomplete,
                missingCookies = requiredCookies
            )
        }
        val cookies = CookieParser.parse(cookie).mapKeys { it.key.uppercase() }
        val missingCookies = requiredCookies.filter { cookies[it].isNullOrBlank() }
        return if (missingCookies.isEmpty()) {
            SessionHealth(status = SessionHealthStatus.Complete)
        } else {
            SessionHealth(
                status = SessionHealthStatus.CookieIncomplete,
                missingCookies = missingCookies
            )
        }
    }

    fun webOnly(cookie: String?): SessionHealth {
        val cookieHealth = fromCookie(cookie)
        return if (!cookieHealth.isComplete) {
            cookieHealth
        } else {
            SessionHealth(status = SessionHealthStatus.WebOnly)
        }
    }

    fun fromAccount(account: Account?): SessionHealth {
        if (account == null) {
            return SessionHealth(status = SessionHealthStatus.LoggedOut)
        }
        val cookieHealth = fromCookie(account.cookie)
        val missingAccountFields = buildMissingAccountFields(account)
        return when {
            !cookieHealth.isComplete -> cookieHealth.copy(
                missingAccountFields = missingAccountFields
            )

            missingAccountFields.isNotEmpty() -> SessionHealth(
                status = SessionHealthStatus.AccountIncomplete,
                missingAccountFields = missingAccountFields
            )

            else -> SessionHealth(status = SessionHealthStatus.Complete)
        }
    }

    private fun buildMissingAccountFields(account: Account): List<String> = buildList {
        if (account.bduss.isBlank()) add("BDUSS")
        if (account.sToken.isBlank()) add("STOKEN")
        if (account.tbs.isBlank()) add("TBS")
    }
}
