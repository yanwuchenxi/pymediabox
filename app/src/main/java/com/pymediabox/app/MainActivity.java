package com.pymediabox.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvVersion = findViewById(R.id.tv_version);
        try {
            tvVersion.setText("v" + getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception ignored) { }

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_home));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_settings));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { selectTab(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        selectTab(0);
        handleIntent(getIntent());
    }

    private void selectTab(int pos) {
        Fragment f;
        switch (pos) {
            case 1: f = new SettingsFragment(); break;
            default: f = new HomeFragment(); break;
        }
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.fragment_container, f);
        t.commit();
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
