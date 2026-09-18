package com.pymediabox.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 播放历史与收藏夹管理
 * 参考 GSYVideoPlayer / VKPlayer 的本地历史记录设计
 */
public class HistoryManager {

    private final SharedPreferences prefs;

    public HistoryManager(Context ctx) {
        prefs = ctx.getSharedPreferences("pymediabox_history", Context.MODE_PRIVATE);
    }

    /** 记录播放历史 */
    public void addHistory(String title, String url, String type) {
        try {
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date());
            JSONArray arr = new JSONArray(prefs.getString("history", "[]"));
            JSONObject obj = new JSONObject();
            obj.put("title", title);
            obj.put("url", url);
            obj.put("type", type);
            obj.put("time", ts);
            arr.put(0, obj);
            // 最多保留 50 条
            while (arr.length() > 50) arr.remove(arr.length() - 1);
            prefs.edit().putString("history", arr.toString()).apply();
        } catch (Exception ignored) { }
    }

    /** 获取播放历史 */
    public List<HistoryItem> getHistory() {
        List<HistoryItem> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString("history", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new HistoryItem(
                    o.getString("title"), o.getString("url"),
                    o.getString("type"), o.getString("time")));
            }
        } catch (Exception ignored) { }
        return list;
    }

    /** 切换收藏 */
    public boolean toggleFavorite(String url) {
        try {
            JSONArray arr = new JSONArray(prefs.getString("favorites", "[]"));
            int idx = -1;
            for (int i = 0; i < arr.length(); i++) {
                if (arr.getJSONObject(i).getString("url").equals(url)) { idx = i; break; }
            }
            boolean isFav;
            if (idx >= 0) {
                arr.remove(idx);
                isFav = false;
            } else {
                JSONObject o = new JSONObject();
                o.put("url", url);
                o.put("title", url.contains("/") ? url.substring(url.lastIndexOf('/') + 1) : url);
                arr.put(o);
                isFav = true;
            }
            prefs.edit().putString("favorites", arr.toString()).apply();
            return isFav;
        } catch (Exception e) {
            return false;
        }
    }

    /** 是否已收藏 */
    public boolean isFavorite(String url) {
        try {
            JSONArray arr = new JSONArray(prefs.getString("favorites", "[]"));
            for (int i = 0; i < arr.length(); i++)
                if (arr.getJSONObject(i).getString("url").equals(url)) return true;
        } catch (Exception ignored) { }
        return false;
    }

    /** 获取收藏列表 */
    public List<HistoryItem> getFavorites() {
        List<HistoryItem> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString("favorites", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new HistoryItem(o.getString("title"), o.getString("url"), "收藏", ""));
            }
        } catch (Exception ignored) { }
        return list;
    }

    /** 清除全部历史与收藏（保留进度由 ResumeManager 处理） */
    public void clearAll() {
        prefs.edit().clear().apply();
    }

    public static class HistoryItem {
        public final String title, url, type, time;
        HistoryItem(String t, String u, String ty, String tm) {
            title=t; url=u; type=ty; time=tm;
        }
    }
}
