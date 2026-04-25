# TiebaLite

面向阅读体验的非官方百度贴吧客户端。

An unofficial Baidu Tieba client focused on reading and browsing.

## 当前状态

这是一个**阅读优先**的客户端，主要体验围绕浏览、搜索、帖子阅读和本地阅读辅助功能持续维护。

**能用的：**
- 首页、动态、热榜、全局搜索
- 吧页（最新 / 精华 / 看图 / 吧内搜索）
- 帖子阅读（楼中楼、只看楼主、正倒序、帖内搜索）
- 图片大图、视频播放、外链跳转
- AI 帖子摘要（需自行配置 API）
- 主题切换、Material You 动态配色

**能用但需登录：**
- 登录、通知、手动签到

**高风险功能（有风险提示）：**
- 回帖、自动签到

**暂不承诺的：**
- 发帖完整闭环
- Android 15/16 target SDK 适配

## 系统要求

Android 10+，更低版本不保证。

## 构建

需要 JDK 17、Android SDK 34、Build-Tools 34.0.0。

```bash
# 检查环境
scripts/check-android-env.sh

# Debug 构建
./gradlew :app:assembleDebug

# Release 构建（需要 keystore.properties）
./gradlew assembleRelease

# 运行测试
./gradlew :app:testDebugUnitTest --console=plain
```

## 文档

- 功能现状说明：[docs/feature-status.md](docs/feature-status.md)

## 相关项目

- [Starry-OvO/aiotieba](https://github.com/Starry-OvO/aiotieba) — 异步贴吧 Python 客户端
- [n0099/tbclient.protobuf](https://github.com/n0099/tbclient.protobuf) — 贴吧 Protobuf 定义合集

## 声明

本项目及源码仅供学习交流使用，严禁用于商业用途。
