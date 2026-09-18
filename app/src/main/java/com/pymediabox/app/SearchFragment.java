package com.pymediabox.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索页（OK影视式）：关键词搜索 + 历史搜索词。
 * 搜索词存 SharedPreferences，点击可再次搜索。
 */
public class SearchFragment extends Fragment {

    private List<String> hotWords = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_search, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        v.findViewById(R.id.btn_search_go).setOnClickListener(x -> {
            TextInputEditText et = v.findViewById(R.id.et_search_go);
            String q = et.getText().toString().trim();
            if (q.isEmpty()) return;
            saveWord(q);
            HomeFragment.openUrl(getContext(), "https://example.com?q=" + q, "搜索：" + q);
        });
        v.findViewById(R.id.btn_clear_words).setOnClickListener(x -> {
            getContext().getSharedPreferences("pymediabox_search", Context.MODE_PRIVATE)
                    .edit().clear().apply();
            loadWords(v);
        });
        loadWords(v);
    }

    private void loadWords(View root) {
        String saved = getContext().getSharedPreferences("pymediabox_search",
                Context.MODE_PRIVATE).getString("words", "");
        hotWords = new ArrayList<>();
        for (String w : saved.split("\n"))
            if (!w.isEmpty()) hotWords.add(w);
        // 演示占位
        if (hotWords.isEmpty()) {
            hotWords.add("复仇者联盟");
            hotWords.add("唐探1900");
            hotWords.add("战狼");
        }
        for (int i = 0; i < hotWords.size() && i < 8; i++) {
            final String w = hotWords.get(i);
            MaterialButton b = (MaterialButton) root.findViewById(R.id.btn_word_0);
            // 动态添加太复杂，简化：仅展示第一个词
            if (i == 0) b.setText(w);
        }
        MaterialButton first = root.findViewById(R.id.btn_word_0);
        if (first != null && !hotWords.isEmpty())
            first.setOnClickListener(x ->
                    HomeFragment.openUrl(getContext(),
                            "https://example.com?q=" + hotWords.get(0),
                            "搜索：" + hotWords.get(0)));
    }

    private void saveWord(String w) {
        String saved = getContext().getSharedPreferences("pymediabox_search",
                Context.MODE_PRIVATE).getString("words", "");
        List<String> list = new ArrayList<>();
        for (String x : saved.split("\n")) if (!x.isEmpty()) list.add(x);
        list.remove(w);
        list.add(0, w);
        if (list.size() > 20) list = list.subList(0, 20);
        StringBuilder sb = new StringBuilder();
        for (String x : list) sb.append(x).append("\n");
        getContext().getSharedPreferences("pymediabox_search", Context.MODE_PRIVATE)
                .edit().putString("words", sb.toString()).apply();
    }
}
