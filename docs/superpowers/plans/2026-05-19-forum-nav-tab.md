# 吧内分区(NavTab)对齐网页版 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Spec:** `docs/superpowers/specs/2026-05-19-forum-nav-tab-design.md`

**Goal:** 把 ForumPage 顶部"最新/精品"双 Tab 改为对齐网页版的平级 NavTab 标签栏,精华子分类(GoodClassify Chip)保留。

**2026-05-20 抓包修正:** App protobuf 的 `nav_tab_info` 在大吧里不稳定/不完整,不能作为唯一来源。网页版真实链路是:
- 首次进吧请求 `POST /c/f/frs/page_pc`,返回 `nav_tab_info.tab` 里的完整 8 项 NavTab、PC `anti.tbs` 和 `frs_common_info`。
- `精华/热门/最新` 这类普通 tab 继续请求 `POST /c/f/frs/page_pc`,body 携带 `tab_id/tab_type/tab_name/is_good/sort_type/forum_id`。
- `开黑/交友/战队丨圈子/视频` 这类 `is_general_tab=1` 的分区请求 `POST /c/f/frs/generalTabList_pc`,body 需要完整 `frs_common_info`。
- PC 签名算法为按 key 字典序拼接 `key=value`,追加 secret `36770b1f34c9bbf2e7d1a99d2b82fa9e`,再取小写 MD5。

**Architecture:**
- 协议层:`FrsPage.frsTabInfo` / `navTabInfo` 只保留 fallback 能力;完整 tab 来源切到 PC `page_pc`。
- PC 数据层:新增 `PcFrsPageResponse` 最小模型、PC sign helper、`page_pc` / `generalTabList_pc` Retrofit 方法。
- 数据层:`ForumData` 加 `navTabs: List<NavTab>` 和 `pcFrsCommonInfo`;`ForumRepository.cache` 从 `(normal, good)` 二元改为 `Map<tabId, ThreadItemList>`。
- ViewModel:`ForumThreadListViewModel` 的 AssistedInject 入参 `ForumType` → `NavTab`;`hiltViewModel(key)` 含 `forumId+tabId`。
- UI:`ForumTab.kt` 重写为动态 `LazyRow`;`ForumPage` 的 HorizontalPager 按 `navTabs.size` 动态出页;ClassifyTabs 显隐改用 `currentTab.isEssence && goodClassifies.size > 1`。

**Tech Stack:** Kotlin 2.3.20 / Jetpack Compose / Hilt + AssistedInject / Wire (protobuf) / JUnit4 + MockK / Gradle KTS

**Decomposition principle:** 每个 task 改一层,保证编译通过 + 现有功能不退化;最后一个 task 才删旧路径。

---

## File Structure

新增:
- `app/src/main/java/com/huanchengfly/tieba/post/ui/models/forum/NavTab.kt` — 数据类 + 常量(Fallback / ESSENCE_TAB_NAME)
- `app/src/main/java/com/huanchengfly/tieba/post/repository/ForumMappers.kt` — `NavTabInfo?.toNavTabs()` 纯函数(便于单测)
- `app/src/test/java/com/huanchengfly/tieba/post/repository/ForumMappersTest.kt` — `toNavTabs()` 单测

修改:
- `app/src/main/java/com/huanchengfly/tieba/post/ui/models/forum/ForumData.kt` — 加 `navTabs` 字段 + 同步 `copy()`
- `app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/ITiebaApi.kt` — 接口签名
- `app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/impls/MixedTiebaApiImpl.kt` — 实现
- `app/src/main/java/com/huanchengfly/tieba/post/repository/source/network/ForumNetworkDataSource.kt` — 透传
- `app/src/main/java/com/huanchengfly/tieba/post/repository/ForumRepository.kt` — 公开 API + 缓存重构 + `toData()` 调 `toNavTabs()`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/threadlist/ForumThreadListViewModel.kt` — `ForumType` → `NavTab`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/threadlist/ForumThreadListPage.kt` — 调用方
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumViewModel.kt` — `isGood` → `tabId`
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumPage.kt` — 动态 Pager + ClassifyTabs 显隐
- `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumTab.kt` — 重写

---

## Task 1: 新增 NavTab 模型 + ForumData.navTabs 字段

**Files:**
- Create: `app/src/main/java/com/huanchengfly/tieba/post/ui/models/forum/NavTab.kt`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/ui/models/forum/ForumData.kt`

- [ ] **Step 1:** 新建 `NavTab.kt`,内容:

```kotlin
package com.huanchengfly.tieba.post.ui.models.forum

import androidx.compose.runtime.Immutable

/**
 * 网页版"吧内分区"一项。对应协议 [FrsTabInfo].
 *
 * @param tabId 分区 ID,等同网页 URL 里的 `?tab=<id>`。
 *   `0` 是 [Fallback] 占位,用于协议未返回 nav_tab_info 的小吧。
 * @param tabType 协议原始 tabType。语义未文档化,先透传保存以便后续判定。
 * @param isDefault 协议侧标记为默认选中(`FrsTabInfo.isDefault == 1`)。
 */
@Immutable
data class NavTab(
    val tabId: Int,
    val tabName: String,
    val tabType: Int,
    val isDefault: Boolean,
) {
    /**
     * 是否为"精华类"分区。
     *
     * TODO(impl): tabType / tabCode 取值未文档化,先按 tabName 启发式判定。
     * 等抓包确认精华 tab 的 tabType 数值或 tabCode 后,改成稳健判定。
     */
    val isEssence: Boolean get() = tabName == ESSENCE_TAB_NAME

    companion object {
        const val ESSENCE_TAB_NAME = "精华"
        const val FALLBACK_TAB_ID = 0

        /** 协议未返回 nav_tab_info / 列表为空时的占位,行为等同旧"最新"。 */
        val Fallback: NavTab = NavTab(
            tabId = FALLBACK_TAB_ID,
            tabName = "全部",
            tabType = 0,
            isDefault = true,
        )
    }
}
```

- [ ] **Step 2:** 在 `ForumData.kt` 加 `navTabs` 字段。改完整体如下(保留手写 `copy()` 的现有风格):

```kotlin
package com.huanchengfly.tieba.post.ui.models.forum

import androidx.compose.runtime.Immutable

// Classify name, Classify ID
typealias GoodClassify = Pair<String, Int>

@Immutable
/*data */class ForumData(
    val id: Long,
    val avatar: String,
    val name: String,
    val forumRuleTitle: String?,
    val slogan: String?,
    val tbs: String?,
    val liked: Boolean,
    val signed: Boolean,
    val signedDays: Int,
    val signedRank: Int,
    val level: Int,
    val levelName: String,
    val score: Int,
    val scoreLevelUp: Int,
    val members: Int,
    val threads: Int,
    val posts: Int,
    val goodClassifies: List<GoodClassify>?,
    val navTabs: List<NavTab> = emptyList(),
) {

    val levelProgress: Float
        get() = score.toFloat() / scoreLevelUp.coerceAtLeast(1)

    fun copy(
        avatar: String = this.avatar,
        forumRuleTitle: String? = this.forumRuleTitle,
        slogan: String? = this.slogan,
        tbs: String? = this.tbs,
        liked: Boolean = this.liked,
        signed: Boolean = this.signed,
        signedDays: Int = this.signedDays,
        signedRank: Int = this.signedRank,
        level: Int = this.level,
        levelName: String = this.levelName,
        score: Int = this.score,
        scoreLevelUp: Int = this.scoreLevelUp,
        members: Int = this.members,
        threads: Int = this.threads,
        posts: Int = this.posts,
        goodClassifies: List<GoodClassify>? = this.goodClassifies,
        navTabs: List<NavTab> = this.navTabs,
    ) = ForumData(
        id = this.id,
        avatar = avatar,
        name = this.name,
        forumRuleTitle = forumRuleTitle,
        slogan = slogan,
        tbs = tbs,
        liked = liked,
        signed = signed,
        signedDays = signedDays,
        signedRank = signedRank,
        level = level,
        levelName = levelName,
        score = score,
        scoreLevelUp = scoreLevelUp,
        members = members,
        threads = threads,
        posts = posts,
        goodClassifies = goodClassifies,
        navTabs = navTabs,
    )
}
```

- [ ] **Step 3:** 编译验证

Run: `./gradlew :app:compileDebugKotlin --no-configuration-cache`
Expected: BUILD SUCCESSFUL.(navTabs 默认空,既有调用方都不需要传参)

- [ ] **Step 4:** Commit

```bash
git add app/src/main/java/com/huanchengfly/tieba/post/ui/models/forum/NavTab.kt \
        app/src/main/java/com/huanchengfly/tieba/post/ui/models/forum/ForumData.kt
git commit -m "feat: ForumData 加 navTabs 字段, 新增 NavTab 模型"
```

---

## Task 2: 写 `NavTabInfo.toNavTabs()` 映射 + 单测(TDD)

**Files:**
- Create: `app/src/main/java/com/huanchengfly/tieba/post/repository/ForumMappers.kt`
- Create: `app/src/test/java/com/huanchengfly/tieba/post/repository/ForumMappersTest.kt`

把映射逻辑独立成纯函数,绕过 `FrsPageResponseData` 字段繁多的构造负担。

- [ ] **Step 1:** 写失败的测试(新文件):

`app/src/test/java/com/huanchengfly/tieba/post/repository/ForumMappersTest.kt`:

```kotlin
package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.FrsTabInfo
import com.huanchengfly.tieba.post.api.models.protos.frsPage.NavTabInfo
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
import org.junit.Assert.assertEquals
import org.junit.Test

class ForumMappersTest {

    @Test
    fun `null nav_tab_info maps to single fallback tab`() {
        val result = (null as NavTabInfo?).toNavTabs()
        assertEquals(listOf(NavTab.Fallback), result)
    }

    @Test
    fun `empty tab list maps to single fallback tab`() {
        val result = NavTabInfo(tab = emptyList()).toNavTabs()
        assertEquals(listOf(NavTab.Fallback), result)
    }

    @Test
    fun `normal multi-tab mapping preserves order and fields`() {
        val info = NavTabInfo(
            tab = listOf(
                FrsTabInfo(tabId = 301, tabName = "精华", tabType = 2, isDefault = 0),
                FrsTabInfo(tabId = 1, tabName = "热门", tabType = 1, isDefault = 1),
                FrsTabInfo(tabId = 503, tabName = "最新", tabType = 3, isDefault = 0),
            )
        )

        val result = info.toNavTabs()

        assertEquals(
            listOf(
                NavTab(tabId = 301, tabName = "精华", tabType = 2, isDefault = false),
                NavTab(tabId = 1, tabName = "热门", tabType = 1, isDefault = true),
                NavTab(tabId = 503, tabName = "最新", tabType = 3, isDefault = false),
            ),
            result
        )
    }

    @Test
    fun `when no tab has isDefault=1, the first tab becomes default`() {
        val info = NavTabInfo(
            tab = listOf(
                FrsTabInfo(tabId = 10, tabName = "A", tabType = 0, isDefault = 0),
                FrsTabInfo(tabId = 20, tabName = "B", tabType = 0, isDefault = 0),
            )
        )

        val result = info.toNavTabs()

        assertEquals(true, result[0].isDefault)
        assertEquals(false, result[1].isDefault)
    }

    @Test
    fun `essence tab is detected by name`() {
        val info = NavTabInfo(
            tab = listOf(FrsTabInfo(tabId = 301, tabName = "精华", tabType = 2, isDefault = 1))
        )
        assertEquals(true, info.toNavTabs().single().isEssence)
    }
}
```

- [ ] **Step 2:** 跑测试,确认 fail

Run: `./gradlew :app:testDebugUnitTest --tests "com.huanchengfly.tieba.post.repository.ForumMappersTest" --no-configuration-cache`
Expected: 编译失败 — `Unresolved reference: toNavTabs`。

- [ ] **Step 3:** 新建 `ForumMappers.kt` 实现:

`app/src/main/java/com/huanchengfly/tieba/post/repository/ForumMappers.kt`:

```kotlin
package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.models.protos.frsPage.NavTabInfo
import com.huanchengfly.tieba.post.ui.models.forum.NavTab

/**
 * 把协议 [NavTabInfo] 的主标签数组映射成 UI 层 [NavTab] 列表.
 *
 * 行为:
 * - 接收 null / 空列表 → 返回 `[NavTab.Fallback]` 占位.
 * - 列表非空但**没有任何 isDefault=1** → 把第一个标记为 default.
 * - 协议 `tabId` / `tabName` / `tabType` / `isDefault` 直传.
 */
internal fun NavTabInfo?.toNavTabs(): List<NavTab> {
    val tabs = this?.tab.orEmpty()
    if (tabs.isEmpty()) return listOf(NavTab.Fallback)

    val anyDefault = tabs.any { it.isDefault == 1 }
    return tabs.mapIndexed { index, t ->
        NavTab(
            tabId = t.tabId,
            tabName = t.tabName,
            tabType = t.tabType,
            isDefault = if (anyDefault) t.isDefault == 1 else index == 0,
        )
    }
}
```

- [ ] **Step 4:** 跑测试,确认 pass

Run: `./gradlew :app:testDebugUnitTest --tests "com.huanchengfly.tieba.post.repository.ForumMappersTest" --no-configuration-cache`
Expected: 5 个测试全 PASS.

- [ ] **Step 5:** 在 `ForumRepository.kt` 的 `toData()` 里调用 `toNavTabs()`(其余字段不动)

修改 `app/src/main/java/com/huanchengfly/tieba/post/repository/ForumRepository.kt:281-306`:在 ForumData 构造里追加 `navTabs = nav_tab_info.toNavTabs()`:

```kotlin
private fun FrsPageResponseData.toData(): ForumData = forum!!.let {
    ForumData(
        id = it.id,
        avatar = it.avatar,
        name = it.name,
        forumRuleTitle = forum_rule?.run {
            title.takeIf { t -> has_forum_rule == 1 && t.isNotEmpty() }
        },
        slogan = forum.slogan.trim().takeUnless { slogan -> slogan.isEmpty() },
        tbs = anti?.tbs?.takeUnless { tbs -> tbs.isEmpty() || tbs.isBlank() },
        liked = it.is_like == 1,
        signed = it.sign_in_info?.user_info?.is_sign_in == 1,
        signedDays = it.sign_in_info?.user_info?.cont_sign_num ?: 0,
        signedRank = it.sign_in_info?.user_info?.user_sign_rank ?: 0,
        level = it.user_level,
        levelName = it.level_name,
        score = it.cur_score,
        scoreLevelUp = it.levelup_score,
        members = it.member_num,
        threads = it.thread_num,
        posts = it.post_num,
        goodClassifies = it.good_classify
            .takeUnless { c -> c.size <= 1 }
            ?.map { c -> GoodClassify(c.class_name, c.class_id) },
        navTabs = nav_tab_info.toNavTabs(),
    )
}
```

- [ ] **Step 6:** 编译验证

Run: `./gradlew :app:compileDebugKotlin --no-configuration-cache`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7:** Commit

```bash
git add app/src/main/java/com/huanchengfly/tieba/post/repository/ForumMappers.kt \
        app/src/main/java/com/huanchengfly/tieba/post/repository/ForumRepository.kt \
        app/src/test/java/com/huanchengfly/tieba/post/repository/ForumMappersTest.kt
git commit -m "feat: 映射 nav_tab_info 到 ForumData.navTabs"
```

---

## Task 3: 协议层签名替换 `goodClassifyId` → `tabId/isEssence/subClassifyId`

**Files:**
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/ITiebaApi.kt:1282-1297`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/impls/MixedTiebaApiImpl.kt:1031-1077`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/repository/source/network/ForumNetworkDataSource.kt:40-62`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/repository/ForumRepository.kt:74-114`(仅 frsPage 私有调用方与公开方法签名适配 —— 内部含义先保持不变,公开方法在 Task 4 重构)

策略:此 task 内,**公开方法 (`loadPage` / `loadGoodPage` / `loadMorePage` / `loadMoreGood`) 签名不变**(让上层 ViewModel 继续编译),只在私有 `frsPage` 内部把入参转译到新协议入参。可在一次 commit 内编译通过。

- [ ] **Step 1:** 修改 `ITiebaApi.kt:1282-1297` 的 `frsPage` 签名:

```kotlin
    /**
     * 吧页面
     *
     * @param forumName 吧名
     * @param page 页码 (从 1 开始)
     * @param loadType 加载类型 (1 - 下拉刷新 2 - 加载更多)
     * @param sortType 排序
     * @param tabId 网页 URL 里 `?tab=` 对应的分区 ID; `0` 表示默认/无分区
     * @param isEssence 当前 tab 是否为"精华类"; 是则置 `is_good=1`,
     *                  且 [subClassifyId] 表示选中的精华子分类
     * @param subClassifyId 精华子分类 `class_id`; 仅 [isEssence]=true 时有效
     */
    fun frsPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        tabId: Int = 0,
        isEssence: Boolean = false,
        subClassifyId: Int? = null,
    ): Flow<FrsPageResponse>
```

- [ ] **Step 2:** 修改 `MixedTiebaApiImpl.kt:1031-1077` 的实现:

```kotlin
    override fun frsPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        tabId: Int,
        isEssence: Boolean,
        subClassifyId: Int?
    ): Flow<FrsPageResponse> {
        // TODO(impl): tabId 进哪个 protobuf 字段需抓包对照网页接口确认。
        // 先按 `cid` 试; 若不通改 `category_id`。精华 tab 沿用旧路径(is_good=1 + cid=class_id)。
        val cidValue: Int = if (isEssence) (subClassifyId ?: 0) else tabId

        return RetrofitTiebaApi.OFFICIAL_PROTOBUF_TIEBA_V12_API.frsPageFlow(
            buildProtobufRequestBody(
                FrsPageRequest(
                    FrsPageRequestData(
                        ad_param = buildAdParam(),
                        app_pos = buildAppPosInfo(),
                        call_from = 0,
                        category_id = 0,
                        cid = cidValue,
                        common = buildCommonRequest(clientVersion = ClientVersion.TIEBA_V12),
                        ctime = 0,
                        data_size = 0,
                        hot_thread_id = 0,
                        is_default_navtab = if (tabId == 0 && !isEssence) 1 else 0,
                        is_good = if (isEssence) 1 else 0,
                        is_selection = 0,
                        kw = forumName.urlEncode(),
                        last_click_tid = 0,
                        load_type = loadType,
                        net_error = 0,
                        pn = page,
                        q_type = 2,
                        rn = 90,
                        rn_need = 30,
                        scr_dip = App.ScreenInfo.DENSITY.toDouble(),
                        scr_h = getScreenHeight(),
                        scr_w = getScreenWidth(),
                        sort_type = sortType,
                        st_param = 0,
                        st_type = "recom_flist",
                        up_schema = "",
                        with_group = 1,
                        yuelaou_locate = ""
                    )
                ),
                clientVersion = ClientVersion.TIEBA_V12
            ),
            forumName = forumName.urlEncode()
        )
    }
```

- [ ] **Step 3:** 修改 `ForumNetworkDataSource.kt:40-62` 的 `frsPage` 签名:

```kotlin
    @Throws(NoConnectivityException::class, TiebaException::class)
    suspend fun frsPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        tabId: Int,
        isEssence: Boolean,
        subClassifyId: Int?
    ): FrsPageResponseData {
        val response = TiebaApi.getInstance()
            .frsPage(forumName, page, loadType, sortType, tabId, isEssence, subClassifyId)
            .catch { throw ConnectivityInterceptor.wrapException(it) }
            .firstOrThrow()
        if (response.data_?.forum == null) throw TiebaApiException(response.error.commonResponse)

        return withContext(Dispatchers.Default) {
            response.data_.thread_list
                .filter(threadFilter)
                .addUsers(response.data_.user_list)
                .let { new ->
                    response.data_.copy(thread_list = new)
                }
        }
    }
```

- [ ] **Step 4:** 修改 `ForumRepository.kt:74` 的私有 `frsPage` —— 入参把 `goodClassifyId: Int?` 改成 `tabId: Int, isEssence: Boolean, subClassifyId: Int?`;内部 `cacheable` 表达式同步换;公开方法 `loadPage / loadGoodPage / loadMorePage / loadMoreGood` 签名**不变**,内部转译后调用私有方法。

`ForumRepository.kt:74-114` 改为:

```kotlin
    private suspend fun frsPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        tabId: Int,
        isEssence: Boolean,
        subClassifyId: Int?,
        forceNew: Boolean = false
    ): ForumPageResult {
        var key: CacheKey? = null
        var cached: ForumCache? = null
        val cacheable = if (isEssence) (subClassifyId ?: 0) == 0 else sortType == ForumSortType.BY_REPLY

        // Load first page from lru cache if possible
        if (page == 1 && cacheable && loadType == 1) {
            key = forumName
            cached = cache[key]
            val typedItemList = cached?.getItemsByType(isGood = isEssence)
            if (!forceNew && typedItemList != null) {
                return ForumPageResult(cached.forum, typedItemList, cached.managers)
            }
        }

        val data = networkDataSource.frsPage(forumName, page, loadType, sortType, tabId, isEssence, subClassifyId)
        val forumData = data.toData()
        var forumManagers: List<ForumManager>? = null
        val showBothName = habitSettings.first().showBothName
        val typedThreads = ThreadItemList(
            threads = data.thread_list.mapUiModel(blockedSettings.first(), showBothName, blockRepo::isBlocked),
            threadIds = data.thread_id_list,
            hasMore = data.page!!.has_more == 1
        )

        if (key != null) {
            forumManagers = data.getManagers(habit = habitSettings.first())
            val normalThreads = if (!isEssence) typedThreads else cached?.normal
            val goodThreads = if (isEssence) typedThreads else cached?.good
            cache.put(key, ForumCache(forumData, forumManagers, normal = normalThreads, good = goodThreads))
        }
        return ForumPageResult(forumData, typedThreads, forumManagers)
    }
```

`ForumRepository.kt:116-172` 公开方法适配(签名不变,内部参数转译):

```kotlin
    suspend fun loadForumInfo(forumName: String, forceNew: Boolean = true): ForumData {
        return frsPage(
            forumName = forumName, page = 1, loadType = 1, sortType = 0,
            tabId = 0, isEssence = false, subClassifyId = null, forceNew = forceNew
        ).first
    }

    suspend fun loadForumDetail(forumName: String): ForumDetail {
        val (forumData, _, managers) = frsPage(
            forumName = forumName, page = 1, loadType = 1, sortType = 0,
            tabId = 0, isEssence = false, subClassifyId = null
        )
        val detail = networkDataSource.loadForumDetail(forumData.id)

        return ForumDetail(
            avatar = forumData.avatar,
            name = forumData.name,
            id = forumData.id,
            intro = detail.content.plainText,
            slogan = detail.slogan,
            memberCount = detail.member_count,
            threadCount = forumData.threads,
            postCount = forumData.posts,
            managers = managers
        )
    }

    suspend fun loadPage(forum: String, page: Int, sortType: Int, forceNew: Boolean): ThreadItemList = frsPage(
        forumName = forum, page = page, loadType = 1, sortType = sortType,
        tabId = 0, isEssence = false, subClassifyId = null, forceNew = forceNew
    ).second

    suspend fun loadGoodPage(forum: String, page: Int, goodClassifyId: Int?, forceNew: Boolean): ThreadItemList = frsPage(
        forumName = forum, page = page, loadType = 1, sortType = 0,
        tabId = 0, isEssence = true, subClassifyId = goodClassifyId ?: 0, forceNew = forceNew
    ).second

    suspend fun loadMorePage(forum: String, page: Int, sortType: Int): ThreadItemList = frsPage(
        forumName = forum, page = page, loadType = 2, sortType = sortType,
        tabId = 0, isEssence = false, subClassifyId = null, forceNew = false
    ).second

    suspend fun loadMoreGood(forum: String, page: Int, goodClassifyId: Int?): ThreadItemList = frsPage(
        forumName = forum, page = page, loadType = 2, sortType = 0,
        tabId = 0, isEssence = true, subClassifyId = goodClassifyId ?: 0, forceNew = false
    ).second
```

(注:旧代码用 `sortType == -1` 当哨兵标记"是精品",此处改成显式 `isEssence` 布尔。`loadGoodPage / loadMoreGood` 不再传 `sortType=-1`)

- [ ] **Step 5:** 编译验证

Run: `./gradlew :app:compileDebugKotlin --no-configuration-cache`
Expected: BUILD SUCCESSFUL. 上层 ViewModel/UI 因公开 API 未变,继续编译通过。

- [ ] **Step 6:** 单测仍通过

Run: `./gradlew :app:testDebugUnitTest --no-configuration-cache`
Expected: PASS.

- [ ] **Step 7:** Commit

```bash
git add app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/ITiebaApi.kt \
        app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/impls/MixedTiebaApiImpl.kt \
        app/src/main/java/com/huanchengfly/tieba/post/repository/source/network/ForumNetworkDataSource.kt \
        app/src/main/java/com/huanchengfly/tieba/post/repository/ForumRepository.kt
git commit -m "refactor: ITiebaApi.frsPage 入参 goodClassifyId 拆为 tabId/isEssence/subClassifyId"
```

---

## Task 4: ForumRepository 公开 API + 缓存按 tabId 重构

**Files:**
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/repository/ForumRepository.kt`

把 `loadPage/loadGoodPage/loadMorePage/loadMoreGood` 四个合并为 `loadByTab/loadMoreByTab`,缓存改 `Map<Int, ThreadItemList>`。此 task 内 ViewModel 仍调老接口 —— 通过保留两个 thin wrapper 让旧调用方编译。Task 5 把 ViewModel 切到新接口后,Task 10 删 wrapper。

- [ ] **Step 1:** 重写 `ForumCache` data class:

修改 `ForumRepository.kt:43-50`:

```kotlin
private data class ForumCache(
    val forum: ForumData,
    val managers: List<ForumManager>?,
    val tabResults: Map<Int, ThreadItemList>,
)
```

- [ ] **Step 2:** 重写私有 `frsPage()`,缓存改按 tabId 索引;改完整体如下:

```kotlin
    private suspend fun frsPage(
        forumName: String,
        page: Int,
        loadType: Int,
        sortType: Int,
        tabId: Int,
        isEssence: Boolean,
        subClassifyId: Int?,
        forceNew: Boolean = false
    ): ForumPageResult {
        var cacheKey: CacheKey? = null
        var cached: ForumCache? = null
        val cacheable = if (isEssence) (subClassifyId ?: 0) == 0 else sortType == ForumSortType.BY_REPLY

        if (page == 1 && cacheable && loadType == 1) {
            cacheKey = forumName
            cached = cache[cacheKey]
            val typedItemList = cached?.tabResults?.get(tabId)
            if (!forceNew && typedItemList != null) {
                return ForumPageResult(cached.forum, typedItemList, cached.managers)
            }
        }

        val data = networkDataSource.frsPage(forumName, page, loadType, sortType, tabId, isEssence, subClassifyId)
        val forumData = data.toData()
        var forumManagers: List<ForumManager>? = null
        val showBothName = habitSettings.first().showBothName
        val typedThreads = ThreadItemList(
            threads = data.thread_list.mapUiModel(blockedSettings.first(), showBothName, blockRepo::isBlocked),
            threadIds = data.thread_id_list,
            hasMore = data.page!!.has_more == 1
        )

        if (cacheKey != null) {
            forumManagers = data.getManagers(habit = habitSettings.first())
            val mergedResults = (cached?.tabResults ?: emptyMap()) + (tabId to typedThreads)
            cache.put(cacheKey, ForumCache(forumData, forumManagers, tabResults = mergedResults))
        }
        return ForumPageResult(forumData, typedThreads, forumManagers)
    }
```

- [ ] **Step 3:** 把 4 个老公开方法**改成 thin wrapper 调新方法** —— 先加新方法 `loadByTab / loadMoreByTab`:

```kotlin
    /**
     * 按 NavTab 拉一页帖子.
     *
     * @param tabId `0` 表示默认 tab (含 fallback "全部"); 非 0 时尊重网页 tab id.
     * @param isEssence true 时表示"精华类" tab; [subClassifyId] 在此情形下生效.
     */
    suspend fun loadByTab(
        forum: String,
        page: Int,
        sortType: Int,
        tabId: Int,
        isEssence: Boolean,
        subClassifyId: Int?,
        forceNew: Boolean,
    ): ThreadItemList = frsPage(
        forumName = forum,
        page = page,
        loadType = 1,
        sortType = sortType,
        tabId = tabId,
        isEssence = isEssence,
        subClassifyId = subClassifyId,
        forceNew = forceNew,
    ).second

    suspend fun loadMoreByTab(
        forum: String,
        page: Int,
        sortType: Int,
        tabId: Int,
        isEssence: Boolean,
        subClassifyId: Int?,
    ): ThreadItemList = frsPage(
        forumName = forum,
        page = page,
        loadType = 2,
        sortType = sortType,
        tabId = tabId,
        isEssence = isEssence,
        subClassifyId = subClassifyId,
        forceNew = false,
    ).second
```

- [ ] **Step 4:** 把 4 个旧公开方法改成 thin wrapper(便于 Task 5 之前仍编译):

```kotlin
    suspend fun loadPage(forum: String, page: Int, sortType: Int, forceNew: Boolean): ThreadItemList =
        loadByTab(forum, page, sortType, tabId = 0, isEssence = false, subClassifyId = null, forceNew = forceNew)

    suspend fun loadGoodPage(forum: String, page: Int, goodClassifyId: Int?, forceNew: Boolean): ThreadItemList =
        loadByTab(forum, page, sortType = 0, tabId = 0, isEssence = true, subClassifyId = goodClassifyId ?: 0, forceNew = forceNew)

    suspend fun loadMorePage(forum: String, page: Int, sortType: Int): ThreadItemList =
        loadMoreByTab(forum, page, sortType, tabId = 0, isEssence = false, subClassifyId = null)

    suspend fun loadMoreGood(forum: String, page: Int, goodClassifyId: Int?): ThreadItemList =
        loadMoreByTab(forum, page, sortType = 0, tabId = 0, isEssence = true, subClassifyId = goodClassifyId ?: 0)
```

- [ ] **Step 5:** 编译验证

Run: `./gradlew :app:compileDebugKotlin --no-configuration-cache`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6:** 单测通过

Run: `./gradlew :app:testDebugUnitTest --no-configuration-cache`
Expected: PASS.

- [ ] **Step 7:** Commit

```bash
git add app/src/main/java/com/huanchengfly/tieba/post/repository/ForumRepository.kt
git commit -m "refactor: ForumCache 改成 Map<tabId, list>, 新增 loadByTab/loadMoreByTab"
```

---

## Task 5: `ForumThreadListViewModel` 从 `ForumType` 迁到 `NavTab`

**Files:**
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/threadlist/ForumThreadListViewModel.kt`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/threadlist/ForumThreadListPage.kt`

`ForumType.Latest/Good` 不再够用 — 改为承载具体 `NavTab`。`onClassifyIdChanged` / `onSortTypeChanged` 行为保留,但走新 `loadByTab` 入口。

- [ ] **Step 1:** 修改 `ForumThreadListViewModel.kt`,整体改写:

```kotlin
package com.huanchengfly.tieba.post.ui.page.forum.threadlist

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.arch.BaseStateViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.TbLiteExceptionHandler
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.emitGlobalEventSuspend
import com.huanchengfly.tieba.post.arch.stateInViewModel
import com.huanchengfly.tieba.post.repository.ExploreRepository.Companion.distinctById
import com.huanchengfly.tieba.post.repository.ForumRepository
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.repository.user.SettingsRepository
import com.huanchengfly.tieba.post.ui.models.Like
import com.huanchengfly.tieba.post.ui.models.ThreadItem
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
import com.huanchengfly.tieba.post.ui.models.settings.ForumSortType
import com.huanchengfly.tieba.post.ui.page.forum.threadlist.ForumThreadListViewModel.Companion.ForumVMFactory
import com.huanchengfly.tieba.post.ui.page.main.explore.concern.ConcernViewModel.Companion.updateLikeStatus
import com.huanchengfly.tieba.post.ui.page.main.explore.concern.ConcernViewModel.Companion.updateLikeStatusUiStateCommon
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlin.math.min

@Stable
@HiltViewModel(assistedFactory = ForumVMFactory::class)
class ForumThreadListViewModel @AssistedInject constructor(
    @Assisted val forumName: String,
    @Assisted val forumId: Long,
    @Assisted val tab: NavTab,
    private val forumRepo: ForumRepository,
    private val threadRepo: PbPageRepository,
    settingsRepo: SettingsRepository,
) : BaseStateViewModel<ForumThreadListUiState>() {

    override val errorHandler = TbLiteExceptionHandler(TAG) { _, e, suppressed ->
        if (suppressed && currentState.threads.isNotEmpty()) {
            _uiState.update { it.copy(isRefreshing = false, isLoadingMore = false, error = null) }
            sendUiEvent(CommonUiEvent.ToastError(e))
        } else {
            _uiState.update { it.copy(isRefreshing = false, isLoadingMore = false, error = e) }
        }
    }

    // 排序流仅在"非精华类" tab 出现; 精华类沿用旧约定 sortType=0
    private val sortTypeFlow: Flow<Int>? =
        if (!tab.isEssence) forumRepo.getSortType(forumName) else null

    override fun createInitialState(): ForumThreadListUiState = ForumThreadListUiState(isRefreshing = true)

    val hideBlocked: StateFlow<Boolean> = settingsRepo.blockSettings
        .map { it.hideBlocked }
        .stateInViewModel(initialValue = true)

    init {
        launchInVM { loadInternal(sortType = null, subClassifyId = null) }
    }

    private suspend fun loadInternal(sortType: Int?, subClassifyId: Int?, forceNew: Boolean = false) {
        _uiState.update { it.copy(isRefreshing = true) }
        val effectiveSort = sortType ?: sortTypeFlow?.first() ?: 0
        val data = forumRepo.loadByTab(
            forum = forumName,
            page = 1,
            sortType = effectiveSort,
            tabId = tab.tabId,
            isEssence = tab.isEssence,
            subClassifyId = subClassifyId,
            forceNew = forceNew,
        )
        _uiState.update {
            ForumThreadListUiState(
                subClassifyId = it.subClassifyId,
                threads = data.threads,
                threadIds = data.threadIds,
                currentPage = 1,
                hasMore = data.hasMore,
            )
        }
    }

    /** 仅在精华类 tab 有意义; 由 ForumViewModel 在用户选了子分类 chip 后调用. */
    fun onSubClassifyIdChanged(classifyId: Int) {
        if (!tab.isEssence) return
        val state = _uiState.updateAndGet { it.copy(subClassifyId = classifyId) }
        if (state.isRefreshing) return
        launchInVM {
            // 当 classifyId == 0 时复用缓存; 否则强制新拉.
            loadInternal(sortType = null, subClassifyId = classifyId, forceNew = classifyId != 0)
        }
    }

    fun onSortTypeChanged(@ForumSortType sortType: Int?) {
        if (currentState.isRefreshing) return
        launchInVM { loadInternal(sortType = sortType, subClassifyId = null, forceNew = false) }
    }

    fun onRefresh() {
        if (currentState.isRefreshing) return
        launchInVM {
            val currentClassify = currentState.subClassifyId
            loadInternal(
                sortType = sortTypeFlow?.first(),
                subClassifyId = if (tab.isEssence) (currentClassify ?: 0) else null,
                forceNew = true,
            )
        }
    }

    fun loadMore() {
        val state = currentState
        if (state.isLoadingMore) return else _uiState.update { it.copy(isLoadingMore = true) }

        launchInVM {
            val effectiveSort = sortTypeFlow?.first() ?: 0
            if (state.threadIds.isNotEmpty()) {
                val size = min(state.threadIds.size, 30)
                val threadIds = state.threadIds.subList(0, size)
                val newList = forumRepo.threadList(forumId, forumName, state.currentPage, effectiveSort, threadIds)
                val threadList = (state.threads + newList).distinctById()

                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        isLoadingMore = false,
                        threads = threadList,
                        threadIds = state.threadIds.drop(size),
                        hasMore = threadList.isNotEmpty()
                    )
                }
            } else {
                val page = state.currentPage + 1
                val data = forumRepo.loadMoreByTab(
                    forum = forumName,
                    page = page,
                    sortType = effectiveSort,
                    tabId = tab.tabId,
                    isEssence = tab.isEssence,
                    subClassifyId = if (tab.isEssence) state.subClassifyId else null,
                )
                val threadList = (state.threads + data.threads).distinctById()
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        isLoadingMore = false,
                        threads = threadList,
                        threadIds = data.threadIds,
                        currentPage = page,
                        hasMore = data.hasMore
                    )
                }
            }
        }
    }

    fun onThreadLikeClicked(thread: ThreadItem) = launchInVM {
        updateLikeStatusUiStateCommon(
            thread = thread,
            onRequestLikeThread = threadRepo::requestLikeThread,
            onEvent = ::emitGlobalEventSuspend
        ) { threadId, liked, loading ->
            _uiState.update { it.copy(threads = it.threads.updateLikeStatus(threadId, liked, loading)) }
        }
    }

    fun onThreadResult(threadId: Long, like: Like): Unit = launchInVM {
        val newThreads = currentState.threads.updateLikeStatus(threadId, like)
        if (newThreads != null) {
            _uiState.update { it.copy(threads = newThreads) }
        }
    }

    companion object {
        private const val TAG = "ForumThreadListViewMode"

        @AssistedFactory
        interface ForumVMFactory {
            fun create(forumName: String, forumId: Long, tab: NavTab): ForumThreadListViewModel
        }
    }
}

data class ForumThreadListUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val subClassifyId: Int? = null,
    val threads: List<ThreadItem> = emptyList(),
    val threadIds: List<Long> = emptyList(),
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val error: Throwable? = null,
) : UiState

sealed interface ForumThreadListUiEvent : UiEvent {
    data class SortTypeChanged(val sortType: Int) : ForumThreadListUiEvent

    /** 子分类切换. `tabId` 用于让监听方过滤"这个事件归我吗". */
    data class ClassifyChanged(val tabId: Int, val subClassifyId: Int) : ForumThreadListUiEvent

    /** 刷新当前主 tab. `tabId` 用于过滤"事件归我吗". */
    data class Refresh(val tabId: Int) : ForumThreadListUiEvent
}
```

> 说明:`ForumType` 枚举 + `goodClassifyId` 字段名一起换成 `subClassifyId`。`ForumThreadListUiEvent.Refresh / ClassifyChanged` 改 payload 携带 `tabId`,事件订阅方按 `tabId` 过滤;`SortTypeChanged` 因为是吧级别广播保持不变。

- [ ] **Step 2:** 改 `ForumThreadListPage.kt:97-131` 的 Composable 入参 + 事件订阅:

```kotlin
@Composable
fun ForumThreadList(
    modifier: Modifier = Modifier,
    threadClickListeners: ThreadClickListeners,
    forumId: Long,
    forumName: String,
    tab: NavTab,
    forumRuleTitle: String?,
    contentPadding: PaddingValues,
    listState: LazyListState = rememberLazyListState(),
    viewModel: ForumThreadListViewModel = hiltViewModel<ForumThreadListViewModel, ForumVMFactory>(
        key = Objects.hash(forumId, forumName, tab.tabId).toString()
    ) {
        it.create(forumName, forumId, tab = tab)
    }
) {
    val navigator = LocalNavController.current

    viewModel.uiEvent.collectCommonUiEventWithLifecycle()

    onGlobalEvent<ForumThreadListUiEvent.Refresh>(
        filter = { it.tabId == tab.tabId },
    ) {
        viewModel.onRefresh()
    }

    if (tab.isEssence) {
        onGlobalEvent<ForumThreadListUiEvent.ClassifyChanged>(
            filter = { it.tabId == tab.tabId },
        ) {
            viewModel.onSubClassifyIdChanged(classifyId = it.subClassifyId)
        }
    } else {
        onGlobalEvent<ForumThreadListUiEvent.SortTypeChanged> {
            viewModel.onSortTypeChanged(sortType = it.sortType)
        }
    }

    // ... 后续 ConsumeThreadPageResult / StateScreen / SwipeUpLazyLoadColumn 等保持不动,
    // 只是把 `ForumType.Good` 等老引用一并清理
```

补两个 import:
```kotlin
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
```
(删除原来的 `ForumType` import)

`ForumThreadListPage.kt` 文件里其它地方没引用 `ForumType` —— 改完编译应通过。

- [ ] **Step 3:** 编译验证(此 task 后 ForumViewModel / ForumPage 仍会引用老 `ForumType`,本步骤会失败 —— 在 Task 6 接着改完才能整体过)

Run: `./gradlew :app:compileDebugKotlin --no-configuration-cache`
Expected: 失败,错误集中在 `ForumViewModel.kt` 与 `ForumPage.kt` 对 `ForumType.Latest/Good`、`isGood`、`onGoodClassifyChanged` 的旧引用。**这是预期的**,Task 6 修复。

- [ ] **Step 4:** **暂不 commit**,本 task 与 Task 6 一并提交(避免中间状态不可编译)。

---

## Task 6: `ForumViewModel` 与 `ForumPage` 切到 `tabId` / `NavTab`

**Files:**
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumViewModel.kt`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumPage.kt`
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumTab.kt`(整文件重写)

把 `isGood: Boolean` / `ForumType` 全部换成 `tabId: Int`;UI 改成动态 Pager。

- [ ] **Step 1:** 改 `ForumViewModel.kt` —— `ForumUiState`、事件、所有 `isGood` 调用方:

`ForumViewModel.kt:211-248` 改为:

```kotlin
data class ForumUiState(
    val forum: ForumData? = null,
    val subClassifyId: Int? = null,
    val error: Throwable? = null
)

sealed interface ForumUiEvent : UiEvent {

    data class AddThread(val forumId: Long?) : ForumUiEvent

    /** 由 UI 层把要滚回顶部的 tab 指明. */
    data class ScrollToTop(val tabId: Int) : ForumUiEvent

    sealed interface SignIn : ForumUiEvent {
        data class Success(val signBonusPoint: Int, val userSignRank: Int) : SignIn
        data class Failure(val errorMsg: String) : SignIn
    }

    sealed interface Like : ForumUiEvent {
        data class Success(val memberSum: String) : Like
        data class Failure(val errorMsg: String) : Like
    }

    sealed interface Dislike : ForumUiEvent {
        object Success : Dislike
        class Failure(val errorMsg: String) : Dislike
    }

    sealed interface PinShortcut : ForumUiEvent {
        object Success : PinShortcut
        class Failure(val errorMsg: String) : PinShortcut
    }
}
```

`ForumViewModel.kt:77-100` 改方法:

```kotlin
    fun onSubClassifyChanged(tabId: Int, classifyId: Int) {
        _uiState.set { copy(subClassifyId = classifyId) }
        launchInVM {
            sendUiEvent(ForumUiEvent.ScrollToTop(tabId = tabId))
            emitGlobalEventSuspend(ForumThreadListUiEvent.ClassifyChanged(tabId, classifyId))
        }
    }

    fun onSortTypeChanged(@ForumSortType sortType: Int) {
        launchInVM {
            forumRepo.saveSortType(forumName, sortType)
            // 排序变化是吧级别广播; ScrollToTop 由 UI 主动按 currentTab 触发,这里不发
            delay(200)
            emitGlobalEventSuspend(ForumThreadListUiEvent.SortTypeChanged(sortType))
        }
    }

    fun onRefreshClicked(tabId: Int) {
        launchInVM {
            sendUiEvent(ForumUiEvent.ScrollToTop(tabId))
            delay(200)
            emitGlobalEventSuspend(ForumThreadListUiEvent.Refresh(tabId))
        }
    }

    fun onFabClicked(@ForumFAB fab: Int, currentTabId: Int) {
        when (fab) {
            ForumFAB.POST -> sendUiEvent(ForumUiEvent.AddThread(forumId = currentState.forum?.id))
            ForumFAB.REFRESH -> onRefreshClicked(currentTabId)
            ForumFAB.BACK_TO_TOP -> sendUiEvent(ForumUiEvent.ScrollToTop(currentTabId))
            ForumFAB.HIDE -> throw IllegalStateException("Incorrect Compose state")
        }
    }
```

删除 `import com.huanchengfly.tieba.post.ui.page.forum.threadlist.ForumType`。

- [ ] **Step 2:** 重写 `ForumTab.kt`:

完整内容:

```kotlin
package com.huanchengfly.tieba.post.ui.page.forum

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.unsafeLazy
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
import com.huanchengfly.tieba.post.ui.models.settings.ForumSortType
import com.huanchengfly.tieba.post.ui.widgets.compose.FancyAnimatedIndicatorWithModifier
import com.huanchengfly.tieba.post.ui.widgets.compose.TabClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.preference.Options
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.launch

private val TabSortTypes: Options<Int> by unsafeLazy {
    persistentMapOf(
        ForumSortType.BY_REPLY to R.string.title_sort_by_reply,
        ForumSortType.BY_SEND to R.string.title_sort_by_send
    )
}

/**
 * 顶部主标签栏. 由 [navTabs] 动态出 N 个 tab.
 *
 * 排序菜单 [TabClickMenu] 仅挂在**非精华**类 tab 上 —— 精华 tab 的排序由协议侧固定.
 */
@Composable
fun ForumTab(
    modifier: Modifier = Modifier,
    navTabs: List<NavTab>,
    pagerState: PagerState,
    sortType: Int,
    onSortTypeChanged: (sortType: Int) -> Unit,
) {
    val currentPage = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()

    val unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tabTextStyle = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp)

    ScrollableTabRow(
        selectedTabIndex = currentPage,
        indicator = { FancyAnimatedIndicatorWithModifier(index = currentPage) },
        divider = {},
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 0.dp,
        modifier = modifier,
    ) {
        navTabs.forEachIndexed { index, tab ->
            val selected = index == currentPage
            val onClick: () -> Unit = {
                coroutineScope.launch { pagerState.animateScrollToPage(index) }
            }

            if (tab.isEssence) {
                Tab(selected = selected, onClick = onClick, unselectedContentColor = unselectedContentColor) {
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = tab.tabName, style = tabTextStyle)
                    }
                }
            } else {
                TabClickMenu(
                    selected = selected,
                    onClick = onClick,
                    text = { Text(text = tab.tabName, style = tabTextStyle) },
                    menuContent = {
                        ListPickerMenuItems(
                            items = TabSortTypes,
                            picked = sortType,
                            onItemPicked = onSortTypeChanged
                        )
                    },
                    unselectedContentColor = unselectedContentColor,
                )
            }
        }
    }
}
```

注:
- 用 `ScrollableTabRow` 替换原 `SecondaryTabRow`,允许 N 个 tab 横向滚动(`edgePadding=0.dp` 让首项贴左边)。
- `ListPickerMenuItems`、`TabClickMenu` 都是现有公共组件,不动。
- 不再导出 `TAB_FORUM_LATEST/GOOD` 常量。

- [ ] **Step 3:** 改 `ForumPage.kt`:

主要四块改动:

**(a) `pagerState` 改成动态页数 + 初始页用 `isDefault`** —— 替换 `ForumPage.kt:183`:

```kotlin
    val navTabs = remember(forumData?.navTabs) {
        forumData?.navTabs.orEmpty().ifEmpty { listOf(com.huanchengfly.tieba.post.ui.models.forum.NavTab.Fallback) }
    }
    val initialPage = remember(navTabs) {
        navTabs.indexOfFirst { it.isDefault }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { navTabs.size }
    val listStates = rememberPagerListStates(pagerState.pageCount)
```

> 注意:`forumData` 第一次进来是 null,`navTabs` 兜底成 `[Fallback]`,`pagerState.pageCount = 1`。等 `forumData` 加载好后 `navTabs` 变成真值,**Compose key 变化** → `rememberPagerState` 不会自动重建。修复:把 `pagerState` 用 `key(forumName)` 包一层 —— 进同一个吧不重建,跨吧重建。

把上面那段再用 `key` 包起来:
```kotlin
    val (navTabs, pagerState, listStates) = key(forumName) {
        // ... 同上但作为 key 区块的返回值
    }
```

具体改法:

```kotlin
    val forumDataNavTabs = forumData?.navTabs
    val navTabs = remember(forumDataNavTabs) {
        forumDataNavTabs.orEmpty().ifEmpty { listOf(NavTab.Fallback) }
    }
    val initialPage = remember(forumName, navTabs) {
        navTabs.indexOfFirst { it.isDefault }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { navTabs.size },
    )
    val listStates = rememberPagerListStates(pagerState.pageCount)
```

`rememberPagerState` 在 androidx.compose.foundation 里就是 lambda 形式;`navTabs.size` 在 `forumData` 变化后会重新读到,内部 `pageCount` lambda 是订阅式的。

import:
```kotlin
import com.huanchengfly.tieba.post.ui.models.forum.NavTab
```

**(b) 重做 `forumThreadPages` `movableContentOf` 列表** —— 替换 `ForumPage.kt:257-272`:

```kotlin
    val forumThreadPages = remember(threadClickListeners, navTabs) {
        navTabs.map { tab ->
            movableContentOf<Modifier, PaddingValues, ForumData> { modifier, contentPadding, forum ->
                ForumThreadList(
                    modifier = modifier,
                    threadClickListeners = threadClickListeners,
                    forumId = forum.id,
                    forumName = forum.name,
                    tab = tab,
                    forumRuleTitle = forum.forumRuleTitle.takeUnless { tab.isEssence },
                    contentPadding = contentPadding,
                    listState = listStates[navTabs.indexOf(tab)]
                )
            }
        }
    }
```

**(c) 更新 `uiEvent` 监听 `ScrollToTop`** —— 替换 `ForumPage.kt:218-223`:

```kotlin
            is ForumUiEvent.ScrollToTop -> {
                val idx = navTabs.indexOfFirst { t -> t.tabId == it.tabId }.coerceAtLeast(0)
                listStates[idx].scrollToItem(0)
                scrollBehavior.state.contentOffset = 0f
                scrollBehavior.state.heightOffset = 0f
            }
```

**(d) `AddThreadSuccess` / `ForumFAB` / `ForumTab` 调用方改用 `currentTabId`** ——

`ForumPage.kt:274-282`:

```kotlin
    onGlobalEvent<GlobalEvent.AddThreadSuccess>() {
        val currentTabId = navTabs.getOrNull(pagerState.currentPage)?.tabId ?: NavTab.FALLBACK_TAB_ID
        coroutineScope.launch {
            emitGlobalEventSuspend(ForumThreadListUiEvent.Refresh(tabId = currentTabId))
        }
    }
```

`ForumPage.kt:405-407` FAB 点击:

```kotlin
            ForumFAB(expanded = fabMenuExpanded, onExpandChanged = onFabExpandChanged, visible = fabVisible) { fab ->
                val currentTabId = navTabs.getOrNull(pagerState.currentPage)?.tabId ?: NavTab.FALLBACK_TAB_ID
                viewModel.onFabClicked(fab, currentTabId = currentTabId)
            }
```

`ForumPage.kt:374-381` 顶部 `ForumTab` 传参 + ClassifyTabs:

```kotlin
                val sortType by viewModel.sortType.collectAsStateWithLifecycle()
                ForumTab(
                    modifier = Modifier.fillMaxWidth(),
                    navTabs = navTabs,
                    pagerState = pagerState,
                    sortType = sortType,
                    onSortTypeChanged = viewModel::onSortTypeChanged
                )

                val currentTab = navTabs.getOrNull(pagerState.currentPage)
                val classifyVisible by remember(currentTab, uiState.forum?.goodClassifies) {
                    derivedStateOf {
                        currentTab?.isEssence == true && (uiState.forum?.goodClassifies?.size ?: 0) > 1
                    }
                }
                val goodClassifies = uiState.forum?.goodClassifies ?: return@CollapsingAvatarTopAppBar
                AnimatedVisibility(visible = classifyVisible) {
                    ClassifyTabs(
                        goodClassifies = goodClassifies,
                        selectedItem = uiState.subClassifyId,
                        onSelected = { subId ->
                            currentTab?.let { viewModel.onSubClassifyChanged(it.tabId, subId) }
                        },
                    )
                }
```

> `uiState.goodClassifyId` 字段名在 Task 6 step 1 已改成 `subClassifyId`,这里同步。

- [ ] **Step 4:** 编译验证

Run: `./gradlew :app:compileDebugKotlin --no-configuration-cache`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5:** 单测仍通过

Run: `./gradlew :app:testDebugUnitTest --no-configuration-cache`
Expected: PASS.

- [ ] **Step 6:** Commit(把 Task 5 + 6 合并为一个完整可编译提交)

```bash
git add app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/threadlist/ForumThreadListViewModel.kt \
        app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/threadlist/ForumThreadListPage.kt \
        app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumViewModel.kt \
        app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumPage.kt \
        app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/ForumTab.kt
git commit -m "feat: ForumPage 顶部改为 NavTab 动态标签栏, 替换最新/精品双 Tab"
```

---

## Task 7: 删除残余 `ForumType` 枚举 + 老 wrapper

**Files:**
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/ui/page/forum/threadlist/ForumThreadListViewModel.kt`(已经在 Task 5 删过;此 task 复核)
- Modify: `app/src/main/java/com/huanchengfly/tieba/post/repository/ForumRepository.kt` — 删 4 个老 wrapper

- [ ] **Step 1:** 在 `ForumRepository.kt` 删 `loadPage / loadGoodPage / loadMorePage / loadMoreGood` 四个老方法(Task 4 加的 thin wrapper)

确保没有其它调用方再用它们 —— Task 5/6 后,`ForumThreadListViewModel` 已经走 `loadByTab/loadMoreByTab`。 但 `ForumRepository.loadForumInfo` / `loadForumDetail` 仍调私有 `frsPage(...)`,这两个保留不动。

- [ ] **Step 2:** 全局搜确认无残余引用

Run: `grep -rn "loadGoodPage\|loadMoreGood\|loadMorePage\|fun loadPage(" app/src/main/java/`
Expected: 仅 `ForumRepository.kt` 内剩余(如有)— 全部应该被删除。

- [ ] **Step 3:** 全局搜 `ForumType`

Run: `grep -rn "ForumType" app/src/`
Expected: 无输出。如果有遗留(如 import) 一并删。

- [ ] **Step 4:** 编译 + 单测

Run: `./gradlew :app:compileDebugKotlin :app:testDebugUnitTest --no-configuration-cache`
Expected: BUILD SUCCESSFUL,5 个 ForumMappers 单测 PASS。

- [ ] **Step 5:** Commit

```bash
git add app/src/main/java/com/huanchengfly/tieba/post/repository/ForumRepository.kt
git commit -m "chore: 删除 ForumRepository 的 loadPage/loadGoodPage/loadMorePage/loadMoreGood 旧 wrapper"
```

---

## Task 8: 装机手测 + 抓 tabId/cid 字段映射

无自动化测试覆盖,网络层 + UI 需手测确认。

- [ ] **Step 1:** 装 CI 包到设备:

```bash
./gradlew :app:installCi --no-configuration-cache
```

- [ ] **Step 2:** 验证大吧(王者荣耀吧 / 原神吧)NavTab 行为
  - 进吧时顶部出现网页版那 8 个 tab(精华/热门/最新/吧友互助/开黑/交友/战队丨圈子/视频)
  - 默认进的 tab 与网页版一致(协议 `isDefault=1` 那一项)
  - 切换 tab → 列表刷新,内容与网页版同 tab 大致一致
  - 切"精华"tab → 顶部 Chip 子分类条显示(攻略心得贴/比赛资讯贴/...);点子分类列表过滤
  - 排序菜单(长按 tab)只在非精华 tab 出现
  - 上拉加载更多在每个 tab 正常工作

- [ ] **Step 3:** 验证小吧(如个人吧 / 冷门吧)fallback 行为
  - 进吧只显示一个"全部"tab(`NavTab.Fallback`)
  - 该 tab 行为等同旧版"最新":能按发表/按回复排序,能加载更多

- [ ] **Step 4:** 验证场景:跨吧导航
  - 从吧 A 切到吧 B(navTabs 不同),Pager 重建,无 page 索引越界
  - 从吧 A 的"开黑"tab 进入帖子,返回后仍停在"开黑"tab,且选过的精华子分类无串号

- [ ] **Step 5:** 若 Step 2 切到某个非精华 tab(如"开黑")**列表为空或与网页版差异巨大**,说明 tabId 没正确写入 protobuf 字段。处理:

抓包 / 看 Charles / 看 mitmproxy 的 `c/f/frs/page` 请求,对照网页 `tab=20928` 与 protobuf 字段。

候选调整(只改 `MixedTiebaApiImpl.kt:1031` 的 `cidValue` 那行):
- 假设 a: `category_id = tabId` 而 `cid = 0`
- 假设 b: 新增 `st_param = tabId`
- 假设 c: 走 `is_default_navtab=0` + 其它字段

每次只改一处,装机重测对应 tab 的内容差异,直到一致。

- [ ] **Step 6:** 若 Step 5 改了 `MixedTiebaApiImpl`,补 commit:

```bash
git add app/src/main/java/com/huanchengfly/tieba/post/api/interfaces/impls/MixedTiebaApiImpl.kt
git commit -m "fix: 实测确认 tabId 写入 <实际字段名>"
```

- [ ] **Step 7:** 最后跑一遍 CI 等同构建

Run: `./gradlew :app:testDebugUnitTest assembleCi --no-configuration-cache`
Expected: BUILD SUCCESSFUL,所有单测 PASS。

---

## 完成清单

- [x] Task 1 — NavTab 模型 + ForumData.navTabs
- [x] Task 2 — NavTabInfo.toNavTabs 映射 + 单测
- [x] Task 3 — 协议层签名替换
- [x] Task 4 — ForumRepository 公开 API + 缓存重构
- [x] Task 5 + 6 — ViewModel + UI 切换到 NavTab
- [x] Task 7 — 删旧 wrapper / ForumType
- [x] Task 8 — 装机手测 + 确认 tabId 协议字段
