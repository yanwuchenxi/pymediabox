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

import java.util.List;

/**
 * 历史/收藏 页（OK影视式）：顶部搜索框（对齐截图"搜索历史记录"）
 * + 分段（观看历史/影视收藏）+ 横向卡片网格。
 */
public class HistoryFragment extends Fragment {

    private RecyclerView recycler;
    private HistoryAdapter adapter;
    private HistoryManager history;
    private MaterialButton btnTabHistory, btnTabFav;
    private boolean favMode = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_history, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        history = new HistoryManager(getContext());
        recycler = v.findViewById(R.id.recycler_hist);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        btnTabHistory = v.findViewById(R.id.btn_tab_hist);
        btnTabFav = v.findViewById(R.id.btn_tab_fav);
        btnTabHistory.setOnClickListener(x -> setMode(false));
        btnTabFav.setOnClickListener(x -> setMode(true));

        v.findViewById(R.id.btn_search_hist).setOnClickListener(x -> {
            Toast.makeText(getContext(), "搜索历史记录（演示）", Toast.LENGTH_SHORT).show();
        });

        setMode(false);
    }

    private void setMode(boolean fav) {
        favMode = fav;
        int sel = 0xFF4CC9F0, un = 0xFF1E2130;
        int selText = 0xFF0D0F1A, unText = 0xFFFFFFFF;
        styleSeg(btnTabHistory, !fav, sel, un, selText, unText);
        styleSeg(btnTabFav, fav, sel, un, selText, unText);

        List<HistoryManager.HistoryItem> items = fav ? history.getFavorites() : history.getHistory();
        TextView empty = (TextView) requireView().findViewById(R.id.tv_hist_empty);
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        empty.setText(fav ? "没有更多收藏" : "没有更多数据");
        if (adapter == null) {
            adapter = new HistoryAdapter(items);
            recycler.setAdapter(adapter);
        } else {
            adapter.data = items;
            adapter.notifyDataSetChanged();
        }
    }

    private void styleSeg(MaterialButton b, boolean selected,
                          int sel, int un, int selText, int unText) {
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selected ? sel : un));
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
            h.meta.setText(favMode ? "收藏" : it.type + (it.time.isEmpty() ? "" : " · " + it.time));
            h.itemView.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), PlayerActivity.class);
                i.putExtra("url", it.url);
                i.putExtra("title", it.title);
                startActivity(i);
            });
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
}
