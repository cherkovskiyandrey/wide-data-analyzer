package com.cherkovskiy.gradle.plugin;

//todo: move to application-context (?)
public interface Artifact {
    String getGroup();

    String getName();

    String getVersion();
}
