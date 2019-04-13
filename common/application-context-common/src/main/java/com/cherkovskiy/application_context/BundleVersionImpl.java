package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.BundleVersion;

import javax.annotation.Nonnull;

public class BundleVersionImpl implements BundleVersion {
    public BundleVersionImpl(@Nonnull String fullVersion) {
        //todo
    }

    @Override
    public int getMajor() {
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Override
    public int getMinor() {
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Override
    public String getSnapshot() {
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Nonnull
    public static BundleVersion valueOf(@Nonnull String versionString) {
        //todo
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int compareTo(@Nonnull BundleVersion bundleVersion) {
        //todo
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String toString() {
        return "BundleVersionImpl{}";
    }
}
