package com.pymediabox.app;

import android.app.Application;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class PymediaBoxApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!Python.isInited()) {
            Python.init(new AndroidPlatform(this));
        }
    }
}
