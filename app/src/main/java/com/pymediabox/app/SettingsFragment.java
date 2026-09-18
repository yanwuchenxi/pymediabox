package com.pymediabox.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_settings, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        prefs = getContext().getSharedPreferences("pymediabox", Context.MODE_PRIVATE);

        // 默认播放源
        TextInputEditText etUrl = v.findViewById(R.id.et_default_url);
        etUrl.setText(prefs.getString("default_url",
                "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4"));
        MaterialButton btnPlay = v.findViewById(R.id.btn_play_default);
        btnPlay.setOnClickListener(x -> {
            String url = etUrl.getText().toString().trim();
            if (!url.isEmpty()) prefs.edit().putString("default_url", url).apply();
            HomeFragment.openUrl(getContext(), url, "默认源");
        });

        // 保持屏幕常亮
        Switch sw = v.findViewById(R.id.switch_keep_screen);
        sw.setChecked(prefs.getBoolean("keep_screen", false));
        sw.setOnCheckedChangeListener((b, checked) -> {
            prefs.edit().putBoolean("keep_screen", checked).apply();
            if (getActivity() == null) return;
            if (checked)
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        });
        if (sw.isChecked())
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 本地扫描
        v.findViewById(R.id.btn_scan_local).setOnClickListener(x ->
                Toast.makeText(getContext(), "本地扫描将在「首页」标签页显示", Toast.LENGTH_SHORT).show());

        // 清除历史/收藏
        v.findViewById(R.id.btn_clear_history).setOnClickListener(x -> {
            SharedPreferences h = getContext().getSharedPreferences("pymediabox_history", Context.MODE_PRIVATE);
            h.edit().clear().apply();
            Toast.makeText(getContext(), "播放历史与收藏已清除", Toast.LENGTH_SHORT).show();
        });
    }
}
