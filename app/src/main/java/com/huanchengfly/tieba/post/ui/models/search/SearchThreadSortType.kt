package com.huanchengfly.tieba.post.ui.models.search

import androidx.annotation.IntDef

@IntDef(SearchThreadSortType.NEWEST, SearchThreadSortType.OLDEST, SearchThreadSortType.RELATIVE)
@Retention(AnnotationRetention.SOURCE)
annotation class SearchThreadSortType {
    companion object {
        const val NEWEST = 5
        const val OLDEST = 0
        const val RELATIVE = 2
    }
}
