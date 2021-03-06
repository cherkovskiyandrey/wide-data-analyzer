package com.cherkovskiy.gradle.plugin.bundle;

public class BundlePackagerConfiguration {
    public static final String NAME = "bundle";

    /**
     * include dependencies of this bundle into archive or not (including api)
     */
    public boolean embeddedDependencies = false;

    public boolean failIfNotBundle = true;
}
