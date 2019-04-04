package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.BundleVersion;

import javax.annotation.Nonnull;

public class BundleVersionName implements Comparable<BundleVersionName> {
    public BundleVersionName(@Nonnull String name, @Nonnull BundleVersion bundleVersion) {
        //todo
    }
    //todo

    public String getName() {
//todo
    }

    public BundleVersion getVersion() {
        //todo
    }

    @Override
    public String toString() {
        return "BundleVersionName{}";
    }

    @Override
    public int compareTo(@Nonnull BundleVersionName bundleVersionName) {
        //todo
    }
}
