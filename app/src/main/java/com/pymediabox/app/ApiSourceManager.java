package com.pymediabox.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * TVBox 式 API 源管理（借鉴 TVBoxOS 的 ApiConfig/Source 设计）。
 * 每个源 = {key, name, url}，url 指向一个返回 {"class": [...]} 的 JSON 接口。
 * 内置 Big Buck Bunny 演示源，保证离线可用；用户可在设置中增删源。
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

    /** 内置演示源（返回 TVBox 格式 class 列表） */
    public static Source builtin() {
        return new Source(BUILTIN_KEY, "内置演示源", "pymediabox://builtin");
    }

    /** 获取全部源：内置 + 用户自定义 */
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

    /** 添加自定义源 */
    public boolean addSource(String name, String url) {
        if (name == null || name.isEmpty() || url == null || !Uri.parse(url).isNetworkUri())
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

    /** 删除自定义源（内置不可删） */
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
     * 获取源首页分类列表（TVBox homeContent 协议）。
     * 内置源直接返回本地数据；自定义源从 url 拉取 JSON。
     *
     * @return 分类名列表，失败时返回 null
     */
    public List<String> homeClasses(Source src) {
        String json;
        if (BUILTIN_KEY.equals(src.key)) {
            json = "{\n"
                    + "  \"class\": [\n"
                    + "    {\"type\": \"1\", \"name\": \"电影\"},\n"
                    + "    {\"type\": \"2\", \"name\": \"剧集\"},\n"
                    + "    {\"type\": \"3\", \"name\": \"综艺\"},\n"
                    + "    {\"type\": \"4\", \"name\": \"动画\"}\n"
                    + "  ]\n"
                    + "}";
        } else {
            json = fetchJson(src.url);
            if (json == null) return null;
        }
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

    private static String fetchJson(String url) {
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
