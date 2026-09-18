package com.pymediabox.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.app_name) + " · 本地影音");

        findViewById(R.id.btn_local).setOnClickListener(v -> scanLocal());
        findViewById(R.id.btn_spider).setOnClickListener(v -> runPythonSpider());
        findViewById(R.id.btn_url).setOnClickListener(v -> {
            Intent i = new Intent(this, PlayerActivity.class);
            i.putExtra("url", "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4");
            i.putExtra("title", "在线示例（Big Buck Bunny）");
            startActivity(i);
        });

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getDataString() != null) {
            Intent i = new Intent(this, PlayerActivity.class);
            i.putExtra("url", intent.getDataString());
            i.putExtra("title", "来自分享");
            startActivity(i);
        }
    }

    private void scanLocal() {
        int need = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO);
        if (need != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO}, 1001);
            return;
        }
        doScanLocal();
    }

    private void doScanLocal() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File[] files = (dir != null) ? dir.listFiles() : null;
        if (files == null || files.length == 0) {
            Toast.makeText(this,
                    "暂无本地文件。请把视频放到：/sdcard/Android/data/" + getPackageName() + "/files/Movies",
                    Toast.LENGTH_LONG).show();
        } else {
            Intent i = new Intent(this, PlayerActivity.class);
            i.putExtra("url", files[0].getAbsolutePath());
            i.putExtra("title", files[0].getName());
            startActivity(i);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            for (int r : grantResults) if (r == PackageManager.PERMISSION_GRANTED) { doScanLocal(); return; }
            Toast.makeText(this, "未授予媒体读取权限", Toast.LENGTH_SHORT).show();
        }
    }

    private void runPythonSpider() {
        Python py = Python.getInstance();
        try {
            // src/main/python/spider.py 会作为模块 spider 打进 APK，可直接 import
            PyObject createSpider = py.getModule("spider").callAttr("create_spider");
            PyObject spider = createSpider.call();
            PyObject result = spider.callAttr("home_content");
            String s = result != null ? result.toString() : "";
            PySpiderCache.last = s;
            Toast.makeText(this, "Python 爬虫响应长度=" + s.length(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Python 爬虫异常: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
