package com.pymediabox.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private View[] navs;
    private int current = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvVersion = findViewById(R.id.tv_version);
        try {
            tvVersion.setText("v" + getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception ignored) { }

        // 顶部 2 Tab（首页/设置）+ 底部 4 导航（首页/搜索/历史收藏/设置）
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_home));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_settings));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                selectTab(tab.getPosition());
                syncBottomNav();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        navs = new View[]{
                findViewById(R.id.bnav_home),
                findViewById(R.id.bnav_search),
                findViewById(R.id.bnav_fav),
                findViewById(R.id.bnav_settings),
        };
        navs[0].setOnClickListener(v -> { tabLayout.selectTab(0); });
        navs[1].setOnClickListener(v -> {
            Intent i = new Intent(this, PlayerActivity.class);
            i.putExtra("url", "https://example.com?q=");
            i.putExtra("title", "搜索");
            startActivity(i);
        });
        navs[2].setOnClickListener(v -> selectTab(0)); // 历史/收藏 在首页分段区
        navs[3].setOnClickListener(v -> tabLayout.selectTab(1));

        selectTab(0);
        syncBottomNav();
        handleIntent(getIntent());
    }

    private void selectTab(int pos) {
        current = pos;
        Fragment f = (pos == 1) ? new SettingsFragment() : new HomeFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.fragment_container, f);
        t.commit();
    }

    private void syncBottomNav() {
        int active = (current == 0) ? 0 : 3;
        for (int i = 0; i < navs.length; i++) {
            TextView label = (TextView) ((android.view.ViewGroup) navs[i]).getChildAt(1);
            int icon = i;
            label.setTextColor(i == active ? 0xFF4CC9F0 : 0xFF8A8FA8);
        }
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getDataString() != null) {
            Intent i = new Intent(this, PlayerActivity.class);
            i.putExtra("url", intent.getDataString());
            i.putExtra("title", "来自分享");
            startActivity(i);
        }
    }
}
