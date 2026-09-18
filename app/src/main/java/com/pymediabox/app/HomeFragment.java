package com.pymediabox.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recycler;
    private Adapter adapter;
    private List<Item> items = new ArrayList<>();
    private SwipeRefreshLayout swipe;
    private EditText etSearch;
    private MaterialButton btnSearch, btnScanLocal;
    private TextView tvEmpty;

    static class Item {
        String title, link, type;
        Item(String t, String l, String ty) { title=t; link=l; type=ty; }
    }

    public static void openUrl(Context ctx, String url, String title) {
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
        recycler = v.findViewById(R.id.recycler_home);
        swipe = v.findViewById(R.id.swipe_home);
        swipe.setColorSchemeResources(R.color.accent);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        etSearch = v.findViewById(R.id.et_search);
        btnSearch = v.findViewById(R.id.btn_search);
        btnScanLocal = v.findViewById(R.id.btn_scan_local);
        tvEmpty = v.findViewById(R.id.tv_empty);

        btnSearch.setOnClickListener(x -> {
            String q = etSearch.getText().toString().trim();
            if (q.isEmpty()) return;
            // 搜索：构造 URL 跳转播放页（实际项目中可替换为真实搜索源）
            openUrl(getContext(), "https://example.com?q=" + q, "搜索：" + q);
        });
        btnScanLocal.setOnClickListener(x -> requestLocalPermission());

        refresh(true);
        swipe.setOnRefreshListener(() -> refresh(false));
    }

    private void requestLocalPermission() {
        AppCompatActivity host = (AppCompatActivity) getActivity();
        int need = ContextCompat.checkSelfPermission(host, Manifest.permission.READ_MEDIA_VIDEO);
        if (need != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(host, new String[]{
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO}, 1001);
        } else {
            refresh(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            for (int r : grantResults) if (r == PackageManager.PERMISSION_GRANTED) { refresh(false); return; }
            Toast.makeText(getContext(), "未授予媒体读取权限", Toast.LENGTH_SHORT).show();
        }
    }

    private void refresh(boolean showLoading) {
        if (showLoading) swipe.setRefreshing(true);
        items.clear();
        // 在线示例
        items.add(new Item("在线示例 · Big Buck Bunny",
                "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4", "在线"));
        items.add(new Item("在线示例 · Sintel",
                "https://media.w3.org/2010/05/sintel/trailer.mp4", "在线"));
        // API 源分类（TVBox 式，内置演示源）
        ApiSourceManager am = new ApiSourceManager(getContext());
        for (ApiSourceManager.Source src : am.sources()) {
            java.util.List<String> classes = am.homeClasses(src);
            if (classes == null) continue;
            for (String c : classes) {
                items.add(new Item(c + " · 分类", "", "API:" + src.name));
            }
        }
        // 本地媒体
        addLocalVideos(items);
        // 空状态
        if (items.isEmpty()) tvEmpty.setVisibility(View.VISIBLE);
        else tvEmpty.setVisibility(View.GONE);

        if (adapter == null) {
            adapter = new Adapter(items);
            recycler.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        swipe.setRefreshing(false);
    }

    private void addLocalVideos(List<Item> out) {
        AppCompatActivity host = (AppCompatActivity) getActivity();
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

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_card, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Item it = data.get(pos);
            h.title.setText(it.title);
            h.link.setText(it.link);
            h.type.setText(it.type);
            h.itemView.setOnClickListener(v -> {
                if (it.link == null || it.link.isEmpty()) {
                    android.widget.Toast.makeText(getContext(),
                            "「" + it.type + "」分类解析需要配置对应 API 源",
                            android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    openUrl(getContext(), it.link, it.title);
                }
            });
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
