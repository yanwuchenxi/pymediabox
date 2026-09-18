package com.pymediabox.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 频道收藏（借鉴直播类 App 的"收藏频道"设计）。
 * 频道 = {key, name, url, group}，url 为直播流。
 */
public class ChannelManager {

    private final SharedPreferences prefs;

    public ChannelManager(Context ctx) {
        prefs = ctx.getSharedPreferences("pymediabox_channels", Context.MODE_PRIVATE);
    }

    public static class Channel {
        public final String key, name, url, group;
        public Channel(String k, String n, String u, String g) {
            key = k; name = n; url = u; group = g;
        }
    }

    /** 内置演示频道 */
    public static Channel demo() {
        return new Channel("demo", "演示频道",
                "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4", "演示");
    }

    public List<Channel> all() {
        List<Channel> list = new ArrayList<>();
        list.add(demo());
        try {
            JSONArray arr = new JSONArray(prefs.getString("user", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Channel(o.getString("key"), o.getString("name"),
                        o.getString("url"), o.optString("group", "")));
            }
        } catch (Exception ignored) { }
        return list;
    }

    public boolean toggleFav(String key, String name, String url, String group) {
        try {
            JSONArray arr = new JSONArray(prefs.getString("user", "[]"));
            int idx = -1;
            for (int i = 0; i < arr.length(); i++)
                if (arr.getJSONObject(i).getString("key").equals(key)) { idx = i; break; }
            boolean fav;
            if (idx >= 0) { arr.remove(idx); fav = false; }
            else {
                JSONObject o = new JSONObject();
                o.put("key", key); o.put("name", name);
                o.put("url", url); o.put("group", group == null ? "" : group);
                arr.put(o); fav = true;
            }
            prefs.edit().putString("user", arr.toString()).apply();
            return fav;
        } catch (Exception e) { return false; }
    }

    public boolean isFav(String key) {
        try {
            JSONArray arr = new JSONArray(prefs.getString("user", "[]"));
            for (int i = 0; i < arr.length(); i++)
                if (arr.getJSONObject(i).getString("key").equals(key)) return true;
        } catch (Exception ignored) { }
        return false;
    }

    public List<Channel> favorites() {
        List<Channel> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString("user", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Channel(o.getString("key"), o.getString("name"),
                        o.getString("url"), o.optString("group", "")));
            }
        } catch (Exception ignored) { }
        return list;
    }
}
