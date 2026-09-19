package com.pymediabox.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * 主框架：顶栏（Logo + 源选择）+ 内容容器 + 底部 4 导航。
 * 统一导航到底部，顶部不再有 Tab，消除重复导航。
 * 4 个页面：首页 / 搜索 / 历史收藏 / 设置
 */
public class MainActivity extends AppCompatActivity {

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

        navs = new View[]{
                findViewById(R.id.bnav_home),
                findViewById(R.id.bnav_search),
                findViewById(R.id.bnav_fav),
                findViewById(R.id.bnav_settings),
        };
        navs[0].setOnClickListener(v -> openPage(0));
        navs[1].setOnClickListener(v -> openPage(1));
        navs[2].setOnClickListener(v -> openPage(2));
        navs[3].setOnClickListener(v -> openPage(3));

        openPage(0);
        handleIntent(getIntent());
    }

    private void openPage(int pos) {
        if (pos == current) return;
        current = pos;
        Fragment f;
        switch (pos) {
            case 1: f = new SearchFragment(); break;
            case 2: f = new HistoryFragment(); break;
            case 3: f = new SettingsFragment(); break;
            default: f = new HomeFragment(); break;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f).commit();
        syncBottomNav();
    }

    private void syncBottomNav() {
        String[] labels = {"首页", "搜索", "历史/收藏", "设置"};
        for (int i = 0; i < navs.length; i++) {
            LinearLayout ll = (LinearLayout) navs[i];
            int n = ll.getChildCount();
            if (n < 2) continue;
            TextView label = (TextView) ll.getChildAt(n - 1);
            boolean sel = (i == current);
            int color = sel ? 0xFFC9A86A : 0xFF8A8FA8;  // 金色
            label.setTextColor(color);
            TextView icon = (TextView) ll.getChildAt(0);
            icon.setTextColor(color);
            // 选中时图标放大
            android.view.ViewGroup.LayoutParams lp = icon.getLayoutParams();
            int h = sel ? 56 : 44;
            lp.height = android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, h, getResources().getDisplayMetrics());
            icon.setLayoutParams(lp);
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
