package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.ApplicationRootClassLoader;
import com.cherkovskiy.application_context.api.Bundle;
import com.cherkovskiy.application_context.api.BundleManagerProvider;
import com.cherkovskiy.application_context.api.ContextBuilder;
import com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.configuration.environments.ConfigurationImpl;
import com.cherkovskiy.application_context.configuration.environments.StandardConfiguration;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cherkovskiy.application_context.BundleContextImpl.DEFAULT_BUNDLE_MANAGER_PROVIDER_NAME;

public class MonolithicApplicationContextBuilder implements ContextBuilder {

    @Nonnull
    private ApplicationRootClassLoader rootClassLoader;
    @Nullable
    private BundleManagerProvider bundleManagerProvider;

    @Override
    @Nonnull
    public ContextBuilder setArguments(@Nonnull String[] args) {
        //TODO: запихать аргументы в ConfigurationImpl
        return this;
    }

    @Override
    @Nonnull
    public ContextBuilder setRootClassLoader(@Nonnull ApplicationRootClassLoader rootClassLoader) {
        this.rootClassLoader = rootClassLoader;
        return this;
    }

    @Nonnull
    @Override
    public ContextBuilder setBundleManagerProvider(@Nullable BundleManagerProvider bundleManagerProvider) {
        this.bundleManagerProvider = bundleManagerProvider;
        return this;
    }


    //TODO: разобраться с исключениями
    @Override
    @Nonnull
    public MonolithicApplicationContext build() {
        Preconditions.checkNotNull(rootClassLoader);

        String appHome = System.getenv("APP_HOME");
        ApplicationResolver applicationResolver = new ApplicationResolver(appHome);
        ResolvedBundleArtifact resolvedAppBundleArtifact = applicationResolver.resolveApplicationBundle();
        List<ResolvedBundleArtifact> resolvedBundles = applicationResolver.resolveOtherBundles();

        ConfigurationImpl globalConfiguration = StandardConfiguration.create();
        ConcurrentMap<String, BundleManagerProvider> bundleManagerProviders = Maps.newConcurrentMap();
        if (bundleManagerProvider != null) {
            bundleManagerProviders.put(DEFAULT_BUNDLE_MANAGER_PROVIDER_NAME, bundleManagerProvider);
        }
        BundleManagerListenerDelegate bundleManagerListenerDelegate = new BundleManagerListenerDelegate();

        Bundle appBundle = new LocalBundle(resolvedAppBundleArtifact, rootClassLoader, globalConfiguration, bundleManagerProviders, bundleManagerListenerDelegate);
        Map<String, Bundle> localBundles = resolvedBundles.stream()
                .map(resolvedBundleArtifact -> new LocalBundle(resolvedBundleArtifact, rootClassLoader, globalConfiguration, bundleManagerProviders, bundleManagerListenerDelegate))
                .collect(Collectors.toMap(
                        b -> b.getId().getName(),
                        Function.identity(),
                        (a, b) -> a
                ));

        //TODO: грузим import сервисы
        //List<Bundle> remoteBundle = //TODO

        return new MonolithicApplicationContext(
                rootClassLoader,
                appBundle,
                localBundles,
                globalConfiguration,
                bundleManagerProviders,
                bundleManagerListenerDelegate
        );
    }
}
