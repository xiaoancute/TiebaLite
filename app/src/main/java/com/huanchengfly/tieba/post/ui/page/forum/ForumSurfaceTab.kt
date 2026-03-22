package com.huanchengfly.tieba.post.ui.page.forum

import androidx.annotation.StringRes
import com.huanchengfly.tieba.post.R

enum class ForumSurfaceTab(
    val key: String,
    @StringRes val titleRes: Int,
    val pagerIndex: Int?,
) {
    Latest("latest", R.string.tab_forum_latest, 0),
    Good("good", R.string.tab_forum_good, 1),
    Media("media", R.string.tab_forum_media, 2),
    Search("search", R.string.btn_search_in_forum, null),
    ;

    val isPagerTab: Boolean
        get() = pagerIndex != null

    companion object {
        val pagerTabs: List<ForumSurfaceTab> = values().filter { it.isPagerTab }

        fun fromPagerIndex(index: Int): ForumSurfaceTab =
            pagerTabs.firstOrNull { it.pagerIndex == index } ?: Latest
    }
}
