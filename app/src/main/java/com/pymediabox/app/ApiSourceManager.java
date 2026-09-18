package com.pymediabox.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * TVBox / 蜂蜜影视 式 API 源管理。
 *
 * 源配置采用 TVBox JSON 协议（与 gaotianliuyun/gao 等蜂蜜源仓库同构）：
 *   {
 *     "class": [{"type": "1", "name": "电影"}, ...],
 *     "urls":  [{"type": "1", "url": "https://...?class=电影&page={page}", "detail": "https://..."}],
 *     "demo":  true / false
 *   }
 * 内置演示源解析真实 JSON，自定义源从 URL 拉取。
 */
public class ApiSourceManager {

    private final SharedPreferences prefs;

    public ApiSourceManager(Context ctx) {
        prefs = ctx.getSharedPreferences("pymediabox_sources", Context.MODE_PRIVATE);
    }

    public static class Source {
        public final String key, name, url;
        public Source(String key, String name, String url) {
            this.key = key; this.name = name; this.url = url;
        }
    }

    public static final String BUILTIN_KEY = "builtin";

    /** 内置演示源（蜂蜜 TVBox 协议，本地 JSON 数据） */
    public static Source builtin() {
        return new Source(BUILTIN_KEY, "内置演示源", "pymediabox://builtin");
    }

    /** 内置源完整 JSON（class + urls + detail），仿蜂蜜源结构 */
    public static String builtinJson() {
        return "{\n"
            + "  \"class\": [\n"
            + "    {\"type\": \"1\", \"name\": \"电影\"},\n"
            + "    {\"type\": \"2\", \"name\": \"剧集\"},\n"
            + "    {\"type\": \"3\", \"name\": \"综艺\"},\n"
            + "    {\"type\": \"4\", \"name\": \"动漫\"}\n"
            + "  ],\n"
            + "  \"urls\": [\n"
            + "    {\"type\": \"1\", \"url\": \"https://example.com/vod?type=1&page={page}\", "
            + "\"detail\": \"https://example.com/vod/{id}\"},\n"
            + "    {\"type\": \"2\", \"url\": \"https://example.com/vod?type=2&page={page}\", "
            + "\"detail\": \"https://example.com/vod/{id}\"},\n"
            + "    {\"type\": \"3\", \"url\": \"https://example.com/vod?type=3&page={page}\", "
            + "\"detail\": \"https://example.com/vod/{id}\"},\n"
            + "    {\"type\": \"4\", \"url\": \"https://example.com/vod?type=4&page={page}\", "
            + "\"detail\": \"https://example.com/vod/{id}\"}\n"
            + "  ]\n"
            + "}";
    }

    public List<Source> sources() {
        List<Source> list = new ArrayList<>();
        list.add(builtin());
        try {
            JSONArray arr = new JSONArray(prefs.getString("user", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Source(o.getString("key"), o.getString("name"), o.getString("url")));
            }
        } catch (Exception ignored) { }
        return list;
    }

    public boolean contains(String key) {
        for (Source s : sources()) if (s.key.equals(key)) return true;
        return false;
    }

    public Source byKey(String key) {
        for (Source s : sources()) if (s.key.equals(key)) return s;
        return null;
    }

    public boolean addSource(String name, String url) {
        if (name == null || name.isEmpty() || url == null || !url.startsWith("http"))
            return false;
        String key = "s" + System.currentTimeMillis() % 100000;
        try {
            JSONArray arr = new JSONArray(prefs.getString("user", "[]"));
            JSONObject o = new JSONObject();
            o.put("key", key);
            o.put("name", name);
            o.put("url", url);
            arr.put(o);
            prefs.edit().putString("user", arr.toString()).apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void removeSource(String key) {
        if (BUILTIN_KEY.equals(key)) return;
        try {
            JSONArray arr = new JSONArray(prefs.getString("user", "[]"));
            JSONArray out = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (!o.getString("key").equals(key)) out.put(o);
            }
            prefs.edit().putString("user", out.toString()).apply();
        } catch (Exception ignored) { }
    }

    /**
     * 获取源首页分类列表（TVBox class 协议）。
     * @return 分类名列表，失败返回 null
     */
    public List<String> homeClasses(Source src) {
        String json = jsonOf(src);
        if (json == null) return null;
        try {
            JSONArray arr = new JSONObject(json).getJSONArray("class");
            List<String> names = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++)
                names.add(arr.getJSONObject(i).getString("name"));
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    /** 获取某分类下的视频列表（TVBox urls 协议，占位符替换 {page}）。 */
    public List<String[]> categoryItems(Source src, String className, int page) {
        String json = jsonOf(src);
        if (json == null) return null;
        try {
            JSONObject root = new JSONObject(json);
            JSONArray classes = root.getJSONArray("class");
            JSONArray urls = root.optJSONArray("urls");
            String typeId = "1";
            for (int i = 0; i < classes.length(); i++) {
                JSONObject c = classes.getJSONObject(i);
                if (c.getString("name").equals(className)) { typeId = c.getString("type"); break; }
            }
            // 演示源：直接生成示例条目
            List<String[]> out = new ArrayList<>();
            if (BUILTIN_KEY.equals(src.key)) {
                String[] titles = {className + " · 示例片 1", className + " · 示例片 2",
                        className + " · 示例片 3", className + " · 示例片 4"};
                for (String t : titles)
                    out.add(new String[]{t, "https://example.com/" + typeId + "/v" + out.size()});
                return out;
            }
            // 自定义源：替换占位符请求
            if (urls != null) {
                for (int i = 0; i < urls.length(); i++) {
                    JSONObject u = urls.getJSONObject(i);
                    if (u.getString("type").equals(typeId)) {
                        String realUrl = u.getString("url")
                                .replace("{page}", String.valueOf(page));
                        String body = fetchJson(realUrl);
                        if (body != null) {
                            // 解析 TVBox 列表协议 {"list": [{"name","vod_id",...}]}
                            JSONArray list = new JSONObject(body).optJSONArray("list");
                            if (list != null) {
                                for (int j = 0; j < list.length(); j++) {
                                    JSONObject it = list.getJSONObject(j);
                                    out.add(new String[]{
                                            it.optString("name", "未知"),
                                            it.optString("url", it.optString("vod_id", ""))});
                                }
                            }
                        }
                        break;
                    }
                }
            }
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    private String jsonOf(Source src) {
        if (BUILTIN_KEY.equals(src.key)) return builtinJson();
        return fetchJson(src.url);
    }

    private static String fetchJson(String url) {
        if (url.startsWith("pymediabox://")) return null;
        try {
            java.net.HttpURLConnection conn =
                    (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) PyMediaBox/1.1");
            int code = conn.getResponseCode();
            if (code != 200) { conn.disconnect(); return null; }
            java.io.InputStream is = conn.getInputStream();
            byte[] buf = new byte[8192];
            int n;
            StringBuilder sb = new StringBuilder();
            while ((n = is.read(buf)) > 0) sb.append(new String(buf, 0, n, "UTF-8"));
            conn.disconnect();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
