package com.pymediabox.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 播放信息卡：记录正在播/上次的 视频名 + 预告/EPG 信息 + 集数。
 * 对应截图：视频区下方的信息卡片（名称 | 预告 | 集数 | 更多）。
 */
public class PlaybackInfoManager {

    private final SharedPreferences prefs;

    public PlaybackInfoManager(Context ctx) {
        prefs = ctx.getSharedPreferences("pymediabox_info", Context.MODE_PRIVATE);
    }

    /** 设置当前播放条目 */
    public void setNow(String name, String preview, int episode) {
        SharedPreferences.Editor e = prefs.edit();
        e.putString("now_name", name);
        e.putString("now_preview", preview);
        e.putInt("now_episode", episode);
        e.putLong("now_ts", System.currentTimeMillis());
        e.apply();
    }

    public String nowName()   { return prefs.getString("now_name", ""); }
    public String nowPreview() { return prefs.getString("now_preview", "暂时没有播放预告"); }
    public int nowEpisode()   { return prefs.getInt("now_episode", 1); }

    /** 最近播放（信息卡"上次看到"） */
    public void setLast(String name) {
        prefs.edit().putString("last_name", name).apply();
    }

    public String lastName() { return prefs.getString("last_name", ""); }

    /** EPG 地址 */
    public void setEpg(String url) {
        prefs.edit().putString("epg_url", url).apply();
    }

    public String epgUrl() { return prefs.getString("epg_url", ""); }
}
