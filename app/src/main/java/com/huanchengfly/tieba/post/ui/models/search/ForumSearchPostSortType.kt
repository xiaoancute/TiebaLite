package com.huanchengfly.tieba.post.ui.models.search

import androidx.annotation.IntDef

@IntDef(ForumSearchPostSortType.NEWEST, ForumSearchPostSortType.RELATIVE)
@Retention(AnnotationRetention.SOURCE)
annotation class ForumSearchPostSortType {
    companion object {
        const val NEWEST = 1
        const val RELATIVE = 2
    }
}
