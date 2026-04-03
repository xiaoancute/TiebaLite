# GitHub Release 自动检查更新设计

## 背景

TiebaLite 当前已经具备基于 GitHub Actions 的 APK 发布链路，但应用内只有版本展示，没有稳定可维护的“检查更新”能力。仓库中虽然残留了 `NewUpdateBean` 和部分更新文案，但没有完整调用链，也不适合继续扩展。

当前仓库的发布方式以 GitHub Release 为中心，因此这次设计选择直接复用 `xiaoancute/TiebaLite` 的 Release 作为更新源，而不是引入新的服务端。

## 目标

- 应用可自动检查 `xiaoancute/TiebaLite` 的新版本。
- 当前安装的是 `recovery` 通道时，只检查 `recovery` 通道的新版本。
- 用户可在“关于”页手动检查更新。
- 检测到新版本时，向用户展示版本信息、更新说明和下载入口。
- 下载行为复用系统下载能力，不实现静默安装。

## 非目标

- 不实现静默更新或后台无感安装。
- 不接入 `zzc10086/TiebaLite` 或其他上游仓库作为更新源。
- 不兼容旧的 `NewUpdateBean` 服务端协议。
- 不在第一期实现后台自动下载、增量更新、渠道混装切换。

## 当前项目现状

- 版本展示位于设置中的“关于”页和更多设置页，当前显示 `BuildConfig.VERSION_NAME`。
- GitHub Actions 已在 tag 构建时自动生成 prerelease 并上传 APK。
- 项目已有 `DataStore` 偏好体系、Compose 设置页体系和基于 Retrofit/OkHttp 的网络基础设施。
- 项目已有 `FileUtil.downloadBySystem(...)`，可复用系统 `DownloadManager` 发起 APK 下载。

## 方案选择

本设计采用“GitHub Releases API + update.json 清单文件”的方案。

### 备选方案比较

1. 客户端直接解析 GitHub Release 列表  
   改动最少，但需要从 tag、标题、asset 文件名中推断版本信息，发布格式稍有变化就容易失效。

2. GitHub Release + `update.json` 清单  
   由 CI 在发版时生成一个机器可读的清单，客户端只解析结构化信息。这个方案最稳、最可维护，也便于未来扩展多渠道。

3. 独立托管更新清单  
   客户端最简单，但会引入额外发布出口，与当前 GitHub Release 流程重复。

结论：选择方案 2。

## 总体架构

### 发布侧

- GitHub Actions 在 tag prerelease 构建时继续生成 APK。
- 在上传 APK 的同时，额外生成并上传 `update.json` 作为 release asset。
- `update.json` 记录当前 release 的结构化更新信息，作为客户端唯一可信的数据来源。

### 客户端侧

- 新增一个独立的 `UpdateRepository`，负责：
  - 拉取 GitHub Release 列表
  - 找到与当前安装通道匹配的最新 release
  - 读取该 release 下的 `update.json`
  - 比较远端与本地版本
- 关于页新增“检查更新”入口。
- 应用冷启动后异步进行自动检查，但不阻塞首页启动。

## 更新源规则

- 更新仓库固定为 `xiaoancute/TiebaLite`。
- 当前版本通道固定从本地构建信息推导，当前需求只支持 `recovery` 通道检查。
- 只接受 `update.json.channel` 与当前安装通道完全一致的版本。
- 版本比较只使用 `versionCode`，不使用 `versionName` 字符串比较。

## update.json 格式

`update.json` 为 release 资产之一，建议结构如下：

```json
{
  "repo": "xiaoancute/TiebaLite",
  "channel": "recovery",
  "versionCode": 390108,
  "versionName": "4.0.0-recovery.11",
  "tagName": "v4.0.0-recovery.11",
  "publishedAt": "2026-04-01T12:00:00Z",
  "prerelease": true,
  "changelog": "## Changes\n- ...",
  "apkName": "TiebaLite-v4.0.0-recovery.11-release.apk",
  "apkUrl": "https://github.com/xiaoancute/TiebaLite/releases/download/v4.0.0-recovery.11/....apk",
  "sha256": "..."
}
```

### 字段要求

- `repo`：固定为 `xiaoancute/TiebaLite`
- `channel`：当前阶段固定为 `recovery`
- `versionCode`：整数，作为是否更新的唯一比较依据
- `versionName`：展示给用户的版本名
- `tagName`：对应 GitHub tag
- `publishedAt`：发布时间，给 UI 展示使用
- `prerelease`：是否预发布
- `changelog`：完整更新说明文本
- `apkName`：APK 文件名
- `apkUrl`：下载 APK 的直链
- `sha256`：APK 校验值，第一期先写入清单，客户端可先保留校验扩展位

## 客户端行为设计

### 自动检查

- 应用冷启动后，在主界面稳定展示后异步触发自动检查。
- 自动检查默认开启。
- 自动检查使用本地时间戳节流，24 小时内最多执行一次。
- 自动检查静默进行，不展示 loading，不因失败打断用户。
- 只有在发现新版本且未被忽略时，才弹出更新提示。

### 手动检查

- 在“关于”页增加“检查更新”按钮。
- 手动检查不受 24 小时节流限制。
- 手动检查需要明确反馈：
  - 已是最新版本
  - 发现新版本
  - 检查失败

### 忽略版本

- 用户可选择“忽略此版本”。
- 自动检查遇到同一个 `versionCode` 时不再重复提醒。
- 手动检查仍然可以看到该版本，避免用户被永久挡住。

## 设置与本地状态

新增以下偏好项：

- `auto_check_app_update`：是否自动检查更新，默认 `true`
- `ignored_update_version_code`：忽略提醒的版本号，默认 `0`
- `last_update_check_at`：上次成功发起检查的时间戳，默认 `0`

说明：

- 不复用当前未被实际使用的 `checkCIUpdate`，避免语义混乱。
- 更新相关状态保持本地优先，不与账号绑定。

## UI 交互设计

### 关于页

- 在当前源码链接和版本信息区域附近增加“检查更新”入口。
- 版本展示继续沿用 `BuildConfig.VERSION_NAME`。
- 如果未来需要，也可在关于页中额外展示“更新通道：recovery”，但第一期不是必需。

### 更新提示对话框

当检测到新版本时，显示更新对话框，包含：

- 新版本版本名
- 发布通道
- 发布时间
- 更新说明

按钮行为：

- `以后提醒`：关闭对话框，不写忽略标记
- `忽略此版本`：记录 `ignored_update_version_code`
- `下载更新`：调用系统下载逻辑

如果远端缺少 `apkUrl` 或关键字段不完整：

- 隐藏 `下载更新`
- 可降级为“前往 Release 页面查看”，或者只允许用户关闭对话框

## 下载与安装策略

- 第一期下载能力复用现有 `FileUtil.downloadBySystem(...)` 和系统 `DownloadManager`。
- 点击“下载更新”后，直接下载 GitHub Release 中的 APK。
- 下载完成后，依赖系统下载通知进入安装流程。
- 不新增静默安装、后台自动安装、未知来源权限引导流程增强。

## 错误处理

### 自动检查

- 网络失败、GitHub 限流、解析失败、字段缺失时都静默失败。
- 自动检查可记录日志，但不弹 toast、不弹对话框。

### 手动检查

- 网络失败或解析失败时，以用户可理解的文案提示“检查更新失败”。
- 若远端数据存在但不满足通道匹配或字段完整性要求，则视为“没有可用更新”。

## GitHub Actions 调整

在 `.github/workflows/build.yml` 的 tag prerelease 流程中新增：

1. 读取 `output-metadata.json` 中的 `versionCode/versionName`
2. 计算 release APK 的 `sha256`
3. 组合生成 `update.json`
4. 将 `update.json` 与 APK 一起上传到对应 release

要求：

- `update.json` 必须与本次 release APK 一一对应
- 重新发布同一 tag 时，允许覆盖已有 `update.json`

## 测试与验收

### 单元测试

- `versionCode` 比较逻辑正确
- 通道过滤逻辑正确
- 忽略版本逻辑正确
- `update.json` 解析成功与缺字段兜底行为正确

### 集成验证

- 手动检查时，若当前已是最新版本，会明确提示
- 发现更高的 `recovery` 版本时，弹出更新对话框
- 点击“忽略此版本”后，自动检查不再重复提醒该版本
- 点击“下载更新”后，进入系统下载

### CI 验证

- tag 构建后 release 资产中同时包含 APK 与 `update.json`
- `update.json` 中的 `versionCode`、`versionName`、`apkUrl`、`sha256` 与实际产物一致

## 分阶段落地建议

### 第一阶段

- 增加 `update.json` 生成与上传
- 新增客户端解析模型与 `UpdateRepository`
- 关于页增加“检查更新”
- 支持手动检查、自动检查、忽略版本、系统下载

### 后续可扩展项

- APK `sha256` 客户端下载后校验
- 多发布通道共存
- 前台下载进度展示
- 安装包下载完成后的更顺滑跳转

## 结论

这次更新功能应定义为“自动检查 + 提示用户更新 + 用户手动下载/安装”。  
它基于 `xiaoancute/TiebaLite` 的 GitHub Release 和 `update.json` 清单工作，严格限定在当前安装通道内比较新版本，不引入新服务端，也不扩大为静默更新系统。
