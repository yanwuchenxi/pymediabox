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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;
    private ApiSourceManager sourceManager;
    private ResumeManager resumeManager;
    private RecyclerView recyclerSources;
    private SourceAdapter adapter;
    private String spiderMethod = "home";
    private PlayerConfig playerConfig;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_settings, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        prefs = getContext().getSharedPreferences("pymediabox", Context.MODE_PRIVATE);
        sourceManager = new ApiSourceManager(getContext());
        resumeManager = new ResumeManager(getContext());
        playerConfig = new PlayerConfig(getContext());

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

        // ===== Python 爬虫调试器 =====
        bindChip(v.findViewById(R.id.chip_home), "home");
        bindChip(v.findViewById(R.id.chip_category), "category");
        bindChip(v.findViewById(R.id.chip_search), "search");
        bindChip(v.findViewById(R.id.chip_detail), "detail");
        bindChip(v.findViewById(R.id.chip_player), "player");
        v.findViewById(R.id.btn_run).setOnClickListener(x -> {
            TextInputEditText etParam = v.findViewById(R.id.et_param);
            String param = etParam.getText().toString().trim();
            String label = spiderLabel(spiderMethod) + (param.isEmpty() ? "" : " " + param);
            ((TextView) v.findViewById(R.id.tv_result))
                    .setText(runSpider(spiderMethod, param));
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

        // ===== 播放器配置（内核 / 缩放 / 超时 / 线程） =====
        bindPlayerConfig(v);
    }

    private void bindPlayerConfig(View v) {
        // 内核 3 选 1
        MaterialButton k0 = v.findViewById(R.id.btn_kernel_0);
        MaterialButton k1 = v.findViewById(R.id.btn_kernel_1);
        MaterialButton k2 = v.findViewById(R.id.btn_kernel_2);
        k0.setOnClickListener(x -> { playerConfig.setKernel(0); refreshKernel(v); });
        k1.setOnClickListener(x -> { playerConfig.setKernel(1); refreshKernel(v); });
        k2.setOnClickListener(x -> { playerConfig.setKernel(2); refreshKernel(v); });
        refreshKernel(v);

        // 画面缩放（ChipGroup 单行多列）
        ChipGroup scaleGroup = v.findViewById(R.id.chip_scale);
        int scaleIdx = playerConfig.scaleIndex();
        int chipId = scaleGroup.getChildCount();
        for (int i = 0; i < chipId; i++) {
            final int idx = i;
            Chip chip = (Chip) scaleGroup.getChildAt(i);
            chip.setChecked(i == scaleIdx);
            chip.setOnCheckedChangeListener((c, on) -> {
                if (on) {
                    playerConfig.setScale(idx);
                    for (int j = 0; j < scaleGroup.getChildCount(); j++)
                        if (j != idx) ((Chip) scaleGroup.getChildAt(j)).setChecked(false);
                }
            });
        }

        // 超时换源（ChipGroup）
        ChipGroup timeoutGroup = v.findViewById(R.id.chip_timeout);
        int[] timeoutVals = {5, 10, 20, 30};
        for (int i = 0; i < timeoutGroup.getChildCount(); i++) {
            final int val = timeoutVals[i];
            Chip chip = (Chip) timeoutGroup.getChildAt(i);
            chip.setChecked(playerConfig.timeoutSec() == val);
            chip.setOnCheckedChangeListener((c, on) -> {
                if (on) {
                    playerConfig.setTimeout(val);
                    for (int j = 0; j < timeoutGroup.getChildCount(); j++)
                        if (j != i) ((Chip) timeoutGroup.getChildAt(j)).setChecked(false);
                }
            });
        }

        // 高级设置：搜索线程
        TextInputEditText etThreads = v.findViewById(R.id.et_threads);
        etThreads.setText(String.valueOf(playerConfig.searchThreads()));
        v.findViewById(R.id.btn_save_config).setOnClickListener(x -> {
            try {
                int t = Integer.parseInt(etThreads.getText().toString().trim());
                playerConfig.setSearchThreads(t);
                Toast.makeText(getContext(), "已保存，线程=" + t, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), "线程数无效", Toast.LENGTH_SHORT).show();
            }
        });

        // 恢复默认
        v.findViewById(R.id.btn_reset_config).setOnClickListener(x -> {
            playerConfig.reset();
            Toast.makeText(getContext(), "已恢复默认", Toast.LENGTH_SHORT).show();
            // 重新绑定
            bindPlayerConfig(x.getRootView());
        });
    }

    private void refreshKernel(View v) {
        int sel = playerConfig.kernelIndex();
        int tint = sel == 0 ? 0xFF4CC9F0 : 0xFF252A3A;
        int unTint = 0xFF252A3A;
        int selText = 0xFF0D0F1A;
        int unText = 0xFFFFFFFF;
        v.findViewById(R.id.btn_kernel_0).setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(sel == 0 ? tint : unTint));
        v.findViewById(R.id.btn_kernel_0).setTextColor(sel == 0 ? selText : unText);
        v.findViewById(R.id.btn_kernel_1).setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(sel == 1 ? tint : unTint));
        v.findViewById(R.id.btn_kernel_1).setTextColor(sel == 1 ? selText : unText);
        v.findViewById(R.id.btn_kernel_2).setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(sel == 2 ? tint : unTint));
        v.findViewById(R.id.btn_kernel_2).setTextColor(sel == 2 ? selText : unText);
    }

    private void bindChip(Chip chip, String m) {
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
        String ts = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        String line = "· " + ts + " " + action + "\n";
        String old = prefs.getString("spider_history", "");
        prefs.edit().putString("spider_history", line + old).apply();
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
