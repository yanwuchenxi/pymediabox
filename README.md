# PyMediaBox

Android 影音播放器，参照 **TVBoxOS-Mobile** 与 **Chaquopy** 项目设计，支持 Python 爬虫解析视频源。

## 设计来源
- **TVBoxOS-Mobile**（第一个参考项目）：借鉴其 `Spider` 抽象接口
  （`homeContent`/`categoryContent`/`detailContent`/`searchContent`/`playerContent`）、
  模块化拆分（`player`/`quickjs`/`crash`）、以及 GitHub Actions 用
  `./gradlew assembleRelease` 产出 APK 验证的 CI 方式。
- **Chaquopy APK**（第二个参考项目）：借鉴其把 CPython 嵌入 Android 运行
  Python 爬虫源（`spider.py`）+ 打包 `requests`/`lxml`/`pycryptodome`
  等依赖的路线。

## 功能
- **影音播放**：系统 `VideoView`（在线 / 本地 / 分享 / 下一个 入口），本地媒体
  目录扫描 + 权限申请；播放页带进度条拖动、重播、全屏、收藏、下一个。
- **断点续播**：`ResumeManager` 按 URL 记忆播放进度（设置可开关），
  重播 / 播完自动清除。
- **API 源管理**：`ApiSourceManager` TVBox 式 JSON 源（内置演示源 + 用户
  自定义增删），首页下拉分类来自 API 源。
- **Python 支持**：Chaquopy 15.0.1 + Python 3.10；`spider.py` 以
  `home_content`/`category_content`/`search_content`/`detail_content`/
  `player_content` 方法返回 JSON（TVBox Spider 接口的 Python 版），
  配套单元测试 `test_spider.py`。
- **构建**：AGP 8.2.2 + JDK 17；`PKCS12` 签名库已入库（密码 `pymediabox`），
  `assembleRelease` 产出签名 APK。
- **CI**：`python -m unittest` + `./gradlew assembleRelease` + 上传 APK artifact。

## 目录
```
.github/workflows/build.yml   CI
app/
  src/main/java/com/pymediabox/app/
    PymediaBoxApp.java        初始化 Python 解释器
    MainActivity.java         主页：本地 / Python 爬虫 / 在线示例 三入口
    PlayerActivity.java       播放页（VideoView）
    PySpiderCache.java        爬虫结果缓存
  src/main/python/
    spider.py                 Python 爬虫（TVBox 接口）
    test_spider.py            单元测试
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
- `assembleRelease` + 签名 + 上传 APK artifact

## 许可
Apache-2.0
