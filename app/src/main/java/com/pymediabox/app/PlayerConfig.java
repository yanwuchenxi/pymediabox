package com.pymediabox.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 播放器设置（借鉴 GSYVideoPlayer 的内核切换与超时换源设计）。
 * - 内核：系统 / ExoPlayer 硬解 / ExoPlayer 软解
 * - 画面缩放：默认 / 16:9 / 4:3 / 填充 / 原始 / 剪裁
 * - 超时换源：5-30s
 * - EPG 地址
 */
public class PlayerConfig {

    public static final String[] KERNELS = {"系统", "EXO硬解", "EXO软解"};
    public static final String[] SCALES = {"默认", "16:9", "4:3", "填充", "原始", "剪裁"};

    private final SharedPreferences prefs;

    public PlayerConfig(Context ctx) {
        prefs = ctx.getSharedPreferences("pymediabox_player", Context.MODE_PRIVATE);
    }

    public int kernelIndex() { return prefs.getInt("kernel", 0); }
    public void setKernel(int i) { prefs.edit().putInt("kernel", i).apply(); }

    public String kernelName() {
        int i = kernelIndex();
        return i >= 0 && i < KERNELS.length ? KERNELS[i] : KERNELS[0];
    }

    public int scaleIndex() { return prefs.getInt("scale", 0); }
    public void setScale(int i) { prefs.edit().putInt("scale", i).apply(); }

    public int timeoutSec() {
        int t = prefs.getInt("timeout", 20);
        // 限制在 5-30
        if (t < 5) t = 5;
        if (t > 30) t = 30;
        return t;
    }
    public void setTimeout(int t) { prefs.edit().putInt("timeout", t).apply(); }

    /** 高级设置：搜索线程 */
    public int searchThreads() {
        int t = prefs.getInt("threads", 8);
        if (t < 1) t = 1;
        if (t > 32) t = 32;
        return t;
    }
    public void setSearchThreads(int t) { prefs.edit().putInt("threads", t).apply(); }

    /** 重置为默认 */
    public void reset() {
        prefs.edit().clear().apply();
    }
}
