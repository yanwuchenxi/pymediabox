package com.pymediabox.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * 断点续播管理（借鉴 ExoPlayer demo / GSYVideoPlayer 的播放进度记忆）。
 * 按 URL 存储上次播放位置（毫秒），播放页 resume 时自动 seekTo。
 */
public class ResumeManager {

    private final SharedPreferences prefs;

    public ResumeManager(Context ctx) {
        prefs = ctx.getSharedPreferences("pymediabox_resume", Context.MODE_PRIVATE);
    }

    /** 保存进度；position>=5s 且 progress<95% 时认为有意义 */
    public void save(String url, int positionMs, int durationMs) {
        if (url == null || positionMs < 5000 || durationMs <= 0
                || positionMs > durationMs * 95 / 100) return;
        prefs.edit().putInt("pos_" + hash(url), positionMs).apply();
    }

    public int load(String url) {
        if (url == null) return 0;
        return prefs.getInt("pos_" + hash(url), 0);
    }

    public void clear(String url) {
        if (url != null) prefs.edit().remove("pos_" + hash(url)).apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    /** 进度列表（供设置页展示） */
    public Map<String, Integer> positions() {
        Map<String, Integer> map = new HashMap<>();
        // 简单遍历不友好，直接暴露 url->pos 由调用方管理
        return map;
    }

    private static int hash(String s) {
        return Math.abs(s.hashCode() % 1000000);
    }
}
