package com.pymediabox.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SpiderFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_spider, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        TextInputEditText etUrl = v.findViewById(R.id.et_url);
        etUrl.setText("https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4");

        TextView tvResult = v.findViewById(R.id.tv_result);
        MaterialButton btnPlay = v.findViewById(R.id.btn_play);
        MaterialButton btnHome = v.findViewById(R.id.btn_run_home);
        MaterialButton btnDetail = v.findViewById(R.id.btn_run_detail);

        // Spinner：爬虫分类
        Spinner spinner = v.findViewById(R.id.spinner_category);
        spinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"电影", "剧集", "综艺"}));

        btnHome.setOnClickListener(x -> tvResult.setText(runSpider("home")));
        btnDetail.setOnClickListener(x -> {
            String sel = String.valueOf(spinner.getSelectedItem());
            tvResult.setText(runSpider("detail", sel));
        });
        btnPlay.setOnClickListener(x -> {
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty()) return;
            Intent i = new Intent(getContext(), PlayerActivity.class);
            i.putExtra("url", url);
            i.putExtra("title", "Python 爬虫解析");
            startActivity(i);
        });
    }

    private String runSpider(String method, Object... args) {
        try {
            Python py = Python.getInstance();
            PyObject spider = py.getModule("spider").callAttr("create_spider").call();
            PyObject result;
            switch (method) {
                case "detail":
                    result = spider.callAttr("detail_content", args[0].toString());
                    break;
                case "home":
                    result = spider.callAttr("home_content");
                    break;
                default:
                    result = spider.callAttr("search_content", args[0].toString());
            }
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            return "Python 异常: " + e.getMessage();
        }
    }
}
