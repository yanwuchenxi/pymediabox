package com.pymediabox.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SpiderFragment extends Fragment {

    private TextView tvResult, tvHistory;
    private SharedPreferences prefs;
    private ChipGroup chips;
    private String method = "home";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_spider, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        prefs = getContext().getSharedPreferences("pymediabox", Context.MODE_PRIVATE);

        TextInputEditText etUrl = v.findViewById(R.id.et_url);
        etUrl.setText(prefs.getString("default_url",
                "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4"));

        tvResult = v.findViewById(R.id.tv_result);
        tvHistory = v.findViewById(R.id.tv_history);

        chips = v.findViewById(R.id.chip_methods);
        bindChip(v.findViewById(R.id.chip_home), "home");
        bindChip(v.findViewById(R.id.chip_category), "category");
        bindChip(v.findViewById(R.id.chip_search), "search");
        bindChip(v.findViewById(R.id.chip_detail), "detail");
        bindChip(v.findViewById(R.id.chip_player), "player");

        MaterialButton btnRun = v.findViewById(R.id.btn_run);
        btnRun.setOnClickListener(x -> {
            TextInputEditText etParam = v.findViewById(R.id.et_param);
            String param = etParam.getText().toString().trim();
            String label = labelOf(method);
            showResult(runSpider(method, param), label + " " + param);
        });

        MaterialButton btnPlay = v.findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(x -> {
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty()) return;
            Intent i = new Intent(getContext(), PlayerActivity.class);
            i.putExtra("url", url);
            i.putExtra("title", "Python 爬虫解析");
            startActivity(i);
        });

        loadHistory();
    }

    private void bindChip(Chip chip, String m) {
        chip.setOnCheckedChangeListener((c, checked) -> { if (checked) method = m; });
    }

    private String labelOf(String m) {
        switch (m) {
            case "category": return "分类";
            case "search": return "搜索";
            case "detail": return "详情";
            case "player": return "播放器";
            default: return "首页";
        }
    }

    private void loadHistory() {
        String h = prefs.getString("spider_history", "");
        tvHistory.setText(h.isEmpty() ? "暂无操作记录" : h);
    }

    private void addHistory(String action, String result) {
        String ts = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        String line = "· " + ts + " " + action + "\n";
        String old = prefs.getString("spider_history", "");
        prefs.edit().putString("spider_history", line + old).apply();
        loadHistory();
    }

    private void showResult(String json, String action) {
        tvResult.setText(json);
        addHistory(action, json);
    }

    private String runSpider(String m, String param) {
        try {
            Python py = Python.getInstance();
            PyObject spider = py.getModule("spider").callAttr("create_spider").call();
            PyObject result;
            switch (m) {
                case "category":
                    result = spider.callAttr("category_content",
                            new String[]{param.isEmpty() ? "1" : param, "1"});
                    break;
                case "search":
                    result = spider.callAttr("search_content",
                            new String[]{param.isEmpty() ? "测试" : param});
                    break;
                case "detail":
                    result = spider.callAttr("detail_content",
                            new String[]{param.isEmpty() ? "1" : param});
                    break;
                case "player":
                    result = spider.callAttr("player_content",
                            new String[]{"", param.isEmpty() ? "1" : param});
                    break;
                default:
                    result = spider.callAttr("home_content");
            }
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            return "Python 异常: " + e.getMessage();
        }
    }
}
