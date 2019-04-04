package com.cherkovskiy.application_context.api;

import com.cherkovskiy.application_context.api.exceptions.BundleReloadException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;

/**
 * Custom implementation of bundle reloading mechanism could be provided by means of {@link BundleContext}.
 * For example by means of REST or WEB UI.
 */
public interface BundleManagerProvider {

    interface Listener {

        void onBundleChange(@Nonnull String name, @Nonnull BundleVersion bundleVersion, @Nonnull File file) throws BundleReloadException;
    }

    void addListener(@Nonnull Listener listener);

    void removeListener(@Nonnull Listener listener);
}
