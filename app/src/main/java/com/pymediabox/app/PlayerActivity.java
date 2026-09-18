package com.pymediabox.app;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private TextView tvTitle, tvProgress;
    private LinearLayout bottomBar;
    private MaterialButton btnPlayPause;
    private SeekBar progress;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = false;
    private final SimpleDateFormat fmt = new SimpleDateFormat("mm:ss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        String url = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");

        tvTitle = new TextView(this);
        tvTitle.setText(title != null ? title : "播放中");
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(14);
        tvTitle.setPadding(16, 12, 16, 12);
        tvTitle.setBackgroundColor(0xCC0D0F1A);

        progress = new SeekBar(this);
        progress.setMax(1000);

        btnPlayPause = new MaterialButton(this);
        btnPlayPause.setText("暂停");
        btnPlayPause.setAllCaps(false);

        tvProgress = new TextView(this);
        tvProgress.setText("00:00 / 00:00");
        tvProgress.setTextColor(Color.WHITE);
        tvProgress.setTextSize(12);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(16, 0, 16, 0);
        row.addView(tvProgress, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(btnPlayPause, new LinearLayout.LayoutParams(-2, -2));

        bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.VERTICAL);
        bottomBar.setPadding(16, 12, 16, 16);
        bottomBar.setBackgroundColor(0x330D0F1A);
        bottomBar.addView(progress, new LinearLayout.LayoutParams(-1, -2));
        bottomBar.addView(row);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.addView(tvTitle, new LinearLayout.LayoutParams(-1, -2));

        // VideoView 自带 surface，放在中间
        videoView = new VideoView(this);
        root.addView(videoView, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(bottomBar, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);

        btnPlayPause.setOnClickListener(v -> togglePlay());
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int p, boolean user) {
                if (user && videoView != null && videoView.getDuration() > 0) {
                    int ms = p * videoView.getDuration() / 1000;
                    videoView.seekTo(ms);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });

        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "缺少播放地址", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {
            videoView.setVideoURI(Uri.parse(url));
            videoView.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                btnPlayPause.setText("暂停");
                startProgress();
            });
            videoView.setOnCompletionListener(mp -> {
                btnPlayPause.setText("重播");
                isPlaying = false;
            });
            videoView.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "播放失败 what=" + what, Toast.LENGTH_SHORT).show();
                return true;
            });
        } catch (Exception e) {
            Toast.makeText(this, "无法解析播放地址: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void togglePlay() {
        if (videoView == null) return;
        if (isPlaying) {
            videoView.pause();
            isPlaying = false;
            btnPlayPause.setText("继续");
        } else {
            videoView.start();
            isPlaying = true;
            btnPlayPause.setText("暂停");
            startProgress();
        }
    }

    private void startProgress() {
        progress.removeCallbacks(progressTask);
        handler.post(progressTask);
    }

    private final Runnable progressTask = new Runnable() {
        @Override public void run() {
            if (videoView != null && videoView.getDuration() > 0) {
                int pos = videoView.getCurrentPosition();
                progress.setProgress(pos * 1000 / videoView.getDuration());
                tvProgress.setText(fmt.format(pos) + " / " + fmt.format(videoView.getDuration()));
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override protected void onPause() {
        super.onPause();
        handler.removeCallbacks(progressTask);
    }

    @Override protected void onDestroy() {
        if (videoView != null) videoView.stopPlayback();
        super.onDestroy();
    }
}
