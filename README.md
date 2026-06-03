# <p align="center">Tieba Lite</p>
<div align="center">
    <a href="https://github.com/xiaoancute/TiebaLite/blob/main/LICENSE">
        <img alt="License" src="https://img.shields.io/github/license/xiaoancute/TiebaLite">
    </a>
    <a href="https://github.com/xiaoancute/TiebaLite/actions/workflows/build.yml">
        <img alt="Build Status" src="https://github.com/xiaoancute/TiebaLite/actions/workflows/build.yml/badge.svg?branch=main">
    </a>
    <a href="https://t.me/tblite_discuss">
        <img alt="Status" src="https://img.shields.io/badge/-Telegram-blue?logo=telegram&style=flat">
    </a>
    <br/>
    <br/>
    <p>一个<strong>第三方</strong>贴吧 Android 客户端, 基于 Jetpack Compose</p>
    <img src="assets/Screenshots.png" alt="Screenshots" />
</div>

## 说明

**本软件及源码仅供学习交流使用，严禁用于商业用途。**

本仓库是 [0ranko0P/TiebaLite](https://github.com/0ranko0P/TiebaLite) 的一个 fork。

### 主要改动

- **吧内分区 (NavTab)** — 顶部标签栏由协议 `nav_tab_info` 动态驱动(替换原版"最新/精品"双 Tab), 支持按吧不同呈现分区。自定义分区(开黑/视频/吧主推荐等 `isGeneralTab=1`)走独立的 `/c/f/frs/generalTabList` 端点。
- **精华子分类** — "精华"标签下保留子分类 Chip 条(`good_classify`), 按当前 tab 自动显隐。
- **减少动画** — 设置里提供"减少动画"开关, 关闭后分区切换的颜色淡入淡出、自动滚入视口等动画直接瞬切。

> 目前走 App protobuf 协议, 与网页版分区内容不完全一致(不同后端)。网页版严格对齐的方案见 `docs/superpowers/specs/2026-05-20-forum-web-pc-bridge.md`, 欢迎参与。

其他修改与上游源仓库保持一致。

## 下载
* [Github Releases](https://github.com/xiaoancute/TiebaLite/releases)
* 下载每夜版: [Github Actions](https://github.com/xiaoancute/TiebaLite/actions/workflows/build.yml)

## 构建

1. 克隆仓库
```shell
git clone https://github.com/xiaoancute/TiebaLite.git
cd TiebaLite
```

2. 配置应用签名 (非必须)

编辑 `signing.properties.example` 填写密钥库路径，密钥别名与密码。保存为 `signing.properties`

3. 开始构建
```shell
./gradle assembleRelease
```

成功后，构建的产物在 `app/build/outputs/apk` 下。


## 友情链接

+ [0ranko0P/TiebaLite](https://github.com/0ranko0P/TiebaLite) — 上游源仓库
+ [Starry-OvO/aiotieba: Asynchronous I/O Client for Baidu Tieba](https://github.com/Starry-OvO/aiotieba)
+ [n0099/tbclient.protobuf: 百度贴吧客户端 Protocol Buffers 定义文件合集](https://github.com/n0099/tbclient.protobuf)
+ [5ec1cff/RoTieba](https://github.com/5ec1cff/RoTieba) — generalTabList 端点实现思路参考
