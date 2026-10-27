# TV

Android 影音应用，仓库：[PFPT/TV](https://github.com/PFPT/TV)。参考开源项目 [FongMi/TV](https://github.com/FongMi/TV)。

同时支持 Android TV 大屏和手机，内容通过外部配置扩展。

## 项目架构

| 项目 | 值 |
| --- | --- |
| package | `com.pfpt.tv` |
| minSdk | 24（Android 7.0） |
| abi | `arm64-v8a`、`armeabi-v7a` |
| flavor | `leanback`（电视）、`mobile`（手机） |

```
TV/
├── app/       主应用，含 leanback 与 mobile 两套界面
├── catvod/    爬虫抽象层（Spider、OkHttp）
├── quickjs/   QuickJS JavaScript 引擎
├── chaquo/    Chaquopy Python 引擎
```

`app/src/main/` 是两个版本共用的业务逻辑，`app/src/leanback/` 与 `app/src/mobile/` 各自实现界面。

## 播放器

- 核心为 ExoPlayer（Media3），并带 FFmpeg 软解，硬解失败时可切换软解
- 渲染使用 SurfaceView / TextureView
- DRM 支持 Widevine、PlayReady、ClearKey
- 弹幕与播放时间轴同步，可接收远程推送
- 字幕支持 SRT / SSA / ASS、系统字幕和远程注入
- 支持倍速、缩放、画中画、后台音频，以及片头片尾跳过

## 点播

- 多站点分类浏览，可按年份、地区、类型筛选
- 多站点并行搜索
- 播放失败时按解析器、线路、其他站点依次换源
- 观看记录保留 60 天，支持收藏和无痕模式
- 电视版使用遥控器；手机版支持亮度、音量、进度手势，以及上下滑切集

## 直播

- 直播源支持 M3U、TXT（`#genre#` 分组）和 JSON
- EPG 使用 XMLTV（含 `.gz`），每 6 小时刷新
- 支持追看和时移
- 频道可收藏，分组可用密码隐藏

## 爬虫

爬虫可以用三种方式编写：

- Java JAR（DexClassLoader）
- JavaScript（QuickJS）
- Python（Chaquopy）

配置里用 `api` 指定爬虫，用 `ext` 传入初始化参数。

## 网络

- DNS over HTTPS，可配置 Bootstrap IP
- HTTP / HTTPS / SOCKS 代理，按 host 规则选择
- Hosts 覆盖，支持通配符
- 按 host 注入响应头
- 广告域名黑名单
- WebView 嗅探媒体地址，可伪装 UA

## 投放与车机

- 手机版作为投放端，扫描局域网 DLNA 设备
- 电视版作为接收端，接收其他设备的投放
- 电视版实现 Android Auto 媒体库，可在车机上续播点播记录和切换直播频道

## 远程控制

应用启动后在本机开启 HTTP 服务，端口从 9978 检测到 9998，用于播放控制、推送字幕和弹幕，以及多设备同步。

## 配置

点播配置通过 URL 或本地路径加载，顶层包括：

- 点播站点 `sites`、解析规则 `parses`
- 直播来源 `lives`
- 网络设置 `doh`、`proxy`、`hosts`、`ads`

直播配置可以写在点播配置里，也可以单独存放。
