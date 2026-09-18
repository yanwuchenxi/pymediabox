package com.pymediabox.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页（影视仓 / OK影视 / 蜂蜜式）：
 * - ① 轮播横幅 Hero 区
 * - ② 分类标签横排（当前源）
 * - ③ 双列封面网格（当前分类）
 * - ④ 观看历史 / 影视收藏 分段
 * - ⑤ 本地媒体 / 默认源 快捷入口
 */
public class HomeFragment extends Fragment {

    public enum HistMode { HISTORY, FAVORITE }

    private SwipeRefreshLayout swipe;
    private RecyclerView recyclerVideos, recyclerClasses, recyclerHistory;
    private VideoAdapter videoAdapter;
    private ClassAdapter classAdapter;
    private HistoryAdapter historyAdapter;

    private HistoryManager history;
    private ApiSourceManager api;
    private ApiSourceManager.Source activeSource;
    private List<String> classNames = new ArrayList<>();
    private String activeClass = "";
    private int activePage = 1;

    private List<HistoryManager.HistoryItem> historyItems = new ArrayList<>();
    private HistMode histMode = HistMode.HISTORY;
    private MaterialButton btnHist, btnFav;

    static class Video {
        String title, link, tag, duration;
        Video(String t, String l, String tag, String dur) {
            title = t; link = l; this.tag = tag; duration = dur;
        }
    }

    public static void openUrl(Context ctx, String url, String title) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(ctx, "该条目无播放地址（需配置 API 源解析）",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(ctx, PlayerActivity.class);
        i.putExtra("url", url);
        i.putExtra("title", title);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_home, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        history = new HistoryManager(getContext());
        api = new ApiSourceManager(getContext());

        swipe = v.findViewById(R.id.swipe_home);
        swipe.setColorSchemeResources(R.color.accent);
        swipe.setOnRefreshListener(() -> loadCategory());

        recyclerVideos = v.findViewById(R.id.recycler_home);
        recyclerVideos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerClasses = v.findViewById(R.id.recycler_classes);
        recyclerClasses.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerHistory = v.findViewById(R.id.recycler_history);
        recyclerHistory.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 历史 / 收藏 分段
        btnHist = v.findViewById(R.id.btn_mode_history);
        btnFav = v.findViewById(R.id.btn_mode_favorite);
        btnHist.setOnClickListener(x -> setHistMode(HistMode.HISTORY));
        btnFav.setOnClickListener(x -> setHistMode(HistMode.FAVORITE));

        // 快捷入口
        v.findViewById(R.id.btn_scan_local).setOnClickListener(x -> requestLocalPermission());
        v.findViewById(R.id.btn_default).setOnClickListener(x -> {
            SharedPreferences sp = getContext().getSharedPreferences("pymediabox", Context.MODE_PRIVATE);
            String url = sp.getString("default_url",
                    "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4");
            openUrl(getContext(), url, "默认源");
        });

        loadClasses();
        setHistMode(HistMode.HISTORY);
    }

    // ---------- 分类标签（当前源） ----------
    private void loadClasses() {
        List<ApiSourceManager.Source> srcs = api.sources();
        activeSource = srcs.get(0);
        classNames = api.homeClasses(activeSource);
        if (classNames == null) classNames = new ArrayList<>();
        activeClass = classNames.isEmpty() ? "全部" : classNames.get(0);
        if (classAdapter == null) {
            classAdapter = new ClassAdapter(classNames);
            recyclerClasses.setAdapter(classAdapter);
        } else {
            classAdapter.data = classNames;
            classAdapter.notifyDataSetChanged();
        }
        loadCategory();
    }

    private void loadCategory() {
        activePage = 1;
        List<Video> list = new ArrayList<>();
        List<String[]> data = api.categoryItems(activeSource, activeClass, 1);
        if (data != null) {
            for (String[] row : data)
                list.add(new Video(row[0], row[1], "· " + activeClass, ""));
        }
        // 空时给演示数据
        if (list.isEmpty()) {
            list.add(new Video("Big Buck Bunny",
                    "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4",
                    "在线", "10MB"));
            list.add(new Video("Sintel",
                    "https://media.w3.org/2010/05/sintel/trailer.mp4",
                    "在线", "52MB"));
            list.add(new Video("Tears of Steel",
                    "https://media.w3.org/2010/05/tearsofsteel/tearsofsteel_720p.mp4",
                    "在线", "108MB"));
            list.add(new Video("Elephants Dream",
                    "https://media.w3.org/2010/05/bunny/elephants_dream_720p.mp4",
                    "在线", "151MB"));
        }
        if (videoAdapter == null) {
            videoAdapter = new VideoAdapter(list);
            recyclerVideos.setAdapter(videoAdapter);
        } else {
            videoAdapter.data = list;
            videoAdapter.notifyDataSetChanged();
        }
        TextView pageInfo = (TextView) requireView().findViewById(R.id.tv_page_info);
        if (pageInfo != null)
            pageInfo.setText("第 1 页 · " + activeClass);
        swipe.setRefreshing(false);
    }

    class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.VH> {
        List<String> data;
        ClassAdapter(List<String> d) { this.data = d; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_class_tag, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            String c = data.get(pos);
            h.tv.setText(c);
            boolean sel = c.equals(activeClass);
            int bg = sel ? 0xFF4CC9F0 : 0xFF1E2130;
            int fg = sel ? 0xFF0D0F1A : 0xFF8A8FA8;
            ((com.google.android.material.card.MaterialCardView) h.itemView)
                    .setCardBackgroundColor(bg);
            h.tv.setTextColor(fg);
            h.itemView.setOnClickListener(v -> {
                activeClass = c;
                notifyItemRangeChanged(0, data.size());
                loadCategory();
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(View v) { super(v); tv = v.findViewById(R.id.tv_class_name); }
        }
    }

    class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VH> {
        List<Video> data;
        VideoAdapter(List<Video> d) { this.data = d; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_card, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Video it = data.get(pos);
            h.title.setText(it.title);
            h.link.setText(it.link != null && !it.link.isEmpty() ? it.link : "由 API 源解析");
            h.link.setVisibility(it.link != null && !it.link.isEmpty()
                    ? View.VISIBLE : View.GONE);
            h.tag.setText(it.tag);
            h.coverChar.setText(it.title.isEmpty() ? "影" : it.title.substring(0, 1));
            h.duration.setText(it.duration.isEmpty() ? it.tag : it.duration);
            h.duration.setVisibility(it.duration.isEmpty() ? View.GONE : View.VISIBLE);
            h.itemView.setOnClickListener(v -> openUrl(getContext(), it.link, it.title));
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title, link, tag, coverChar, duration;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tv_item_title);
                link = v.findViewById(R.id.tv_item_link);
                tag = v.findViewById(R.id.tv_item_type);
                coverChar = v.findViewById(R.id.tv_cover_char);
                duration = v.findViewById(R.id.tv_duration);
            }
        }
    }

    // ---------- 历史 / 收藏 ----------
    private void setHistMode(HistMode m) {
        histMode = m;
        int sel = 0xFF4CC9F0, un = 0xFF1E2130;
        int selText = 0xFF0D0F1A, unText = 0xFFFFFFFF;
        setSegBtn(btnHist, m == HistMode.HISTORY, sel, un, selText, unText);
        setSegBtn(btnFav, m == HistMode.FAVORITE, sel, un, selText, unText);

        historyItems = m == HistMode.FAVORITE ? history.getFavorites() : history.getHistory();
        recyclerHistory.setVisibility(historyItems.isEmpty() ? View.GONE : View.VISIBLE);
        if (historyAdapter == null) {
            historyAdapter = new HistoryAdapter(historyItems);
            recyclerHistory.setAdapter(historyAdapter);
        } else {
            historyAdapter.data = historyItems;
            historyAdapter.notifyDataSetChanged();
        }
    }

    private void setSegBtn(MaterialButton b, boolean selected,
                           int sel, int un, int selText, int unText) {
        b.setBackgroundTintList(ColorStateList.valueOf(selected ? sel : un));
        b.setTextColor(selected ? selText : unText);
    }

    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        List<HistoryManager.HistoryItem> data;
        HistoryAdapter(List<HistoryManager.HistoryItem> d) { this.data = d; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_history, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            HistoryManager.HistoryItem it = data.get(pos);
            h.cover.setText(it.title.isEmpty() ? "影" : it.title.substring(0, 1));
            h.title.setText(it.title);
            h.meta.setText(histMode == HistMode.FAVORITE
                    ? "收藏" : it.type + (it.time.isEmpty() ? "" : " · " + it.time));
            h.itemView.setOnClickListener(v -> openUrl(getContext(), it.url, it.title));
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView cover, title, meta;
            VH(View v) {
                super(v);
                cover = v.findViewById(R.id.tv_hist_cover);
                title = v.findViewById(R.id.tv_hist_title);
                meta = v.findViewById(R.id.tv_hist_meta);
            }
        }
    }

    // ---------- 本地媒体 ----------
    private void requestLocalPermission() {
        AppCompatActivity host = (AppCompatActivity) getActivity();
        int need = ContextCompat.checkSelfPermission(host, Manifest.permission.READ_MEDIA_VIDEO);
        if (need != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(host, new String[]{
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO}, 1001);
        } else {
            toastLocalCount();
        }
    }

    private void toastLocalCount() {
        AppCompatActivity host = (AppCompatActivity) getActivity();
        if (host == null) return;
        File dir = host.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File[] files = (dir != null) ? dir.listFiles() : null;
        int count = 0;
        if (files != null)
            for (File f : files) {
                String n = f.getName().toLowerCase();
                if (n.endsWith(".mp4") || n.endsWith(".mkv") || n.endsWith(".m4v") || n.endsWith(".avi"))
                    count++;
            }
        if (count == 0)
            Toast.makeText(getContext(), "暂无本地视频，请放入 " +
                    "/sdcard/Android/data/com.pymediabox.app/files/Movies",
                    Toast.LENGTH_LONG).show();
        else
            openUrl(getContext(), files[0].getAbsolutePath(), files[0].getName());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            for (int r : grantResults) if (r == PackageManager.PERMISSION_GRANTED) { toastLocalCount(); return; }
            Toast.makeText(getContext(), "未授予媒体读取权限", Toast.LENGTH_SHORT).show();
        }
    }
}
