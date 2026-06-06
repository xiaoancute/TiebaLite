package com.huanchengfly.tieba.post.ui.models.settings

import androidx.annotation.IntDef
import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.ui.models.search.ForumSearchPostSortType
import com.huanchengfly.tieba.post.ui.models.search.SearchThreadSortType
import com.huanchengfly.tieba.post.utils.ImageUtil

@IntDef(ForumFAB.POST, ForumFAB.REFRESH, ForumFAB.BACK_TO_TOP, ForumFAB.HIDE)
@Retention(AnnotationRetention.SOURCE)
annotation class ForumFAB {
    companion object {
        const val POST = 1
        const val REFRESH = 2
        const val BACK_TO_TOP = 4
        const val HIDE = 8
    }
}

/**
 * 帖子排序方式
 * */
@IntDef(ForumSortType.BY_REPLY, ForumSortType.BY_SEND)
@Retention(AnnotationRetention.SOURCE)
annotation class ForumSortType {
    companion object {
        const val BY_REPLY = 0
        const val BY_SEND = 1
    }
}

/**
 * 图片上传水印
 * */
@IntDef(WaterType.NO, WaterType.USER_NAME, WaterType.FORUM_NAME)
@Retention(AnnotationRetention.SOURCE)
annotation class WaterType {
    companion object {
        const val NO = 0
        const val USER_NAME = 1
        const val FORUM_NAME = 2
    }
}

@IntDef(
    AutoClearImageCacheInterval.OFF,
    AutoClearImageCacheInterval.ON_LAUNCH,
    AutoClearImageCacheInterval.DAILY,
    AutoClearImageCacheInterval.THREE_DAYS,
)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoClearImageCacheInterval {
    companion object {
        const val OFF = 0
        const val ON_LAUNCH = 1
        const val DAILY = 2
        const val THREE_DAYS = 3

        const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L
        const val THREE_DAYS_MILLIS = 3L * ONE_DAY_MILLIS

        fun shouldClear(@AutoClearImageCacheInterval interval: Int, lastClearTime: Long, now: Long): Boolean =
            when (interval) {
                ON_LAUNCH -> true
                DAILY -> lastClearTime <= 0L || now - lastClearTime >= ONE_DAY_MILLIS
                THREE_DAYS -> lastClearTime <= 0L || now - lastClearTime >= THREE_DAYS_MILLIS
                else -> false
            }
    }
}

/**
 * User habit
 *
 * @param autoClearImageCacheInterval 图片缓存自动清理周期
 * @param collectedDesc 收藏贴倒序浏览
 * @param favoriteDesc 收藏贴自动开启倒序浏览
 * @param favoriteSeeLz 从收藏进入的贴子将自动切换至只看楼主
 * @param forumSortType 吧页面默认排序方式
 * @param hideMedia 隐藏贴子列表的图片和视频
 * @param hideReply 隐藏回贴入口
 * @param hideReplyWarning 隐藏回贴风险提示
 * @param imageLoadType 图片加载设置
 * @param imageWatermarkType 图片上传水印设置
 * @param lastAutoClearImageCacheTime 上次自动清理图片缓存的时间戳
 * @param preloadNextPage 提前预加载下一页内容
 * @param forumSearchPostSortType 吧内搜贴默认排序方式
 * @param searchThreadSortType 搜贴默认排序方式
 * @param showBothName 同时显示用户名和昵称
 * @param stickyHeader 帖子页面是否使用StickyHeader
 * */
@Immutable
data class HabitSettings(
    @AutoClearImageCacheInterval val autoClearImageCacheInterval: Int = AutoClearImageCacheInterval.OFF,
    val collectedDesc: Boolean = false,
    val favoriteDesc: Boolean = false,
    val favoriteSeeLz: Boolean = true,
    @ForumSortType val forumSortType: Int = ForumSortType.BY_REPLY,
    val hideMedia: Boolean = false,
    val hideReply: Boolean = false,
    val hideReplyWarning: Boolean = false,
    val imageLoadType: Int = ImageUtil.SETTINGS_SMART_ORIGIN,
    @WaterType val imageWatermarkType: Int = WaterType.FORUM_NAME,
    val lastAutoClearImageCacheTime: Long = 0L,
    val preloadNextPage: Boolean = false,
    @ForumSearchPostSortType val forumSearchPostSortType: Int = ForumSearchPostSortType.NEWEST,
    @SearchThreadSortType val searchThreadSortType: Int = SearchThreadSortType.NEWEST,
    val showBothName: Boolean = false,
    val stickyHeader: Boolean = true,
)
