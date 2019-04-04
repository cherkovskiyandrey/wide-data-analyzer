package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.ApplicationContext;
import com.cherkovskiy.application_context.api.Bundle;
import com.cherkovskiy.application_context.api.BundleManagerProvider;
import com.cherkovskiy.application_context.api.BundleVersion;
import com.cherkovskiy.application_context.api.bundles.ServiceDescriptor;
import com.cherkovskiy.application_context.api.configuration.ConfigurationContext;
import com.cherkovskiy.application_context.api.exceptions.BundleReloadException;
import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;
import com.cherkovskiy.application_context.configuration.environments.ConfigurationImpl;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;

class MonolithicApplicationContext implements ApplicationContext, BundleManagerProvider.Listener {
    @Nonnull
    private final Bundle appBundle;
    @Nonnull
    private final Map<String, Bundle> localBundles;
    @Nonnull
    private final ConfigurationImpl globalConfiguration;
    @Nonnull
    private final ConcurrentMap<String, BundleManagerProvider> bundleManagerProviders;


    MonolithicApplicationContext(
            @Nonnull Bundle appBundle,
            @Nonnull Map<String, Bundle> localBundles,
            @Nonnull ConfigurationImpl globalConfiguration,
            @Nonnull ConcurrentMap<String, BundleManagerProvider> bundleManagerProviders,
            @Nonnull BundleManagerListenerDelegate bundleManagerListenerProxy) {
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
        final BundleFile bundleFile = new BundleFile(file);
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
        Bundle bundle = localBundles.get(name);
        if (bundle.getId().compareTo(newBundleVersionName) < 0) {
            throw new BundleReloadException(format("Bundle %s has incompatible version. Version has to be equals or higher a current one: %s.", newBundleVersionName, bundle));
        }

        final BundleResolver resolver;
        if (bundleFile.isEmbedded()) {
            //todo: special direcory where budle will be unpacked
            final File bundleUnpackDir = new File(baseTmpDir, bundleFile.getName());
            FileUtils.forceMkdir(bundleUnpackDir);
            resolver = new EmbeddedResolver(bundleUnpackDir);
        } else {
            resolver = new ProjectBundleResolver(Lists.newArrayList(dependencies));
        }
    }
}