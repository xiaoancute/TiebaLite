package com.huanchengfly.tieba.post.ui.page.threadstore

import androidx.lifecycle.SavedStateHandle
import com.huanchengfly.tieba.post.repository.ThreadStoreRepository
import com.huanchengfly.tieba.post.ui.models.Author
import com.huanchengfly.tieba.post.ui.models.ThreadStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ThreadStoreViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repo = mockk<ThreadStoreRepository>()
    private fun page(start: Long, count: Int = 20) = (start until start + count).map(::thread)

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { repo.load(page = any()) } returns emptyList()
        coEvery { repo.load(page = 0) } returns page(1)
    }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `normal pagination includes the twenty records on page one`() = runTest(dispatcher) {
        coEvery { repo.load(page = 1) } returns page(21)
        val vm = ThreadStoreViewModel(repo, SavedStateHandle())
        advanceUntilIdle()
        assertEquals(0, vm.currentState.currentPage)
        coVerify(exactly = 0) { repo.load(page = 1) }
        vm.onLoadMore()
        advanceUntilIdle()
        assertEquals((1L..40L).toList(), vm.currentState.data.map { it.id })
        coVerify(exactly = 0) { repo.load(page = 2) }
    }

    @Test fun `search finds a match beyond the first page without requiring scrolling`() = runTest(dispatcher) {
        coEvery { repo.load(page = 1) } returns page(21)
        coEvery { repo.load(page = 2) } returns listOf(thread(41).copy(title = "深处的目标"))
        val vm = ThreadStoreViewModel(repo, SavedStateHandle())
        advanceUntilIdle()
        vm.onQueryChange("目标")
        advanceUntilIdle()
        assertEquals(listOf(41L), vm.currentState.data.searchCollections(vm.query.value).map { it.id })
        assertFalse(vm.currentState.hasMore)
        assertEquals(41, vm.currentState.data.size)
    }

    @Test fun `failed page keeps partial matches and retries the same page`() = runTest(dispatcher) {
        coEvery { repo.load(page = 1) } throws IOException("offline")
        val vm = ThreadStoreViewModel(repo, SavedStateHandle())
        advanceUntilIdle()
        vm.onQueryChange("thread")
        advanceUntilIdle()
        assertEquals(20, vm.currentState.data.size)
        assertTrue(vm.currentState.hasMore)
        assertNotNull(vm.currentState.loadMoreError)
        assertNull(vm.currentState.error)
        coEvery { repo.load(page = 1) } returns page(21, 1)
        vm.onRetry()
        advanceUntilIdle()
        assertNull(vm.currentState.loadMoreError)
        assertFalse(vm.currentState.hasMore)
        assertEquals(21, vm.currentState.data.size)
    }

    @Test fun `clearing query cancels further fetching and keeps the loaded range`() = runTest(dispatcher) {
        val pendingPage = CompletableDeferred<List<ThreadStore>>()
        coEvery { repo.load(page = 1) } coAnswers { pendingPage.await() }
        val vm = ThreadStoreViewModel(repo, SavedStateHandle())
        advanceUntilIdle()
        vm.onQueryChange("thread")
        runCurrent()
        assertTrue(vm.currentState.isLoadingMore)
        vm.onQueryChange("")
        pendingPage.complete(page(21))
        advanceUntilIdle()
        assertEquals(20, vm.currentState.data.size)
        assertFalse(vm.currentState.isLoadingMore)
        assertTrue(vm.currentState.hasMore)
        assertNull(vm.currentState.loadMoreError)
        coVerify(exactly = 0) { repo.load(page = 2) }
    }

    @Test fun `refresh discards an older pending page and keeps the current search`() = runTest(dispatcher) {
        val pendingPage = CompletableDeferred<List<ThreadStore>>()
        coEvery { repo.load(page = 1) } coAnswers { pendingPage.await() }
        val vm = ThreadStoreViewModel(repo, SavedStateHandle())
        advanceUntilIdle()
        vm.onQueryChange("new")
        runCurrent()
        coEvery { repo.load(page = 0) } returns listOf(thread(99).copy(title = "new"))
        vm.onRefresh()
        pendingPage.complete(page(21))
        advanceUntilIdle()
        assertEquals(listOf(99L), vm.currentState.data.map { it.id })
        assertEquals("new", vm.query.value)
        assertFalse(vm.currentState.hasMore)
        assertFalse(vm.currentState.isLoadingMore)
    }

    @Test fun `repeating full page is reported as incomplete instead of looping`() = runTest(dispatcher) {
        coEvery { repo.load(page = 1) } returns page(1)
        val vm = ThreadStoreViewModel(repo, SavedStateHandle())
        advanceUntilIdle()
        vm.onQueryChange("thread")
        advanceUntilIdle()
        assertTrue(vm.currentState.hasMore)
        assertNotNull(vm.currentState.loadMoreError)
        assertEquals(20, vm.currentState.data.size)
        coVerify(exactly = 0) { repo.load(page = 2) }
    }

    @Test fun `overlapping pages deduplicate entries and continue to the end`() = runTest(dispatcher) {
        coEvery { repo.load(page = 1) } returns page(20)
        coEvery { repo.load(page = 2) } returns page(40, 1)
        val vm = ThreadStoreViewModel(repo, SavedStateHandle(mapOf("collection_query" to "thread")))
        advanceUntilIdle()
        assertEquals((1L..40L).toList(), vm.currentState.data.map { it.id })
        assertFalse(vm.currentState.hasMore)
    }

    @Test fun `failed refresh of a complete list can retry from the beginning`() = runTest(dispatcher) {
        coEvery { repo.load(page = 0) } returns page(1, 1)
        val vm = ThreadStoreViewModel(repo, SavedStateHandle())
        advanceUntilIdle()
        coEvery { repo.load(page = 0) } throws IOException("offline")
        vm.onRefresh()
        advanceUntilIdle()
        assertNotNull(vm.currentState.loadMoreError)
        assertEquals(listOf(1L), vm.currentState.data.map { it.id })
        coEvery { repo.load(page = 0) } returns page(2, 1)
        vm.onRetry()
        advanceUntilIdle()
        assertEquals(listOf(2L), vm.currentState.data.map { it.id })
        assertNull(vm.currentState.loadMoreError)
    }

    @Test fun `changing a completed search reuses data without restarting requests`() = runTest(dispatcher) {
        coEvery { repo.load(page = 0) } returns page(1, 1)
        val handle = SavedStateHandle()
        val vm = ThreadStoreViewModel(repo, handle)
        advanceUntilIdle()
        vm.onQueryChange("first")
        vm.onQueryChange("second")
        advanceUntilIdle()
        coVerify(exactly = 1) { repo.load(page = any()) }
        assertEquals("second", handle.get<String>("collection_query"))
    }

    @Test fun `deletion rebuilds page offsets so the next item is not skipped`() = runTest(dispatcher) {
        coEvery { repo.load(page = 1) } returns page(21, 1)
        val vm = ThreadStoreViewModel(repo, SavedStateHandle(mapOf("collection_query" to "thread")))
        advanceUntilIdle()
        val deleted = vm.currentState.data.first()
        coEvery { repo.remove(deleted) } returns Result.success(Unit)
        coEvery { repo.load(page = 0) } returns page(2)
        coEvery { repo.load(page = 1) } returns emptyList()
        vm.onDelete(deleted)
        advanceUntilIdle()
        assertEquals((2L..21L).toList(), vm.currentState.data.map { it.id })
        assertFalse(vm.currentState.hasMore)
        assertNull(vm.currentState.loadMoreError)
    }

    @Test fun `search matches metadata literally and preserves deleted entries`() {
        val entries = listOf(
            thread(1).copy(title = "100%_[攻略]", isDeleted = true),
            thread(2).copy(forumName = "Minecraft"),
            thread(3).copy(author = Author(3, "某个作者", "")),
        )
        assertEquals(listOf(1L), entries.searchCollections("%_[").map { it.id })
        assertEquals(listOf(2L), entries.searchCollections("  MINECRAFT ").map { it.id })
        assertEquals(listOf(3L), entries.searchCollections("作者").map { it.id })
        assertEquals(listOf(2L), entries.searchCollections("2").map { it.id })
        assertEquals(entries, entries.searchCollections(" "))
    }

    private fun thread(id: Long) = ThreadStore(
        id = id, title = "thread $id", forumName = "吧", isDeleted = false,
        maxPid = 0, markPid = 0, postNo = 0, count = 0,
        author = Author(id, "author", ""),
    )
}
