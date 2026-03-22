# TiebaLite

TiebaLite 是一个正在复活中的非官方贴吧客户端。 TiebaLite is an unofficial Baidu Tieba client currently being revived.

## 项目现状 / Project Status

- 当前完成标准是“阅读优先可用”，不是完整贴吧替代品。 English: the current target is reading-first usability, not a full Tieba replacement.
- 当前推荐用途是公开浏览：首页、动态热榜、搜索、吧页、帖子页、图片、视频、外链。 English: the recommended use today is public browsing across home, explore/hot feeds, search, forums, threads, images, video, and outbound links.
- Android 主支持范围是 `10+`；更低版本只保留尽量不坏的最低维护态。 English: primary Android support is `10+`; older versions are best-effort only.
- 登录、通知、手动签到仍属保守守卫能力，只有完整会话下才会进入稳定路径。 English: login, notifications, and manual sign remain conservative guarded features and only enter the stable path with a complete session.
- 发帖、回帖、自动签到不作为稳定承诺。 English: posting, replying, and auto sign are intentionally not promised stable.

## What Works Now / 当前能做什么

- 首页、公开动态流、全局搜索、吧页、帖子页、话题榜和话题详情已经形成阅读主线闭环。 English: home, public explore feeds, search, forums, threads, hot topics, and topic detail now form a complete reading-first mainline.
- Tieba 站内帖子和吧链接会尽量优先回到原生页面，`/mo/q/checkurl` 也会先解析真实目标；外站链接继续交给系统浏览器或 Custom Tabs。 English: Tieba thread/forum links prefer native pages, `/mo/q/checkurl` is resolved before routing, and external links continue to go to the system browser or Custom Tabs.
- “我”页高频入口已经按状态收口：消息、收藏、服务中心不会再把半登录用户带进误导性的半成品页面。 English: high-frequency entries in the Me page are now state-aware, so notifications, favorites, and service-center routes no longer dump incomplete sessions into misleading half-working pages.
- 设置页、关于页和主要浏览页会明确说明当前是“公开浏览优先”的复活分支。 English: Settings, About, and major browse surfaces now state clearly that this branch is public-browsing first.

## What Is Intentionally Not Promised / 当前不承诺什么

- 不承诺发帖、回帖、自动签到的稳定闭环。 English: no stable promise is made for posting, replying, or auto sign.
- 不承诺登录、通知、手动签到已经完成真实账号端到端验证。 English: login, notifications, and manual sign are not yet claimed as fully validated with a real end-to-end account test.
- 不承诺已经完成对现行贴吧网页全部论坛内容维度的对齐。 English: the app does not yet claim parity with every modern forum surface from the current Tieba web experience.
- 不承诺 Android `15/16` 的 target SDK 升级已经完成。 English: Android `15/16` target SDK migration is still not complete.

## Build & Run / 构建与运行

- 需要 `JDK 17`、Android SDK `34`、`build-tools 34.0.0`。 English: you need `JDK 17`, Android SDK `34`, and `build-tools 34.0.0`.
- 可先运行 `scripts/check-android-env.sh` 检查本机环境。 English: run `scripts/check-android-env.sh` first if you want a quick environment check.
- 构建 Debug 包：`./gradlew :app:assembleDebug`。 English: build the debug APK with `./gradlew :app:assembleDebug`.
- 当前已知稳定的本地构建环境说明见 `docs/development-setup.md`。 English: the currently known-good local build environment is documented in `docs/development-setup.md`.

## Docs / 文档入口

- 功能边界与对外能力分级：[`docs/feature-status.md`](docs/feature-status.md)
- 复活审计与技术债记录：[`docs/revival-audit.md`](docs/revival-audit.md)
- 当前恢复进度与已验证命令：[`STATUS.md`](STATUS.md)
- 本地构建环境说明：[`docs/development-setup.md`](docs/development-setup.md)

## Maintainer Notes / 维护说明

- 对外口径固定为：`公开浏览稳定主线 + 核心账号保守守卫 + 发帖/回帖/自动签到不承诺稳定`。 English: keep the public message fixed as `stable public browsing + conservative guarded core account + no stability promise for posting/replying/auto sign`.
- 账号能力是否可进入稳定路径，继续以 `SessionHealth` 作为唯一判断源。 English: keep `SessionHealth` as the single source of truth for whether account features may enter the stable path.
- 更细的能力边界以 `docs/feature-status.md` 为准；README 只做入口级摘要。 English: `docs/feature-status.md` remains the detailed source of truth, while this README stays at entry-level summary depth.
- 本项目及源码仅供学习交流使用，不应用于商业用途。 English: this project and its source are for learning and research only, not for commercial use.
