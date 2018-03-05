package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.BundleVersion;

public class BundleVersionImpl implements BundleVersion {
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
}
