package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.models.database.ReadingProgress
import com.huanchengfly.tieba.post.utils.extension.findFlow
import kotlinx.coroutines.flow.Flow
import org.litepal.LitePal

object ReadingProgressUtil {
    const val PAGE_SIZE = 50

    fun save(progress: ReadingProgress) {
        if (progress.threadId == 0L || progress.postId == 0L || progress.threadTitle.isBlank()) {
            return
        }
        progress.copy(timestamp = System.currentTimeMillis())
            .saveOrUpdate("threadId = ?", progress.threadId.toString())
    }

    fun get(threadId: Long): ReadingProgress? {
        if (threadId == 0L) {
            return null
        }
        return LitePal.where("threadId = ?", threadId.toString())
            .findFirst(ReadingProgress::class.java)
    }

    fun remove(threadId: Long) {
        if (threadId == 0L) {
            return
        }
        LitePal.deleteAll(ReadingProgress::class.java, "threadId = ?", threadId.toString())
    }

    fun getAll(page: Int = 0): List<ReadingProgress> {
        return LitePal.order("timestamp desc")
            .limit(PAGE_SIZE)
            .offset(page * PAGE_SIZE)
            .find(ReadingProgress::class.java)
    }

    fun getFlow(page: Int = 0): Flow<List<ReadingProgress>> {
        return LitePal.order("timestamp desc")
            .limit(PAGE_SIZE)
            .offset(page * PAGE_SIZE)
            .findFlow()
    }
}
