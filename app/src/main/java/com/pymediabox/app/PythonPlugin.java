package com.pymediabox.app;

import com.chaquo.python.PyPlugin;
import com.chaquo.python.PyPluginConfig;
import java.io.File;

public class PythonPlugin extends PyPlugin {
    @Override
    public File getPythonRoot() {
        return new File(getFilesDir(), "pymedia");
    }
}
