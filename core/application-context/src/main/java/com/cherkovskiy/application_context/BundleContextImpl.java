package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.BundleContext;
import com.cherkovskiy.application_context.api.BundleManagerProvider;
import com.cherkovskiy.application_context.api.configuration.ConfigurableConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentMap;

//todo
public class BundleContextImpl implements BundleContext {
    public static final String DEFAULT_BUNDLE_MANAGER_PROVIDER_NAME = "<default>";
    @Nonnull
    private final ConfigurableConfiguration configurableConfiguration;
    @Nonnull
    private final ConcurrentMap<String, BundleManagerProvider> bundleManagerProviders;
    @Nonnull
    private final BundleManagerProvider.Listener bundleManagerListener;

    public BundleContextImpl(
            @Nonnull ConfigurableConfiguration configurableConfiguration,
            @Nonnull ConcurrentMap<String, BundleManagerProvider> bundleManagerProviders,
            @Nonnull BundleManagerProvider.Listener bundleManagerListener) {
        this.configurableConfiguration = configurableConfiguration;
        this.bundleManagerProviders = bundleManagerProviders;
        this.bundleManagerListener = bundleManagerListener;
    }

    @Nonnull
    @Override
    public ConfigurableConfiguration getConfigurableConfiguration() {
        return configurableConfiguration;
    }

    @Override
    public void addBundleManagerProvider(@Nonnull String name, @Nonnull BundleManagerProvider bundleManagerProvider) {
        bundleManagerProviders.put(name, bundleManagerProvider);
        bundleManagerProvider.addListener(bundleManagerListener);
    }

    @Nullable
    @Override
    public BundleManagerProvider removeBundleManagerProvider(@Nonnull String name) {
        BundleManagerProvider bundleManagerProvider = bundleManagerProviders.remove(name);
        bundleManagerProvider.removeListener(bundleManagerListener);
        return bundleManagerProvider;
    }

    @Override
    public BundleManagerProvider removeDefaultBundleManagerProvider() {
        return bundleManagerProviders.remove(DEFAULT_BUNDLE_MANAGER_PROVIDER_NAME);
    }
}
