package com.huanchengfly.tieba.post.repository

import androidx.annotation.VisibleForTesting
import androidx.core.util.Predicate
import com.huanchengfly.tieba.post.arch.shareInBackground
import com.huanchengfly.tieba.post.models.database.BlockForum
import com.huanchengfly.tieba.post.models.database.BlockKeyword
import com.huanchengfly.tieba.post.models.database.BlockUser
import com.huanchengfly.tieba.post.models.database.dao.BlockDao
import com.huanchengfly.tieba.post.models.database.dao.TypedKeyword
import com.huanchengfly.tieba.post.repository.source.network.ForumBlockNetworkDataSource
import com.huanchengfly.tieba.post.repository.source.network.OfficialBlockForum
import com.huanchengfly.tieba.post.repository.source.network.SearchNetworkDataSource
import com.huanchengfly.tieba.post.utils.AccountUtil
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Block Repository that manages blocking rule data.
 * */
@Singleton
class BlockRepository @Inject constructor(
    private val localDataSource: BlockDao
) {
    private val officialForumIds = ConcurrentHashMap<String, Long>()

    /**
     * Blacklisted predicates.
     * */
    val blacklist: SharedFlow<List<Predicate<String>>> = localDataSource.observeTypedKeywords(whitelisted = false)
        .map(::mapToPredicates)
        .shareInBackground(started = SharingStarted.Lazily)

    /**
     * Whitelisted predicates. Note that whitelist has the highest priority.
     * */
    val whitelist: SharedFlow<List<Predicate<String>>> = localDataSource.observeTypedKeywords(whitelisted = true)
        .map(::mapToPredicates)
        .shareInBackground(started = SharingStarted.Lazily)

    suspend fun upsertForum(forum: BlockForum) = withContext(NonCancellable) {
        localDataSource.upsertForum(forum)
    }

    suspend fun syncOfficialForums() {
        if (AccountUtil.getInstance().currentAccount.first() == null) return

        val forums = ForumBlockNetworkDataSource.loadDislikeForums()
        cacheOfficialForums(forums)
        if (forums.isNotEmpty()) {
            withContext(NonCancellable) {
                localDataSource.insertForums(
                    forums = forums.map { BlockForum(normalizeForumName(it.name)) }.toTypedArray()
                )
            }
        }
    }

    suspend fun upsertSyncedForum(forumName: String) {
        val normalizedName = normalizeForumName(forumName)
        ignoreRemoteSyncFailure {
            if (AccountUtil.getInstance().currentAccount.first() != null) {
                val forumId = findForumId(normalizedName)
                if (forumId != null) {
                    ForumBlockNetworkDataSource.submitDislikeForum(forumId)
                    officialForumIds[normalizedName] = forumId
                }
            }
        }
        upsertForum(BlockForum(normalizedName))
    }

    suspend fun deleteForum(forumName: String) = withContext(NonCancellable) {
        localDataSource.deleteForum(forumName)
    }

    suspend fun deleteSyncedForum(forumName: String) {
        val normalizedName = normalizeForumName(forumName)
        ignoreRemoteSyncFailure {
            if (AccountUtil.getInstance().currentAccount.first() != null) {
                val forumId = findForumId(normalizedName)
                if (forumId != null) {
                    ForumBlockNetworkDataSource.cancelDislikeForum(forumId)
                    officialForumIds.remove(normalizedName)
                }
            }
        }
        deleteForum(normalizedName)
    }

    suspend fun deleteForums(forumNames: List<String>) = withContext(NonCancellable) {
        localDataSource.deleteForums(forumNames)
    }

    suspend fun deleteSyncedForums(forumNames: List<String>) {
        forumNames.forEach { deleteSyncedForum(it) }
    }

    suspend fun addKeyword(keyword: String, isRegex: Boolean, whitelisted: Boolean) = withContext(NonCancellable) {
        localDataSource.addKeyword(keyword, isRegex, whitelisted)
    }

    suspend fun deleteKeyword(keyword: BlockKeyword) = withContext(NonCancellable) {
        localDataSource.deleteKeywordById(keyword.id)
    }

    suspend fun deleteKeywords(keywords: List<BlockKeyword>) = withContext(NonCancellable) {
        localDataSource.deleteKeywordByIdList(idList = keywords.map { it.id })
    }

    suspend fun upsertUser(user: BlockUser) = withContext(NonCancellable) {
        localDataSource.upsertUser(user)
    }

    suspend fun deleteUser(uid: Long) = withContext(NonCancellable) {
        localDataSource.deleteUserById(uid)
    }

    suspend fun deleteUsers(users: List<BlockUser>) = withContext(NonCancellable) {
        localDataSource.deleteUserByIdList(uidList = users.map { it.uid })
    }

    fun observeForums(): Flow<List<String>> = localDataSource.observeForums()

    fun observeUser(uid: Long): Flow<Boolean?> = localDataSource.observeUser(uid)

    fun observeUsers(whitelisted: Boolean): Flow<List<BlockUser>> = localDataSource.observeUsers(whitelisted)

    fun observeKeyword(whitelisted: Boolean): Flow<List<BlockKeyword>> = localDataSource.observeKeywordRules(whitelisted)

    private suspend fun findForumId(forumName: String): Long? {
        officialForumIds[forumName]?.let { return it }

        val data = SearchNetworkDataSource.searchForum(forumName)
        val matches = buildList {
            data.exactMatch?.let(::add)
            addAll(data.fuzzyMatch)
        }
        return matches.firstOrNull {
            normalizeForumName(it.forumName.orEmpty()) == forumName ||
                    normalizeForumName(it.forumNameShow.orEmpty()) == forumName
        }?.forumId ?: data.exactMatch?.forumId
    }

    private fun cacheOfficialForums(forums: List<OfficialBlockForum>) {
        officialForumIds.clear()
        officialForumIds.putAll(
            forums.associate { forum -> normalizeForumName(forum.name) to forum.id }
        )
    }

    private suspend inline fun ignoreRemoteSyncFailure(crossinline block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            // Local rules still work if the official endpoint or forum lookup fails.
        }
    }

    /**
     * @return is user or contents blocked
     */
    suspend fun isBlocked(uid: Long, vararg contents: String): Boolean {
        val userRule = localDataSource.getUser(uid)
        // user rule matched, skip keywords check
        return if (userRule != null) {
            !userRule.whitelisted
        } else {
            isBlocked(blacklist.first(), whitelist.first(), *contents)
        }
    }

    suspend fun isBlocked(uid: Long, contents: Array<String>, blockWaterPost: Boolean): Boolean {
        val userRule = localDataSource.getUser(uid)
        // user rule matched, skip keyword and built-in content checks
        return if (userRule != null) {
            !userRule.whitelisted
        } else {
            isBlocked(
                blacklist.first(),
                whitelist.first(),
                blockWaterPost,
                *contents,
            )
        }
    }

    suspend fun isBlocked(forumName: String, uid: Long, vararg contents: String): Boolean {
        return if (localDataSource.getForum(forumName) != null) {
            true
        } else {
            isBlocked(uid, *contents)
        }
    }

    suspend fun isBlocked(
        forumName: String,
        uid: Long,
        contents: Array<String>,
        blockWaterPost: Boolean,
    ): Boolean {
        return if (localDataSource.getForum(forumName) != null) {
            true
        } else {
            isBlocked(uid, contents, blockWaterPost)
        }
    }

    companion object {

        fun normalizeForumName(name: String): String {
            return name.trim().run { if (endsWith("吧")) substring(0, lastIndex) else this }
        }

        private class KeywordPredicate(val keyword: String): Predicate<String> {
            override fun test(t: String?): Boolean {
                return !t.isNullOrEmpty() && t.contains(keyword, ignoreCase = true)
            }

            override fun toString(): String = keyword
        }

        private class RegexPredicate(pattern: String): Predicate<String> {
            private val regex = pattern.toRegex()

            override fun test(t: String?): Boolean {
                return !t.isNullOrEmpty() && regex.containsMatchIn(input = t)
            }

            override fun toString(): String = regex.pattern
        }

        // Convert Keywords to Predicates
        private fun mapToPredicates(rules: List<TypedKeyword>): List<Predicate<String>> {
            return rules.map {
                if (it.isRegex) RegexPredicate(pattern = it.keyword) else KeywordPredicate(keyword = it.keyword)
            }
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun Array<out String>.anyMatches(predicates: List<Predicate<String>>): Boolean {
            if (isEmpty() || predicates.isEmpty()) return false

            forEach { content ->
                if (predicates.any { it.test(content) }) {
                    return true
                }
            }
            return false
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun isBlocked(blacklist: List<Predicate<String>>, whitelist: List<Predicate<String>>, vararg contents: String): Boolean {
            // whitelist has the highest priority
            return if (contents.anyMatches(predicates = whitelist)) {
                false
            } else {
                contents.anyMatches(predicates = blacklist)
            }
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun isBlocked(
            blacklist: List<Predicate<String>>,
            whitelist: List<Predicate<String>>,
            blockWaterPost: Boolean,
            vararg contents: String,
        ): Boolean {
            return if (contents.anyMatches(predicates = whitelist)) {
                false
            } else {
                contents.anyMatches(predicates = blacklist) ||
                        blockWaterPost && WaterPostBlocker.isWaterPost(*contents)
            }
        }
    }
}
