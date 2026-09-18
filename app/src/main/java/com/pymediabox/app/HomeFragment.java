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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页：搜索 + 快捷入口 + 分段筛选（推荐 / 历史 / 收藏）
 * 原队列页功能已合并到这里。
 */
public class HomeFragment extends Fragment {

    public enum ViewMode { RECOMMEND, HISTORY, FAVORITE }

    private RecyclerView recycler;
    private SwipeRefreshLayout swipe;
    private Adapter adapter;
    private List<Item> items = new ArrayList<>();
    private ViewMode mode = ViewMode.RECOMMEND;
    private HistoryManager history;

    private MaterialButton btnModeRecommend, btnModeHistory, btnModeFav;
    private TextView tvEmpty;
    private ChannelManager channelManager;
    private PlaybackInfoManager infoManager;
    private RecyclerView recyclerChannels;
    private ChannelAdapter channelAdapter;

    static class Item {
        String title, link, type;
        Item(String t, String l, String ty) { title=t; link=l; type=ty; }
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
        channelManager = new ChannelManager(getContext());
        infoManager = new PlaybackInfoManager(getContext());

        recycler = v.findViewById(R.id.recycler_home);
        swipe = v.findViewById(R.id.swipe_home);
        swipe.setColorSchemeResources(R.color.accent);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        tvEmpty = v.findViewById(R.id.tv_empty);

        // 搜索
        v.findViewById(R.id.btn_search).setOnClickListener(x -> {
            android.widget.EditText et = v.findViewById(R.id.et_search);
            String q = et.getText().toString().trim();
            if (q.isEmpty()) return;
            openUrl(getContext(), "https://example.com?q=" + q, "搜索：" + q);
        });

        // 快捷入口
        v.findViewById(R.id.btn_scan_local).setOnClickListener(x -> requestLocalPermission());
        v.findViewById(R.id.btn_default).setOnClickListener(x -> {
            SharedPreferences sp = getContext().getSharedPreferences("pymediabox", Context.MODE_PRIVATE);
            String url = sp.getString("default_url",
                    "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4");
            openUrl(getContext(), url, "默认源");
        });

        // 分段模式
        btnModeRecommend = v.findViewById(R.id.btn_mode_recommend);
        btnModeHistory = v.findViewById(R.id.btn_mode_history);
        btnModeFav = v.findViewById(R.id.btn_mode_favorite);
        btnModeRecommend.setOnClickListener(x -> setMode(ViewMode.RECOMMEND));
        btnModeHistory.setOnClickListener(x -> setMode(ViewMode.HISTORY));
        btnModeFav.setOnClickListener(x -> setMode(ViewMode.FAVORITE));

        swipe.setOnRefreshListener(() -> refresh());

        // 信息卡：展示当前播放
        bindInfoCard();
        // 收藏频道横向列表
        recyclerChannels = v.findViewById(R.id.recycler_channels);
        recyclerChannels.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(
                        getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        refreshChannels();
        v.findViewById(R.id.btn_fav_channels).setOnClickListener(x ->
                android.widget.Toast.makeText(getContext(), "已收藏 " +
                        channelManager.favorites().size() + " 个频道",
                        android.widget.Toast.LENGTH_SHORT).show());

        setMode(ViewMode.RECOMMEND);
    }

    private void bindInfoCard() {
        String name = infoManager.nowName().isEmpty()
                ? "频道名称" : infoManager.nowName();
        String preview = infoManager.nowPreview();
        ((TextView) requireView().findViewById(R.id.tv_info_name)).setText(name);
        ((TextView) requireView().findViewById(R.id.tv_info_preview)).setText(preview);
        ((TextView) requireView().findViewById(R.id.tv_info_ep)).setText(
                String.valueOf(infoManager.nowEpisode()));
        String last = infoManager.lastName();
        ((TextView) requireView().findViewById(R.id.tv_info_meta)).setText(
                last.isEmpty() ? "收藏频道" : "上次看到 " + last);
    }

    private void refreshChannels() {
        List<ChannelManager.Channel> list = channelManager.all();
        if (channelAdapter == null) {
            channelAdapter = new ChannelAdapter(list);
            recyclerChannels.setAdapter(channelAdapter);
        } else {
            channelAdapter.data = list;
            channelAdapter.notifyDataSetChanged();
        }
    }

    class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.VH> {
        List<ChannelManager.Channel> data;
        ChannelAdapter(List<ChannelManager.Channel> d) { this.data = d; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_channel, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            ChannelManager.Channel c = data.get(pos);
            h.thumb.setText(c.name.isEmpty() ? "频" : c.name.substring(0, 1));
            h.name.setText(c.name);
            h.group.setText(c.group.isEmpty() ? c.url : c.group);
            h.itemView.setOnClickListener(v ->
                    openUrl(getContext(), c.url, c.name));
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView thumb, name, group;
            VH(View v) {
                super(v);
                thumb = v.findViewById(R.id.tv_channel_thumb);
                name = v.findViewById(R.id.tv_channel_name);
                group = v.findViewById(R.id.tv_channel_group);
            }
        }
    }

    private void setMode(ViewMode m) {
        mode = m;
        int sel = 0xFF4CC9F0, un = 0xFF1E2130;
        int selText = 0xFF0D0F1A, unText = 0xFFFFFFFF;
        setSegBtn(btnModeRecommend, m == ViewMode.RECOMMEND, sel, un, selText, unText);
        setSegBtn(btnModeHistory, m == ViewMode.HISTORY, sel, un, selText, unText);
        setSegBtn(btnModeFav, m == ViewMode.FAVORITE, sel, un, selText, unText);
        refresh();
    }

    private void setSegBtn(MaterialButton b, boolean selected,
                           int sel, int un, int selText, int unText) {
        b.setBackgroundTintList(ColorStateList.valueOf(selected ? sel : un));
        b.setTextColor(selected ? selText : unText);
    }

    private void refresh() {
        swipe.setRefreshing(true);
        items.clear();
        switch (mode) {
            case HISTORY:
                for (HistoryManager.HistoryItem it : history.getHistory())
                    items.add(new Item(it.title, it.url, it.type + " · " + it.time));
                break;
            case FAVORITE:
                for (HistoryManager.HistoryItem it : history.getFavorites())
                    items.add(new Item(it.title, it.url, "收藏"));
                break;
            default:
                addRecommend(items);
        }
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        switch (mode) {
            case HISTORY: tvEmpty.setText("暂无播放历史，播放视频后自动记录"); break;
            case FAVORITE: tvEmpty.setText("暂无收藏，在播放页点 ☆ 收藏"); break;
            default: tvEmpty.setText("暂无视频，点击「本地」扫描媒体文件");
        }
        if (adapter == null) {
            adapter = new Adapter(items);
            recycler.setAdapter(adapter);
        } else {
            adapter.data = items;
            adapter.notifyDataSetChanged();
        }
        swipe.setRefreshing(false);
    }

    private void addRecommend(List<Item> out) {
        out.add(new Item("在线示例 · Big Buck Bunny",
                "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4", "在线"));
        out.add(new Item("在线示例 · Sintel",
                "https://media.w3.org/2010/05/sintel/trailer.mp4", "在线"));
        // API 源分类
        ApiSourceManager am = new ApiSourceManager(getContext());
        for (ApiSourceManager.Source src : am.sources()) {
            List<String> classes = am.homeClasses(src);
            if (classes == null) continue;
            for (String c : classes)
                out.add(new Item(c + " · 分类", "", "API:" + src.name));
        }
        addLocalVideos(out);
    }

    private void requestLocalPermission() {
        AppCompatActivity host = (AppCompatActivity) getActivity();
        int need = ContextCompat.checkSelfPermission(host, Manifest.permission.READ_MEDIA_VIDEO);
        if (need != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(host, new String[]{
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO}, 1001);
        } else {
            refresh();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            for (int r : grantResults) if (r == PackageManager.PERMISSION_GRANTED) { refresh(); return; }
            Toast.makeText(getContext(), "未授予媒体读取权限", Toast.LENGTH_SHORT).show();
        }
    }

    private void addLocalVideos(List<Item> out) {
        AppCompatActivity host = (AppCompatActivity) getActivity();
        if (host == null) return;
        int need = ContextCompat.checkSelfPermission(host, Manifest.permission.READ_MEDIA_VIDEO);
        if (need != PackageManager.PERMISSION_GRANTED) return;
        File dir = host.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (dir == null) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            String n = f.getName().toLowerCase();
            if (n.endsWith(".mp4") || n.endsWith(".mkv") || n.endsWith(".m4v") || n.endsWith(".avi"))
                out.add(new Item(f.getName(), f.getAbsolutePath(), "本地"));
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        List<Item> data;
        Adapter(List<Item> d) { this.data = d; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_card, p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Item it = data.get(pos);
            h.title.setText(it.title);
            h.link.setText(it.link != null && !it.link.isEmpty() ? it.link : "由 API 源解析");
            h.link.setVisibility(it.link != null && !it.link.isEmpty()
                    ? View.VISIBLE : View.GONE);
            h.type.setText(it.type);
            h.itemView.setOnClickListener(v -> openUrl(getContext(), it.link, it.title));
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title, link, type;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tv_item_title);
                link = v.findViewById(R.id.tv_item_link);
                type = v.findViewById(R.id.tv_item_type);
            }
        }
    }
}
