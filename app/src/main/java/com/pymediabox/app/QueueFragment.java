package com.pymediabox.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * 播放队列 Fragment：展示历史记录与收藏
 * 参考 GSYVideoPlayer 的播放列表设计
 */
public class QueueFragment extends Fragment {

    private RecyclerView recycler;
    private TextView tvEmpty;
    private Adapter adapter;
    private HistoryManager history;
    private boolean showFavorites = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_queue, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        history = new HistoryManager(getContext());
        recycler = v.findViewById(R.id.recycler_queue);
        tvEmpty = v.findViewById(R.id.tv_empty_queue);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        MaterialButton btnToggle = v.findViewById(R.id.btn_toggle_queue);
        btnToggle.setOnClickListener(x -> {
            showFavorites = !showFavorites;
            btnToggle.setText(showFavorites ? "显示历史" : "显示收藏");
            refresh();
        });

        refresh();
    }

    private void refresh() {
        List<HistoryManager.HistoryItem> items = showFavorites
                ? history.getFavorites() : history.getHistory();
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmpty.setText(showFavorites ? "暂无收藏" : "暂无播放历史");
        if (adapter == null) {
            adapter = new Adapter(items);
            recycler.setAdapter(adapter);
        } else {
            adapter.data = items;
            adapter.notifyDataSetChanged();
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        List<HistoryManager.HistoryItem> data;
        Adapter(List<HistoryManager.HistoryItem> d) { this.data = d; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_queue, p, false));
        }
        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            HistoryManager.HistoryItem it = data.get(pos);
            h.title.setText(it.title);
            h.meta.setText(it.type + (it.time.isEmpty() ? "" : " · " + it.time));
            h.itemView.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), PlayerActivity.class);
                i.putExtra("url", it.url);
                i.putExtra("title", it.title);
                startActivity(i);
            });
            // 收藏按钮
            if (h.favoriteBtn != null) {
                boolean fav = history.isFavorite(it.url);
                h.favoriteBtn.setText(fav ? "★" : "☆");
                h.favoriteBtn.setOnClickListener(b -> {
                    boolean added = history.toggleFavorite(it.url);
                    ((TextView) b).setText(added ? "★" : "☆");
                    Toast.makeText(getContext(), added ? "已收藏" : "已取消收藏",
                            Toast.LENGTH_SHORT).show();
                });
            }
        }
        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title, meta, favoriteBtn;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tv_queue_title);
                meta = v.findViewById(R.id.tv_queue_meta);
                favoriteBtn = v.findViewById(R.id.btn_favorite);
            }
        }
    }
}
