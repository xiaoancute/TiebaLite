package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.abstractText
import com.huanchengfly.tieba.post.api.models.protos.plainText
import com.huanchengfly.tieba.post.models.database.Block
import com.huanchengfly.tieba.post.models.database.Block.Companion.getKeywords
import org.litepal.LitePal
import org.litepal.extension.delete
import org.litepal.extension.findAllAsync
import java.util.regex.Pattern

object BlockManager {
    private val blockList: MutableList<Block> = mutableListOf()

    val blackList: List<Block>
        get() = blockList.filter { it.category == Block.CATEGORY_BLACK_LIST }

    val whiteList: List<Block>
        get() = blockList.filter { it.category == Block.CATEGORY_WHITE_LIST }

    fun addBlock(block: Block) {
        block.save()
        blockList.add(block)
    }

    fun addBlockAsync(
        block: Block,
        callback: ((Boolean) -> Unit)? = null,
    ) {
        block.saveAsync()
            .listen {
                callback?.invoke(it)
                blockList.add(block)
            }
    }

    fun removeBlock(id: Long) {
        LitePal.delete<Block>(id)
        blockList.removeAll { it.id == id }
    }

    fun init() {
        LitePal.findAllAsync<Block>().listen { blocks ->
            blockList.addAll(blocks)
        }
    }

    fun shouldBlock(content: String): Boolean {
        // 支持正则表达式的屏蔽判断
        val isWhite = whiteList.any { block ->
            block.type == Block.TYPE_KEYWORD && block.getKeywords().all { keyword ->
                if (block.isRegex) {
                    try {
                        Pattern.compile(keyword).matcher(content).find()
                    } catch (_: Exception) {
                        false
                    }
                } else {
                    content.contains(keyword)
                }
            }
        }
        if (isWhite)
            return false
        val isBlack = blackList.any { block ->
            block.type == Block.TYPE_KEYWORD && block.getKeywords().all { keyword ->
                if (block.isRegex) {
                    try {
                        Pattern.compile(keyword).matcher(content).find()
                    } catch (_: Exception) {
                        false // 如果正则表达式非法则忽略
                    }
                } else {
                    content.contains(keyword)
                }
            }
        }
        return isBlack
    }

    fun shouldBlock(userId: Long = 0L, userName: String? = null): Boolean {
        val isWhite = whiteList.any { block ->
            if (block.isRegex) {
                return false
            }
            block.type == Block.TYPE_USER
                    && (block.uid == userId.toString() || block.username == userName)
        }
        if (isWhite)
            return false
        val isBlack = blackList.any { block ->
            if (block.isRegex) {
                return false
            }
            block.type == Block.TYPE_USER
                    && (block.uid == userId.toString() || block.username == userName)
        }
        return isBlack
    }

    fun ThreadInfo.shouldBlock(): Boolean =
        shouldBlock(title) || shouldBlock(abstractText) || shouldBlock(
            authorId.takeIf { it != 0L } ?: (author?.id ?: -1),
            author?.name?.ifEmpty { author.nameShow })

    fun Post.shouldBlock(): Boolean =
        shouldBlock(content.plainText) || shouldBlock(
            author_id.takeIf { it != 0L } ?: (author?.id ?: -1),
            author?.name?.ifEmpty { author.nameShow })

    fun SubPostList.shouldBlock(): Boolean =
        shouldBlock(content.plainText) || shouldBlock(
            author_id.takeIf { it != 0L } ?: (author?.id ?: -1),
            author?.name?.ifEmpty { author.nameShow })

    fun MessageListBean.MessageInfoBean.shouldBlock(): Boolean =
        shouldBlock(content.orEmpty()) || shouldBlock(
            this.replyer?.id?.toLongOrNull() ?: -1,
            this.replyer?.name?.ifEmpty { this.replyer.nameShow }
        )
}