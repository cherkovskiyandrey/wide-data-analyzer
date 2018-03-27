package com.cherkovskiy.gradle.plugin;

public class BundlePackagerConfiguration {
    public static final String NAME = "bundle";

    /**
     * include dependencies of this bundle into archive or not (including api)
     */
    boolean embeddedDependencies = false;
}
