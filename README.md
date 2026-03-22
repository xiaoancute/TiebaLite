# TiebaLite

非官方百度贴吧客户端，正在复活中。

An unofficial Baidu Tieba client, currently being revived.

## 当前状态

这是一个**阅读优先**的客户端，不是完整的贴吧替代品。

**能用的：**
- 首页、动态、热榜、全局搜索
- 吧页（最新 / 精华 / 看图 / 吧内搜索）
- 帖子阅读（楼中楼、只看楼主、正倒序、帖内搜索）
- 图片大图、视频播放、外链跳转
- AI 帖子摘要（需自行配置 API）
- 主题切换、Material You 动态配色

**能用但需登录：**
- 登录、通知、手动签到

**实验性（有风险提示）：**
- 回帖、自动签到

**不承诺的：**
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

- 功能分级与能力边界：[docs/feature-status.md](docs/feature-status.md)

## 相关项目

- [Starry-OvO/aiotieba](https://github.com/Starry-OvO/aiotieba) — 异步贴吧 Python 客户端
- [n0099/tbclient.protobuf](https://github.com/n0099/tbclient.protobuf) — 贴吧 Protobuf 定义合集

## 声明

本项目及源码仅供学习交流使用，严禁用于商业用途。
