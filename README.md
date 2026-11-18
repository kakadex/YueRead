# 曰读 YueRead

> 容器格式的本地阅读工具。软件本身不提供、不内置、不分发任何书籍内容，
> 所有内容需由用户自行准备与导入。

## 这是什么

曰读（YueRead）是基于 GPL-3.0 开源项目二次开发的 Android 阅读器。
它提供阅读界面、书源规则的解析引擎、本地 TXT/EPUB 解析、朗读与离线缓存等能力。

## 重要声明

- **本应用不提供任何书源、书城或书籍内容**，安装后书架与书源均为空。
- 书源规则的合法性由使用者自行负责；请仅用于你有权访问的内容。
- 本项目不采集任何遥测数据，无崩溃上报、无统计埋点。
- 本应用无服务端，数据只保存在本机；WebDAV 同步由用户自备服务器。

## 权限说明

| 权限 | 用途 |
|---|---|
| 存储 / 管理外部存储 | 打开本地书籍、导入导出备份 |
| 网络 | 访问用户自行配置的书源 |
| 前台服务 | 后台朗读、音频/视频播放 |
| 通知 | 播放控制与下载进度 |
| 悬浮窗 | 阅读悬浮球、播放窗口 |
| 安装未知应用 | 应用内更新（可选） |

## 构建

```bash
# 需要 JDK 17 与 Android SDK 36
./gradlew :app:assembleAppRelease
```

签名配置从 `keystore/signing.properties` 读取（该文件与密钥不入库）：

```properties
RELEASE_STORE_FILE=../keystore/yueread-release.jks
RELEASE_STORE_PASSWORD=****
RELEASE_KEY_ALIAS=yueread
RELEASE_KEY_PASSWORD=****
```

## 许可证

本项目继承上游 GPL-3.0 许可证，详见 `LICENSE`。
修改与再分发需同样以 GPL-3.0 开源，并保留原始版权声明。
