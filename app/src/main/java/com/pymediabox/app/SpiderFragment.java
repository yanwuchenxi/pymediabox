package com.pymediabox.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SpiderFragment extends Fragment {

    private TextView tvResult, tvHistory;
    private SharedPreferences prefs;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_spider, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        prefs = getContext().getSharedPreferences("pymediabox", Context.MODE_PRIVATE);

        TextInputEditText etUrl = v.findViewById(R.id.et_url);
        etUrl.setText(prefs.getString("default_url",
                "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4"));

        TextInputEditText etSearch = v.findViewById(R.id.et_search_spider);
        tvResult = v.findViewById(R.id.tv_result);
        tvHistory = v.findViewById(R.id.tv_history);

        MaterialButton btnPlay = v.findViewById(R.id.btn_play);
        MaterialButton btnHome = v.findViewById(R.id.btn_run_home);
        MaterialButton btnDetail = v.findViewById(R.id.btn_run_detail);
        MaterialButton btnSearch = v.findViewById(R.id.btn_search_spider);

        // Spinner：爬虫分类
        Spinner spinner = v.findViewById(R.id.spinner_category);
        spinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"电影", "剧集", "综艺", "动画"}));

        btnHome.setOnClickListener(x -> showResult(runSpider("home", null, null)));
        btnDetail.setOnClickListener(x -> {
            String sel = String.valueOf(spinner.getSelectedItem());
            showResult(runSpider("detail", sel, null));
        });
        btnSearch.setOnClickListener(x -> {
            String q = etSearch.getText().toString().trim();
            if (q.isEmpty()) {
                Toast.makeText(getContext(), "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                return;
            }
            showResult(runSpider("search", q, null));
        });
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

    private void loadHistory() {
        String h = prefs.getString("spider_history", "");
        tvHistory.setText(h.isEmpty() ? "暂无操作记录" : h);
    }

    private void addHistory(String action, String result) {
        String ts = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        String line = "· " + ts + " " + action + " → " + (result.length() > 40 ? result.substring(0, 40) + "…" : result) + "\n";
        String old = prefs.getString("spider_history", "");
        prefs.edit().putString("spider_history", line + old).apply();
        loadHistory();
    }

    private void showResult(String json) {
        tvResult.setText(json);
        addHistory("查询", json);
    }

    private String runSpider(String method, String arg1, String arg2) {
        try {
            Python py = Python.getInstance();
            PyObject spider = py.getModule("spider").callAttr("create_spider").call();
            PyObject result;
            switch (method) {
                case "detail":
                    result = spider.callAttr("detail_content", arg1 != null ? arg1 : "");
                    break;
                case "search":
                    result = spider.callAttr("search_content", arg1 != null ? arg1 : "");
                    break;
                case "home":
                    result = spider.callAttr("home_content");
                    break;
                default:
                    result = spider.callAttr("category_content", arg1 != null ? arg1 : "1");
            }
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            return "Python 异常: " + e.getMessage();
        }
    }
}
