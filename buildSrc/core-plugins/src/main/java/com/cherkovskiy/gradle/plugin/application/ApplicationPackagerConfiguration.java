package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.vfs.dir.SimpleDirectoryProvider;


public class ApplicationPackagerConfiguration {
    public static final String NAME = "applicationCfg";

    public String format = new SimpleDirectoryProvider().getSupportedFormats().get(0);

    public boolean failOnErrors = false;

    public String pathToBinsResources = "core/application-common/bin";

    public String starterName = "application-starter";

}
