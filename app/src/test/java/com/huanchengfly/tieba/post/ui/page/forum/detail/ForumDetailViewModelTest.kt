package com.huanchengfly.tieba.post.ui.page.forum.detail

import com.huanchengfly.tieba.post.repository.ForumRepository
import com.huanchengfly.tieba.post.ui.models.forum.ForumDetail
import com.huanchengfly.tieba.post.ui.models.forum.ForumIntroduction
import com.huanchengfly.tieba.post.ui.models.forum.ForumManager
import com.huanchengfly.tieba.post.ui.models.forum.ForumManagerGroup
import com.huanchengfly.tieba.post.ui.models.forum.FriendForum
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ForumDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repo = mockk<ForumRepository>()
    private val overview = ForumDetail(
        id = 123, name = "minecraft", avatar = "", intro = "基本简介",
        friendForums = listOf(FriendForum(1, "terraria", "")),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { repo.loadForumDetail("minecraft") } returns overview
        coEvery { repo.loadForumIntroduction(123) } returns ForumIntroduction("标语", emptyList())
        coEvery { repo.loadForumManagers(123) } returns emptyList()
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `slow introduction and failed team do not block basic info or friend forums`() = runTest(dispatcher) {
        val intro = CompletableDeferred<ForumIntroduction>()
        coEvery { repo.loadForumIntroduction(123) } coAnswers { intro.await() }
        coEvery { repo.loadForumManagers(123) } throws IOException("offline")
        val vm = ForumDetailViewModel("minecraft", repo)
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertTrue(vm.state.value.introductionLoading)
        assertNotNull(vm.state.value.managersError)
        assertEquals(overview.friendForums, vm.state.value.detail!!.friendForums)
        assertEquals("基本简介", vm.state.value.detail!!.intro)

        intro.complete(ForumIntroduction("完整标语", emptyList()))
        advanceUntilIdle()
        assertEquals("完整标语", vm.state.value.detail!!.slogan)
        assertFalse(vm.state.value.introductionLoading)
        assertNotNull(vm.state.value.managersError)
    }

    @Test
    fun `retrying team clears its error without refetching other sections`() = runTest(dispatcher) {
        coEvery { repo.loadForumManagers(123) } throws IOException("offline")
        val vm = ForumDetailViewModel("minecraft", repo)
        advanceUntilIdle()

        val groups = listOf(ForumManagerGroup("小吧主", listOf(ForumManager(2, "用户", ""))))
        coEvery { repo.loadForumManagers(123) } returns groups
        vm.reloadManagers()
        advanceUntilIdle()

        assertNull(vm.state.value.managersError)
        assertFalse(vm.state.value.managersLoading)
        assertEquals(groups, vm.state.value.detail!!.managerGroups)
        coVerify(exactly = 1) { repo.loadForumDetail(any()) }
        coVerify(exactly = 1) { repo.loadForumIntroduction(any()) }
    }

    @Test
    fun `failed complete introduction preserves overview description and can retry`() = runTest(dispatcher) {
        coEvery { repo.loadForumIntroduction(123) } throws IOException("offline")
        val vm = ForumDetailViewModel("minecraft", repo)
        advanceUntilIdle()
        assertEquals("基本简介", vm.state.value.detail!!.intro)
        assertNotNull(vm.state.value.introductionError)
        assertNull(vm.state.value.error)

        coEvery { repo.loadForumIntroduction(123) } returns ForumIntroduction("恢复后的标语", emptyList())
        vm.reloadIntroduction()
        advanceUntilIdle()
        assertNull(vm.state.value.introductionError)
        assertEquals("恢复后的标语", vm.state.value.detail!!.slogan)
    }

    @Test
    fun `overview failure can retry and load the remaining sections`() = runTest(dispatcher) {
        coEvery { repo.loadForumDetail("minecraft") } throws IOException("offline")
        val vm = ForumDetailViewModel("minecraft", repo)
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
        coVerify(exactly = 0) { repo.loadForumManagers(any()) }

        coEvery { repo.loadForumDetail("minecraft") } returns overview
        vm.reload()
        advanceUntilIdle()
        assertNull(vm.state.value.error)
        assertEquals(123L, vm.state.value.detail!!.id)
    }

    @Test
    fun `refresh cancels old section requests and keeps the new result`() = runTest(dispatcher) {
        val oldIntro = CompletableDeferred<ForumIntroduction>()
        coEvery { repo.loadForumIntroduction(123) } coAnswers { oldIntro.await() }
        val vm = ForumDetailViewModel("minecraft", repo)
        advanceUntilIdle()
        assertTrue(vm.state.value.introductionLoading)

        coEvery { repo.loadForumIntroduction(123) } returns ForumIntroduction("最新标语", emptyList())
        vm.reload()
        advanceUntilIdle()
        oldIntro.complete(ForumIntroduction("过期标语", emptyList()))
        advanceUntilIdle()

        assertEquals("最新标语", vm.state.value.detail!!.slogan)
        assertFalse(vm.state.value.introductionLoading)
        assertNull(vm.state.value.introductionError)
    }
}
