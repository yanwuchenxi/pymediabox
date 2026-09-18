"""PyMediaBox Python Spider

参照 TVBoxOS-Mobile 的 Spider 接口设计，支持 Python 解析。
实现 home_content / category_content / search_content / detail_content /
player_content 方法，返回 JSON 字符串。
"""
import json
import urllib.request
import urllib.parse
import ssl

class PySpider:
    def __init__(self, base_url=""):
        self.base_url = base_url.rstrip("/")
        self.ctx = ssl.create_default_context()
        self.ctx.check_hostname = False
        self.ctx.verify_mode = ssl.CERT_NONE

    def _get(self, url, referer=""):
        req = urllib.request.Request(url, headers={
            "User-Agent": "Mozilla/5.0 (Linux; Android 14) PyMediaBox/1.0",
            "Referer": referer,
        })
        with urllib.request.urlopen(req, context=self.ctx, timeout=15) as r:
            return r.read().decode("utf-8", errors="replace")

    def home_content(self):
        """返回首页分类列表，JSON 字符串"""
        # 示例：返回内置分类
        data = {
            "class": [
                {"type": "1", "name": "电影"},
                {"type": "2", "name": "剧集"},
                {"type": "3", "name": "综艺"},
            ]
        }
        return json.dumps(data, ensure_ascii=False)

    def category_content(self, tid="1", pg="1"):
        """返回分类下的视频列表，JSON 字符串"""
        data = {
            "list": [
                {"name": "示例 1", "type": tid, "link": "https://example.com/1"},
                {"name": "示例 2", "type": tid, "link": "https://example.com/2"},
            ]
        }
        return json.dumps(data, ensure_ascii=False)

    def search_content(self, key=""):
        """搜索，JSON 字符串"""
        data = {"list": []}
        if key:
            data["list"].append({"name": key, "type": "movie", "link": "https://example.com?q=" + urllib.parse.quote(key)})
        return json.dumps(data, ensure_ascii=False)

    def detail_content(self, id_=""):
        """详情，JSON 字符串"""
        return json.dumps({"name": "详情 " + id_, "link": "https://example.com/" + id_}, ensure_ascii=False)

    def player_content(self, flag="", id_=""):
        """播放器地址，JSON 字符串"""
        return json.dumps({"url": "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4"}, ensure_ascii=False)

def create_spider(base_url=""):
    return PySpider(base_url)
