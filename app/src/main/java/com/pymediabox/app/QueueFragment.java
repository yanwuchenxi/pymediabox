package com.pymediabox.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
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

import java.util.List;

/**
 * 播放队列：历史 / 收藏双视图（分段按钮切换），条目可进播放页、可切换收藏。
 */
public class QueueFragment extends Fragment {

    private RecyclerView recycler;
    private TextView tvEmpty;
    private Adapter adapter;
    private boolean showFavorites = false;
    private HistoryManager history;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_queue, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        history = new HistoryManager(getContext());
        recycler = v.findViewById(R.id.recycler_queue);
        tvEmpty = v.findViewById(R.id.tv_empty_queue);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        MaterialButton btnHistory = v.findViewById(R.id.btn_tab_history);
        MaterialButton btnFav = v.findViewById(R.id.btn_tab_fav);
        btnHistory.setOnClickListener(x -> setView(false, btnHistory, btnFav));
        btnFav.setOnClickListener(x -> setView(true, btnHistory, btnFav));

        setView(false, btnHistory, btnFav);
    }

    private void setView(boolean fav, MaterialButton btnHistory, MaterialButton btnFav) {
        showFavorites = fav;
        int sel = 0xFF4CC9F0;   // accent
        int un = 0xFF1E2130;    // card_bg
        int selText = 0xFF0D0F1A;
        int unText = 0xFFFFFFFF;
        btnHistory.setBackgroundTintList(ColorStateList.valueOf(fav ? un : sel));
        btnHistory.setTextColor(fav ? unText : selText);
        btnFav.setBackgroundTintList(ColorStateList.valueOf(fav ? sel : un));
        btnFav.setTextColor(fav ? selText : unText);
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
            adapter.bind(items);
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private List<HistoryManager.HistoryItem> data;

        Adapter(List<HistoryManager.HistoryItem> d) { this.data = d; }
        void bind(List<HistoryManager.HistoryItem> d) { data = d; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_queue, p, false));
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            HistoryManager.HistoryItem it = data.get(pos);
            h.title.setText(it.title);
            h.meta.setText(it.type + (it.time.isEmpty() ? "" : " · " + it.time));
            h.itemView.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), PlayerActivity.class);
                i.putExtra("url", it.url);
                i.putExtra("title", it.title);
                startActivity(i);
            });
            boolean fav = history.isFavorite(it.url);
            h.favBtn.setText(fav ? "★" : "☆");
            h.favBtn.setOnClickListener(b -> {
                boolean added = history.toggleFavorite(it.url);
                ((TextView) b).setText(added ? "★" : "☆");
                Toast.makeText(getContext(), added ? "已收藏" : "已取消收藏",
                        Toast.LENGTH_SHORT).show();
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title, meta, favBtn;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tv_queue_title);
                meta = v.findViewById(R.id.tv_queue_meta);
                favBtn = v.findViewById(R.id.btn_favorite);
            }
        }
    }
}
