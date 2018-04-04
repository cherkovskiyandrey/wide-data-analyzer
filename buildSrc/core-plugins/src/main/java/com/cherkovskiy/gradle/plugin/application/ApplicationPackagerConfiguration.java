package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.vfs.dir.SimpleDirectoryProvider;

//TODO
public class ApplicationPackagerConfiguration {
    public static final String NAME = "applicationCfg";

    public String format = new SimpleDirectoryProvider().getSupportedFormats().get(0);

}
