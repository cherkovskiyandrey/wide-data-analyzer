package com.cherkovskiy.application_context.api;

import com.cherkovskiy.application_context.api.configuration.ConfigurableConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Used to customise system when bundle is loading.
 */
public interface BundleContext {

    @Nonnull
    ConfigurableConfiguration getConfigurableConfiguration();

    void addBundleManagerProvider(@Nonnull String name, @Nonnull BundleManagerProvider bundleManagerProvider);

    @Nullable
    BundleManagerProvider removeBundleManagerProvider(@Nonnull String name);

    BundleManagerProvider removeDefaultBundleManagerProvider();
}
