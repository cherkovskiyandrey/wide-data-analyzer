package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.BundleManagerProvider;
import com.cherkovskiy.application_context.api.BundleVersion;
import com.cherkovskiy.application_context.api.exceptions.BundleReloadException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public class BundleManagerListenerDelegate implements BundleManagerProvider.Listener {
    @Nullable
    private BundleManagerProvider.Listener delegate;

    @Override
    public void onBundleChange(@Nonnull String name, @Nonnull BundleVersion bundleVersion, @Nonnull File file) throws BundleReloadException {
        if (delegate != null) {
            delegate.onBundleChange(name, bundleVersion, file);
        }
    }

    public void delegateTo(@Nonnull BundleManagerProvider.Listener delegate) {
        this.delegate = delegate;
    }
}
