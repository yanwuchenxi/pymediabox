package com.pymediabox.app;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        TextView tv = findViewById(R.id.tv_play_title);
        String title = getIntent().getStringExtra("title");
        String url = getIntent().getStringExtra("url");
        tv.setText(title != null ? title : "播放中");

        videoView = findViewById(R.id.vv_player);
        videoView.setMediaController(new MediaController(this));
        try {
            videoView.setVideoURI(Uri.parse(url));
            videoView.setOnPreparedListener(mp -> mp.start());
            videoView.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "播放失败 what=" + what, Toast.LENGTH_SHORT).show();
                return true;
            });
        } catch (Exception e) {
            Toast.makeText(this, "无法解析播放地址: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) videoView.pause();
    }

    @Override
    protected void onDestroy() {
        if (videoView != null) { videoView.stopPlayback(); videoView = null; }
        super.onDestroy();
    }
}
