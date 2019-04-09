package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.*;
import com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.api.bundles.ServiceDescriptor;
import com.cherkovskiy.application_context.api.configuration.ConfigurationContext;
import com.cherkovskiy.application_context.api.exceptions.BundleReloadException;
import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;
import com.cherkovskiy.application_context.compiler.BundleCompiler;
import com.cherkovskiy.application_context.compiler.CompileException;
import com.cherkovskiy.application_context.configuration.environments.ConfigurationImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;

class MonolithicApplicationContext implements ApplicationContext, BundleManagerProvider.Listener {
    @Nonnull
    private final ApplicationRootClassLoader rootClassLoader;
    @Nonnull
    private final Bundle appBundle;
    @Nonnull
    private final Map<String, Bundle> localBundles;
    @Nonnull
    private final ConfigurationImpl globalConfiguration;
    @Nonnull
    private final ConcurrentMap<String, BundleManagerProvider> bundleManagerProviders;
    private final Object reloadLock = new Object();

    MonolithicApplicationContext(
            @Nonnull ApplicationRootClassLoader rootClassLoader,
            @Nonnull Bundle appBundle,
            @Nonnull Map<String, Bundle> localBundles,
            @Nonnull ConfigurationImpl globalConfiguration,
            @Nonnull ConcurrentMap<String, BundleManagerProvider> bundleManagerProviders,
            @Nonnull BundleManagerListenerDelegate bundleManagerListenerProxy) {
        this.rootClassLoader = rootClassLoader;
        this.appBundle = appBundle;
        this.localBundles = localBundles;
        this.globalConfiguration = globalConfiguration;
        this.bundleManagerProviders = bundleManagerProviders;
        bundleManagerListenerProxy.delegateTo(this);
    }

    public void init() {
        // App bundle load first and eager
        appBundle.load();
        Set<ServiceDescriptor> appBundleServices = appBundle.getServices();

        //todo: закидываем всё в граф, и дальше по спеке
    }

    @Nonnull
    @Override
    public <T> T getService(@Nonnull Class<T> clsToken) throws ServiceNotFoundException {
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Nonnull
    @Override
    public <T> T getService(@Nonnull Class<T> clsToken, @Nonnull String serviceName) throws ServiceNotFoundException {
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Nonnull
    @Override
    public <T> T getService(@Nonnull Class<T> clsToken, @Nonnull String bundleName, @Nullable BundleVersion bundleVersion) throws ServiceNotFoundException {
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Nonnull
    @Override
    public <T> T getService(@Nonnull Class<T> clsToken, @Nonnull String serviceName, @Nonnull String bundleName, @Nullable BundleVersion bundleVersion) throws ServiceNotFoundException {
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Nonnull
    @Override
    public ConfigurationContext getConfigurationContext() {
        //TODO: по кол стэку вызова понять в рамках какого бадла я нахожусь и вернуть соответсвующий контекст
        //return configurationContext;
        //TODO: This code move to bundleContainer.getCurrentBundle().getConfigurationContext();
        //-------------
//        ConfigurationImpl configuration = StandardConfiguration.createLocalConfiguration(globalConfiguration);
//        return new ConfigurationContextProxy(configuration);
        //-------------
        //return bundleContainer.getCurrentBundle().getConfigurationContext();
        throw new UnsupportedOperationException("It is not supported yet.");
    }


    public void destroy() {
        //todo: stop all services and destroy all bundles
    }

    @Override
    public void onBundleChange(@Nonnull String name, @Nonnull BundleVersion bundleVersion, @Nonnull File file) throws BundleReloadException {
        final BundleFile bundleFile;
        try {
            bundleFile = new BundleFile(file);
        } catch (IOException e) {
            throw new BundleReloadException(format("Bundle %s, version %s could not be reloaded.", name, bundleVersion), e);
        }

        final BundleVersion newBundleVersion = new BundleVersionImpl(bundleFile.getVersion());
        final BundleVersionName newBundleVersionName = new BundleVersionName(bundleFile.getName(), newBundleVersion);

        if (!name.equalsIgnoreCase(bundleFile.getName())) {
            throw new BundleReloadException(format("Declared bundle name %s doesn't correspond to name in file: %s", name, bundleFile.getName()));
        }

        if (newBundleVersion.compareTo(bundleVersion) != 0) {
            throw new BundleReloadException(format("For bundle %s, declared bundle version %s doesn't correspond to version in file: %s",
                    name, bundleFile.getVersion(), newBundleVersion));
        }

        if (!localBundles.containsKey(name)) {
            throw new BundleReloadException(format("Bundle %s outside of distributive and could not be reloaded.", newBundleVersionName));
        }

        synchronized (reloadLock) {
            Bundle bundle = localBundles.get(name);
            if (bundle.getId().compareTo(newBundleVersionName) < 0) {
                throw new BundleReloadException(format("Bundle %s has incompatible version. Version has to be equals or higher a current one: %s.", newBundleVersionName, bundle));
            }

            ApplicationResolver applicationResolver = new ApplicationResolver(System.getenv("APP_HOME"));
            ResolvedBundleArtifact resolvedOrigBundle = applicationResolver.resolveOutSideBundle(bundleFile);

            BundleCompiler bundleCompiler = new BundleCompiler(rootClassLoader);
            final ResolvedBundleArtifact patchedBundle;
            try {
                patchedBundle = bundleCompiler.compile(resolvedOrigBundle);
            } catch (CompileException e) {
                throw new BundleReloadException(format("Bundle %s, version %s could not be reloaded.", name, bundleVersion), e);
            }

            applicationResolver.addReloadedBundle(patchedBundle);
            try {
                bundle.reload(patchedBundle);
            } catch (Exception e) {
                applicationResolver.removeReloadedBundle(patchedBundle);
            }
            applicationResolver.removeUnusedBundles();
        }
    }
}