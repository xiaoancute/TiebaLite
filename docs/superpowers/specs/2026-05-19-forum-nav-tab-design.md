# 吧内分区(NavTab)对齐网页版 — 设计文档

- 状态:草案
- 日期:2026-05-19
- 影响范围:`:app` — 吧页面(ForumPage)、Forum 协议层、`ForumRepository`、`ForumThreadListViewModel`

## 1. 背景

App 当前吧页面只有两个硬编码 Tab —— **最新 / 精品**(`ForumTab.kt:28-29`),通过协议字段 `is_good=0/1` 切换。
网页版(`https://tieba.baidu.com/f?kw=<吧名>`)实际呈现的是**一条平级标签栏**,URL 用 `?tab=<tabId>` 区分。

王者荣耀吧实测(2026-05-19,登录态)的主标签栏:

```
精华(tab=301)  热门(tab=1)  最新(tab=503)  吧友互助(tab=1627914)
开黑(tab=20928)  交友(tab=20929)  战队丨圈子(tab=23672)  视频(tab=445300)
```

进入"精华"标签后,正文上方会出现子标签栏 `?tab=301&subtab=<class_id>`:`全部 / 攻略心得贴 / 比赛资讯贴 / 公告活动贴 / 吧务组公告 / cos同人 / 配音图鉴贴 / 交友记录贴 / 每日话题贴` —— 这正是 App 当前的 `good_classify` 子分类。

截图存档:`docs/superpowers/specs/tieba-web-forum-tabs.png`

## 2. 目标 / 非目标

### 目标
1. 进吧时读取协议返回的 `nav_tab_info.tab`,顶部呈现网页版那条平级标签栏。
2. 切换标签时按对应 tabId 重新请求帖子列表。
3. "精华"标签下的子分类(`good_classify`)行为不变,仍以 Chip 条形式呈现。
4. 处理每吧 NavTab 不同(数量、内容、默认项)带来的所有动态性。

### 非目标(本轮不做)
- 持久化"上次选择的分区"。
- 深链(`tblite://` / 网页 URL 里的 `?tab=`)解析到对应分区。
- 发帖时选择目标分区。
- 网页版子标签的发现机制(如:"精华"之外的标签是否也有 subtab)。

## 3. 字段映射

| 网页 URL 参数 | App 协议字段(`FrsPageRequestData`) | 当前 App 行为 |
|---|---|---|
| `tab=301` (精华) | `is_good=1` | 走"精品"Tab |
| `tab=503` (最新) | `is_good=0` | 走"最新"Tab |
| `tab=1` (热门) | 未接入 | — |
| `tab=20928+` (吧友互助/开黑/...) | 未接入 | — |
| `&subtab=<id>` | `cid=<class_id>` + `is_good=1` | 走"精品"Tab 的子分类 |

`FrsTabInfo.proto`(已存在,Kotlin 代码未引用):

```
int32 tabId        // 网页 URL 里的 tab=<id>
int32 tabType      // 分类类型(精华/热门/普通自定义...)
string tabName     // 标签名,如"精华"
int32 isGeneralTab // 是否通用标签
int32 isDefault    // 是否默认选中(每吧只会有一个)
string tabCode     // 标签 code(可能与 tabType 二选一标识"精华")
```

`FrsPageResponseData.nav_tab_info: NavTabInfo`(字段 37,已存在),`NavTabInfo.tab: repeated FrsTabInfo`(字段 1)是主标签数组。

## 4. 架构

```
                          frsPage Protobuf 响应
                                   │
                                   ▼
                  ForumNetworkDataSource.frsPage()
                                   │
                                   ▼  (映射)
                    ForumRepository.frsPage()
                                   │
                                   ▼
              ForumData(navTabs, goodClassifies, ...)
                                   │
                          ┌────────┴────────┐
                          ▼                 ▼
                   ForumViewModel    ForumThreadListViewModel
                  (整体 UI 状态)      (每 tab 一个,AssistedInject)
                          │                 │
                          ▼                 ▼
                      ForumPage  ◄────  ForumThreadList
                 (HorizontalPager
                  按 navTabs 出页)
```

## 5. 详细设计

### 5.1 数据模型

**新增** `com.huanchengfly.tieba.post.ui.models.forum.NavTab`:

```kotlin
@Immutable
data class NavTab(
    val tabId: Int,        // 0 表示"全部"占位(fallback)
    val tabName: String,
    val tabType: Int,      // 用于判定"是否精华类(有子分类)"
    val isDefault: Boolean,
)
```

**修改** `ForumData`:

```kotlin
data class ForumData(
    // ... 现有字段不动
    val navTabs: List<NavTab>,  // 新增,空列表表示 fallback
    val goodClassifies: List<GoodClassify>?,  // 保留,语义改为"主 tab 的子分类"
)
```

### 5.2 协议层

**`ITiebaApi.frsPage`** 入参从 `(forumName, page, loadType, sortType, goodClassifyId)` 改为 `(forumName, page, loadType, sortType, tabId, subClassifyId)`:

- `tabId: Int` —— 0 表示"全部/默认",大于 0 表示具体 NavTab
- `subClassifyId: Int?` —— 仅在精华类 tab 下传值,对应 `good_classify.class_id`

**`MixedTiebaApiImpl.frsPage`** 内部:

- `is_good` 不再由"是否选了精品 Tab"决定,而是由"当前 tab 的 tabType / tabCode 是否为精华类"决定;
- `cid` 字段填入:精华 tab 下填 `subClassifyId ?: 0`,非精华 tab 下填 `tabId`(**待实测**:如果协议不接受 tabId 写在 cid,改填 `category_id` 字段);
- 老的 `goodClassifyId` 入参彻底删除。

**待实测 TODO**:tabId(数字范围 1 ~ 数百万,如 1627914)在 protobuf 请求里的归属字段。候选:`cid` / `category_id` / 其它。先按 `cid` 试,抓包对比网页接口确认。

### 5.3 Repository

**`ForumRepository`** 主要改动:

1. `frsPage()` 私有方法的入参 / 缓存 key 同步重做:
   - 入参:`(forumName, page, loadType, sortType, tabId, subClassifyId, forceNew)`
   - 缓存粒度:从 `LruCache<forumName, ForumCache(normal, good)>` 改为 `LruCache<forumName, ForumCache(Map<tabId, ThreadItemList>)>`,容量保持 2(两个吧)。
   - LRU 命中规则:换吧整桶失效;切 tab 命中桶内 `Map<tabId, _>`;sortType 非默认时 bypass 缓存(沿用现状)。

2. **公开方法精简**:`loadPage` / `loadGoodPage` / `loadMorePage` / `loadMoreGood` 四个合并为两个:
   ```kotlin
   suspend fun loadPage(
       forum: String,
       page: Int,
       tabId: Int,
       sortType: Int,
       subClassifyId: Int?,
       forceNew: Boolean,
   ): ThreadItemList
   
   suspend fun loadMorePage(
       forum: String, page: Int, tabId: Int, sortType: Int, subClassifyId: Int?
   ): ThreadItemList
   ```

3. `FrsPageResponseData.toData()` 映射时把 `nav_tab_info.tab` 转成 `List<NavTab>`;若 `nav_tab_info` 为 `null` 或 `tab` 为空,产出 `listOf(NavTab(tabId = 0, tabName = "全部", tabType = 0, isDefault = true))` 一项 fallback。

### 5.4 ViewModel

**`ForumViewModel`**(顶部页面):

- `uiState.forum.navTabs` 决定 HorizontalPager 页数。
- 没有"哪个是默认页"的硬编码;`MainPageScreen` 初始定位到 `navTabs.indexOfFirst { it.isDefault }.coerceAtLeast(0)`。
- 现有 `goodClassifyId` 状态保留,但**仅当当前页所属 tab 是精华类时生效**(`uiState.goodClassifyId` 在切到非精华 tab 时不清空,以便切回精华仍记得用户选过哪一档)。

**`ForumThreadListViewModel`** (按 tab 实例化):

- AssistedInject 构造入参从 `(forumName, forumId, type: ForumType)` 改为 `(forumName, forumId, tab: NavTab)`,`ForumType` 枚举删除。
- 在 `ForumThreadList(...)` 里调用 `hiltViewModel<_, ForumVMFactory>(key = ...)` 的 `key` 改为 `Objects.hash(forumId, forumName, tab.tabId).toString()`(`forumId` 留在 key 里,防止两个吧 tabId 巧合相同串号 —— 不同吧的同名标签 tabId 几乎不会撞,但 fallback 占位 `tabId=0` 会全部撞,`forumId` 是关键)。
- `loadInternal()` 内部按 `(sortType, subClassifyId)` 调 `forumRepo.loadPage(...)` 的形态沿用现状;**唯一变化**是 subClassifyId 现在仅在"精华类 tab"下才会被外部喂入非空值(由 `ForumViewModel.uiState.goodClassifyId` 注入),其它 tab 恒为 null。
- `onSortTypeChanged` / `onRefresh` 现状保留。

### 5.5 UI 层

**`ForumPage.kt`**:

- `pagerState = rememberPagerState { uiState.forum?.navTabs?.size ?: 1 }`;`rememberPagerState` 的 input key 包含 `forumName`,防止跨吧残留。
- `HorizontalPager` 内每页对应一个 NavTab,沿用 `movableContentOf` + `ForumThreadList`。
- `forumThreadPages` 的 `key` 用 `navTab.tabId`。

**`ForumTab.kt`**:

- 完全重写。常量 `TAB_FORUM_LATEST / TAB_FORUM_GOOD` 删除。
- 改成 `LazyRow` 横滑显示 `navTabs`,选中态走 `FancyAnimatedIndicator`(继续复用)。
- "排序"菜单(`TabClickMenu`)仅挂在 `tabType == 最新类` 的 tab 上(沿用 `TabSortTypes`);其它 tab 不出排序菜单 —— **可由开发实测后调整,如果协议对所有 tab 都接受 sortType,则全部 tab 都挂菜单**。

**`ClassifyTabs`**(精华子分类,已存在):

- 展示条件改为 `currentNavTab.tabType == 精华类 && forumData.goodClassifies.size > 1`(**而非**写死 `currentPage == TAB_FORUM_GOOD`)。
- 这样:小吧没精华子分类时自动隐藏;某吧给热门 tab 也配了 good_classify(理论可能)也会自动出。

## 6. "每吧不同"约束清单

| 场景 | 处理 |
|---|---|
| NavTab 列表每吧不同 | `nav_tab_info` 每次进吧实时填 `navTabs`,不跨吧缓存 |
| `pageCount` 每吧不同 | `rememberPagerState { size }` 输入随 forumName 重建 |
| 子 ViewModel 隔离 | `hiltViewModel(key = ...)` 的 key 含 `forumId` + `tabId`,fallback `tabId=0` 也不串 |
| 缓存命中范围 | LRU `(forumName) -> Map<tabId, list>`,换吧失整桶,切 tab 走 Map |
| "哪个 tab 显示子分类" | 用 `tabType` / `goodClassifies.size > 1`,**不写死** tabId 数字 |
| 默认进哪个 tab | 用 `isDefault=1` 决定,不写死"第一个" |
| 小吧没 NavTab | fallback `NavTab(tabId=0, tabName="全部")` 单项;UI 仍工作 |

## 7. 兼容 / 风险

1. **协议字段位置不确定**:tabId 写到 `cid` 还是 `category_id` 需要抓包实测。本设计先按 `cid` 推进,若实测不通在 5.2 节"待实测 TODO"处调整 —— 不影响上层结构。
2. **`tabType` 语义未文档化**:精华 tab 的 tabType 数值需要通过实际响应观察。短期回退方案:若 `tabType` 判定不可靠,改用 `forum.good_classify.size > 1` 推断"当前 tab 是精华类"(基于:协议返回 good_classify 列表非空就意味着当前 tab 需要展示子分类)。
3. **存量 LRU 缓存兼容**:`ForumCache` 是进程内的 `LruCache`,无持久化,改结构无迁移问题。
4. **行为回归**:旧"精品"Tab 用户切到"精品"再选"精品贴 1"的链路,新版等价于"选精华 tab + 选攻略心得贴 subtab",体感一致。

## 8. 测试策略

- **JVM 单测**:`FrsPageResponseData.toData()` 的 `nav_tab_info → navTabs` 映射,覆盖 (a) 正常多 tab (b) 空 tab fallback (c) 多个 isDefault 取第一个。
- **手测**(无 instrumented test):
  - 王者荣耀吧:8 个 tab 全部能加载;切 tab 列表刷新;精华 tab 下子分类 Chip 可点。
  - 一个小吧(如个人吧):验证 fallback "全部"单 tab 行为。
  - 切到精华、选某 subtab、跳出再回来,subClassifyId 保留(在同一个 forumName 范围内)。

## 9. 后续(超本轮范围,先记一笔)

- 持久化"上次选择的分区"(按 forumName 写 DataStore,类似现有 `sortType`)。
- 网页 URL `?tab=` 深链:`ClipBoardLinkDetector.parseDeepLink` 已识别贴吧链接,扩展 `Destination.Forum` 加 `initialTabId: Int?` 字段。
- 发帖时选目标分区(`Reply` 页面接入)。
