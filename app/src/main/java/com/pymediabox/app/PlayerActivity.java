package com.pymediabox.app;

import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

public class PlayerActivity extends AppCompatActivity {

    private ExoPlayer exoPlayer;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        TextView tv = findViewById(R.id.tv_play_title);
        String title = getIntent().getStringExtra("title");
        String url = getIntent().getStringExtra("url");
        tv.setText(title != null ? title : "播放中");

        playerView = findViewById(R.id.player_view);

        exoPlayer = ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);

        try {
            MediaItem item = MediaItem.fromUri(Uri.parse(url));
            exoPlayer.setMediaItem(item);
            exoPlayer.prepare();
            exoPlayer.play();
        } catch (Exception e) {
            Toast.makeText(this, "无法解析播放地址: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) exoPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        if (exoPlayer != null) { exoPlayer.release(); exoPlayer = null; }
        super.onDestroy();
    }
}
