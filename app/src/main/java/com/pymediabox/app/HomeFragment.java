package com.pymediabox.app;

import android.Manifest;
import android.content.Intent;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String KEY_FILE = "file";
    public static final String KEY_TITLE = "title";
    public static final String KEY_INDEX = "index";

    private RecyclerView recycler;
    private Adapter adapter;
    private List<Item> items = new ArrayList<>();
    private SwipeRefreshLayout swipe;

    public static void openUrl(android.content.Context ctx, String url, String title) {
        Intent i = new Intent(ctx, PlayerActivity.class);
        i.putExtra("url", url);
        i.putExtra("title", title);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    static class Item {
        String title, link, type;
        Item(String t, String l, String ty) { title=t; link=l; type=ty; }
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

        // 示例数据 + 本地扫描
        refresh(true);
        swipe.setOnRefreshListener(this::refresh);
    }

    private void refresh(boolean showLoading) {
        if (showLoading) swipe.setRefreshing(true);
        items.clear();
        items.add(new Item("在线示例 · Big Buck Bunny",
                "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4", "在线"));
        items.add(new Item("在线示例 · H.265 测试",
                "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4", "在线"));
        addLocalVideos(items);
        if (adapter == null) {
            adapter = new Adapter(items);
            recycler.setAdapter(adapter);
        } else adapter.notifyDataSetChanged();
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
            if (n.endsWith(".mp4") || n.endsWith(".mkv") || n.endsWith(".m4v"))
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
                Intent i = new Intent(getContext(), PlayerActivity.class);
                i.putExtra("url", it.link);
                i.putExtra("title", it.title);
                startActivity(i);
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
