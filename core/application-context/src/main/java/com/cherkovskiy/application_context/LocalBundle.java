package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.ApplicationRootClassLoader;
import com.cherkovskiy.application_context.api.Bundle;
import com.cherkovskiy.application_context.api.BundleLifecycle;
import com.cherkovskiy.application_context.api.BundleManagerProvider;
import com.cherkovskiy.application_context.api.bundles.Dependency;
import com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.api.bundles.ResolvedDependency;
import com.cherkovskiy.application_context.api.bundles.ServiceDescriptor;
import com.cherkovskiy.application_context.api.configuration.ConfigurableConfiguration;
import com.cherkovskiy.application_context.api.configuration.ConfigurationContext;
import com.cherkovskiy.application_context.api.exceptions.BundleReloadException;
import com.cherkovskiy.application_context.configuration.ConfigurableConfigurationProxy;
import com.cherkovskiy.application_context.configuration.ConfigurationContextProxy;
import com.cherkovskiy.application_context.configuration.environments.ConfigurationImpl;
import com.cherkovskiy.application_context.configuration.environments.StandardConfiguration;
import com.cherkovskiy.application_context.exceptions.BundleLoadException;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

class LocalBundle implements Bundle {
    @Nonnull
    private final ResolvedBundleArtifact resolvedBundleArtifact;
    @Nonnull
    private final ConfigurationImpl localConfiguration;
    @Nonnull
    private final ApplicationRootClassLoader rootClassLoader;
    @Nonnull
    private final BundleClassLoader bundleClassLoader;
    @Nonnull
    private final ConfigurationContext configurationContext;
    @Nonnull
    private final ConfigurableConfiguration configurableConfiguration;
    @Nonnull
    private final BundleContextImpl bundleContext;

    LocalBundle(@Nonnull ResolvedBundleArtifact resolvedBundleArtifact,
                @Nonnull ApplicationRootClassLoader rootClassLoader,
                @Nonnull ConfigurationImpl globalConfiguration,
                @Nonnull ConcurrentMap<String, BundleManagerProvider> bundleManagerProviders,
                @Nonnull BundleManagerProvider.Listener bundleManagerListener) {
        this.resolvedBundleArtifact = resolvedBundleArtifact;
        this.rootClassLoader = rootClassLoader;
        this.localConfiguration = StandardConfiguration.createLocalConfiguration(globalConfiguration);
        this.bundleClassLoader = new BundleClassLoader(rootClassLoader.getUnderlyingClassLoader());
        this.configurationContext = new ConfigurationContextProxy(this.localConfiguration);
        this.configurableConfiguration = new ConfigurableConfigurationProxy(this.localConfiguration);
        this.bundleContext = new BundleContextImpl(
                configurableConfiguration,
                bundleManagerProviders,
                bundleManagerListener
        );
    }

    @Override
    public BundleVersionName getId() {
        //todo
    }

    @Override
    public void load() {
        try {
            // Register api dependencies
            Set<ResolvedDependency> apiDependencies = Sets.newTreeSet(Dependency.COMPARATOR);
            apiDependencies.addAll(resolvedBundleArtifact.getApiExport());
            apiDependencies.addAll(resolvedBundleArtifact.getApiImport());
            apiDependencies.addAll(resolvedBundleArtifact.getCommon());
            rootClassLoader.updateClasses(apiDependencies);

            // Set other dependencies to class loader
            Set<File> internalDependencies = Stream.concat(resolvedBundleArtifact.getImplExternal().stream(), resolvedBundleArtifact.getImplInternal().stream())
                    .map(ResolvedDependency::getFile)
                    .collect(Collectors.toSet());
            internalDependencies.add(resolvedBundleArtifact.getFile());
            bundleClassLoader.addDependencies(internalDependencies);

            // run com.cherkovskiy.application_context.api.BundleLifecycle.beforeInit()
            if (resolvedBundleArtifact.getStarter() != null) {
                Class<?> starterClass = bundleClassLoader.loadClass(resolvedBundleArtifact.getStarter());

                if (!BundleLifecycle.class.isAssignableFrom(starterClass)) {
                    throw new RuntimeException(format("Starter class \"%s\" doesn't implement \"%s\" in bundle \"%s\" located in %s",
                            resolvedBundleArtifact.getStarter(),
                            BundleLifecycle.class.getName(),
                            resolvedBundleArtifact.getName(),
                            resolvedBundleArtifact.getFile().getAbsolutePath()
                    ));
                }

                @SuppressWarnings("unchecked")
                Class<? extends BundleLifecycle> bundleLifecycleClass = (Class<? extends BundleLifecycle>) starterClass;
                BundleLifecycle bundleLifecycleBean = bundleLifecycleClass.newInstance();

                bundleLifecycleBean.beforeInit(BundleVersionImpl.valueOf(resolvedBundleArtifact.getVersion()), bundleContext);
                //todo: refresh всем конфигурационным контекстам во всех уже загруженных бандлах
            }
        } catch (Exception e) {
            throw new BundleLoadException(e);
        }
    }

    @Override
    public void reload(@Nonnull ResolvedBundleArtifact resolvedBundleArtifact) throws BundleReloadException {
        //todo: смотрим на вресию текущую и та что на входе, если равны то на com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact.reloadNumber
        // , и для тех артефактов для которых версия на входе или reloadNumber больше - загружаем их в rootClassLoader,
        // так же перегружаем само тело бандла - создаём новый класс лоадер bundleClassLoader
        //TODO: отдельно потом разобраться с сервисами, прокси на них, рефреш евентом и далее по спеке
    }

    @Override
    public void unload() {
        //todo
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Nonnull
    @Override
    public Set<ServiceDescriptor> getServices() {
        return resolvedBundleArtifact.getServices();
    }
}
