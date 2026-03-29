package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.models.ReadingTargetType
import com.huanchengfly.tieba.post.models.database.LocalFavoriteItem
import com.huanchengfly.tieba.post.utils.extension.findFlow
import kotlinx.coroutines.flow.Flow
import org.litepal.LitePal

object LocalFavoriteUtil {
    const val PAGE_SIZE = 100

    fun saveThread(
        threadId: Long,
        title: String,
        forumName: String? = null,
        username: String? = null,
        avatar: String? = null,
    ) {
        if (threadId == 0L || title.isBlank()) {
            return
        }
        saveOrUpdate(
            LocalFavoriteItem(
                targetType = ReadingTargetType.THREAD,
                threadId = threadId,
                forumName = forumName.orEmpty(),
                title = title,
                subtitle = forumName,
                username = username,
                avatar = avatar,
            ),
            condition = "targetType = ? and threadId = ?",
            conditionArgs = arrayOf(
                ReadingTargetType.THREAD.toString(),
                threadId.toString(),
            )
        )
    }

    fun saveForum(
        forumName: String,
        avatar: String? = null,
    ) {
        if (forumName.isBlank()) {
            return
        }
        saveOrUpdate(
            LocalFavoriteItem(
                targetType = ReadingTargetType.FORUM,
                forumName = forumName,
                title = forumName,
                avatar = avatar,
            ),
            condition = "targetType = ? and forumName = ?",
            conditionArgs = arrayOf(
                ReadingTargetType.FORUM.toString(),
                forumName,
            )
        )
    }

    fun removeThread(threadId: Long) {
        if (threadId == 0L) {
            return
        }
        LitePal.deleteAll(
            LocalFavoriteItem::class.java,
            "targetType = ? and threadId = ?",
            ReadingTargetType.THREAD.toString(),
            threadId.toString(),
        )
    }

    fun removeForum(forumName: String) {
        if (forumName.isBlank()) {
            return
        }
        LitePal.deleteAll(
            LocalFavoriteItem::class.java,
            "targetType = ? and forumName = ?",
            ReadingTargetType.FORUM.toString(),
            forumName,
        )
    }

    fun hasThread(threadId: Long): Boolean {
        if (threadId == 0L) {
            return false
        }
        return LitePal.where(
            "targetType = ? and threadId = ?",
            ReadingTargetType.THREAD.toString(),
            threadId.toString(),
        ).count(LocalFavoriteItem::class.java) > 0
    }

    fun hasForum(forumName: String): Boolean {
        if (forumName.isBlank()) {
            return false
        }
        return LitePal.where(
            "targetType = ? and forumName = ?",
            ReadingTargetType.FORUM.toString(),
            forumName,
        ).count(LocalFavoriteItem::class.java) > 0
    }

    fun getAll(page: Int = 0): List<LocalFavoriteItem> {
        return LitePal.order("timestamp desc")
            .limit(PAGE_SIZE)
            .offset(page * PAGE_SIZE)
            .find(LocalFavoriteItem::class.java)
    }

    fun getFlow(page: Int = 0): Flow<List<LocalFavoriteItem>> {
        return LitePal.order("timestamp desc")
            .limit(PAGE_SIZE)
            .offset(page * PAGE_SIZE)
            .findFlow()
    }

    fun clear() {
        LitePal.deleteAll(LocalFavoriteItem::class.java)
    }

    private fun saveOrUpdate(
        item: LocalFavoriteItem,
        condition: String,
        conditionArgs: Array<String>,
    ) {
        item.copy(timestamp = System.currentTimeMillis()).saveOrUpdate(condition, *conditionArgs)
    }
}
