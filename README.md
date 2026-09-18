# PyMediaBox

Android 影音播放器，参照 **TVBoxOS-Mobile** 与 **Chaquopy** 项目设计，支持 Python 爬虫解析视频源。

## 设计来源
- **TVBoxOS-Mobile**：借鉴其 `Spider` 抽象接口（`homeContent`/`categoryContent`/`detailContent`/`searchContent`/`playerContent`）、模块化结构、以及 GitHub Actions 用 `./gradlew assembleRelease` 产出 APK 验证的 CI 方式。
- **Chaquopy APK**：借鉴其"CPython 嵌入 Android 跑 Python 爬虫源 + 打包 requests/lxml/pycryptodome"路线，改用 Chaquopy 15.0.1 + Python 3.10 官方实现。
- **GSYVideoPlayer / ExoPlayer demo**：借鉴播放历史、收藏、断点续播、多源管理等功能设计。

## 功能
- **影音播放**：`PlayerActivity`（在线 / 本地 / 分享 / 下一个），进度条拖动、重播、全屏、收藏、下一个；本地媒体目录扫描 + 权限申请。
- **断点续播**：`ResumeManager` 按 URL 记忆播放进度（设置可开关），重播 / 播完自动清除。
- **API 源管理**：`ApiSourceManager` TVBox 式 JSON 源（内置演示源 + 用户自定义增删），首页推荐列表分类即来自 API 源。
- **Python 爬虫**：`spider.py` 实现 TVBox Spider 协议 Python 版（JSON 返回），设置页内置"爬虫调试器"（ChipGroup 选接口 + 参数 + 即时响应）。
- **首页分段筛选**：推荐 / 历史 / 收藏 三种视图切换（原队列页功能合并）。

## 页面结构（2 Tab）
| Tab | 内容 |
|---|---|
| 首页 | 搜索栏 + 本地/默认源快捷 + 推荐/历史/收藏分段筛选 + 卡片列表 |
| 设置 | 播放行为（常亮/续播/默认源）· API 源管理 · Python 爬虫调试器 · 数据管理 · 关于 |

预览图见 `preview/out/`（1080×2400 高保真 mockup，可用 `python3 preview/gen_preview.py` 重新生成）。

## 目录
```
.github/workflows/build.yml   CI
app/
  src/main/java/com/pymediabox/app/
    PymediaBoxApp.java        初始化 Python 解释器
    MainActivity.java         2 Tab 框架
    HomeFragment.java         首页（搜索+分段筛选+卡片列表）
    SettingsFragment.java     设置（含爬虫调试器）
    PlayerActivity.java       播放页（进度/重播/全屏/收藏/下一个）
    ApiSourceManager.java     TVBox 式 API 源
    HistoryManager.java       播放历史+收藏
    ResumeManager.java        断点续播
  src/main/python/
    spider.py                 Python 爬虫（TVBox 接口）
    test_spider.py            单元测试
  src/main/res/layout/        布局（fragment_home / fragment_settings / item_card ...）
preview/
  gen_preview.py              预览图生成脚本
  out/01_home.png 02_settings.png
signing/
  pymediabox-release.p12      签名库（密码 pymediabox）
```

## 本地构建
```
./gradlew assembleRelease
```
APK 输出到 `app/build/outputs/apk/release/app-release.apk`。

## 构建验证
GitHub Actions：https://github.com/yanwuchenxi/pymediabox/actions
- Python 单元测试（spider 接口 3 项）
- `assembleRelease` + PKCS12 签名 + 上传 APK artifact

## 许可
Apache-2.0
