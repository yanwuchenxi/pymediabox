package com.pymediabox.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;
    private ApiSourceManager sourceManager;
    private ResumeManager resumeManager;
    private RecyclerView recyclerSources;
    private SourceAdapter adapter;
    private String spiderMethod = "home";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_settings, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        prefs = getContext().getSharedPreferences("pymediabox", Context.MODE_PRIVATE);
        sourceManager = new ApiSourceManager(getContext());
        resumeManager = new ResumeManager(getContext());

        // 版本
        TextView tvVer = v.findViewById(R.id.tv_settings_version);
        try {
            tvVer.setText("版本 " + getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0).versionName);
        } catch (Exception ignored) { }

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
        Switch swKeep = v.findViewById(R.id.switch_keep_screen);
        swKeep.setChecked(prefs.getBoolean("keep_screen", false));
        swKeep.setOnCheckedChangeListener((b, checked) -> {
            prefs.edit().putBoolean("keep_screen", checked).apply();
            if (getActivity() == null) return;
            if (checked)
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        });
        if (swKeep.isChecked())
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 断点续播开关
        Switch swResume = v.findViewById(R.id.switch_resume);
        swResume.setChecked(prefs.getBoolean("resume_enabled", true));
        swResume.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean("resume_enabled", checked).apply());

        // API 源添加
        TextInputEditText etSrc = v.findViewById(R.id.et_new_source_url);
        v.findViewById(R.id.btn_add_source).setOnClickListener(x -> {
            String u = etSrc.getText().toString().trim();
            if (u.isEmpty()) {
                Toast.makeText(getContext(), "请输入接口 URL", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sourceManager.addSource("API 源", u)) {
                Toast.makeText(getContext(), "已添加 API 源", Toast.LENGTH_SHORT).show();
                etSrc.setText("");
                refreshSources();
            } else {
                Toast.makeText(getContext(), "URL 无效", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerSources = v.findViewById(R.id.recycler_sources);
        recyclerSources.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshSources();

        // ===== Python 爬虫调试器（原爬虫页合并） =====
        com.google.android.material.chip.ChipGroup chips = v.findViewById(R.id.chip_methods);
        bindChip(v.findViewById(R.id.chip_home), "home");
        bindChip(v.findViewById(R.id.chip_category), "category");
        bindChip(v.findViewById(R.id.chip_search), "search");
        bindChip(v.findViewById(R.id.chip_detail), "detail");
        bindChip(v.findViewById(R.id.chip_player), "player");
        v.findViewById(R.id.btn_run).setOnClickListener(x -> {
            com.google.android.material.textfield.TextInputEditText etParam =
                    v.findViewById(R.id.et_param);
            String param = etParam.getText().toString().trim();
            String label = spiderLabel(spiderMethod) + (param.isEmpty() ? "" : " " + param);
            v.findViewById(R.id.tv_result).setText(runSpider(spiderMethod, param));
            addSpiderHistory(label);
        });

        // 本地扫描
        v.findViewById(R.id.btn_scan_local).setOnClickListener(x ->
                Toast.makeText(getContext(), "请在「首页」点击 📂 本地 进行扫描",
                        Toast.LENGTH_SHORT).show());

        // 清除历史/收藏/断点
        v.findViewById(R.id.btn_clear_history).setOnClickListener(x -> {
            new HistoryManager(getContext()).clearAll();
            resumeManager.clearAll();
            Toast.makeText(getContext(), "播放历史、收藏与断点已清除",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void bindChip(com.google.android.material.chip.Chip chip, String m) {
        chip.setOnCheckedChangeListener((c, checked) -> { if (checked) spiderMethod = m; });
    }

    private String spiderLabel(String m) {
        switch (m) {
            case "category": return "分类";
            case "search": return "搜索";
            case "detail": return "详情";
            case "player": return "播放器";
            default: return "首页";
        }
    }

    private void addSpiderHistory(String action) {
        String ts = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
                .format(new java.util.Date());
        String line = "· " + ts + " " + action + "\n";
        String old = prefs.getString("spider_history", "");
        prefs.edit().putString("spider_history", line + old).apply();
    }

    private String runSpider(String m, String param) {
        try {
            com.chaquo.python.Python py = com.chaquo.python.Python.getInstance();
            com.chaquo.python.PyObject spider =
                    py.getModule("spider").callAttr("create_spider").call();
            com.chaquo.python.PyObject result;
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

    private void refreshSources() {
        List<ApiSourceManager.Source> items = sourceManager.sources();
        if (adapter == null) {
            adapter = new SourceAdapter(items);
            recyclerSources.setAdapter(adapter);
        } else {
            adapter.bind(items);
        }
    }

    class SourceAdapter extends RecyclerView.Adapter<SourceAdapter.VH> {
        private List<ApiSourceManager.Source> data;
        SourceAdapter(List<ApiSourceManager.Source> d) { this.data = d; }
        void bind(List<ApiSourceManager.Source> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_source, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            ApiSourceManager.Source s = data.get(pos);
            h.name.setText(s.name);
            h.url.setText(s.url);
            boolean builtin = ApiSourceManager.BUILTIN_KEY.equals(s.key);
            h.badge.setText(builtin ? "内置" : "自定义");
            h.del.setVisibility(builtin ? View.GONE : View.VISIBLE);
            h.del.setOnClickListener(b -> {
                sourceManager.removeSource(s.key);
                refreshSources();
                Toast.makeText(getContext(), "已删除", Toast.LENGTH_SHORT).show();
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView name, url, badge;
            MaterialButton del;
            VH(View v) {
                super(v);
                name = v.findViewById(R.id.tv_source_name);
                url = v.findViewById(R.id.tv_source_url);
                badge = v.findViewById(R.id.tv_source_badge);
                del = v.findViewById(R.id.btn_source_delete);
            }
        }
    }
}
