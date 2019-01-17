package com.cherkovskiy.application_context.api;

import javax.annotation.Nullable;

public interface BundleVersion {

    int getMajor();

    int getMinor();

    @Nullable
    String getSnapshot();
}
