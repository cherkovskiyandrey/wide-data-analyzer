package com.cherkovskiy.gradle.plugin.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;

//todo: move to application-context (?)
public interface Dependency {

    @Nonnull
    String getGroup();

    @Nonnull
    String getName();

    @Nonnull
    String getVersion();

    @Nullable
    String getFileName();

    static String toString(Dependency bundleDependency) {
        return String.join(":", bundleDependency.getGroup(), bundleDependency.getName(), bundleDependency.getVersion());
    }

    Comparator<? super Dependency> COMPARATOR = Comparator.comparing(Dependency::getGroup)
            .thenComparing(Comparator.comparing(Dependency::getName))
            .thenComparing(Comparator.comparing(Dependency::getVersion));
}
