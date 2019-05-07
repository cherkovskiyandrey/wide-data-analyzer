package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.*;
import com.cherkovskiy.application_context.api.bundles.Dependency;
import com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.api.bundles.ResolvedDependency;
import com.cherkovskiy.application_context.api.bundles.ServiceDescriptor;
import com.cherkovskiy.application_context.api.configuration.ConfigurationContext;
import com.cherkovskiy.application_context.api.exceptions.BundleReloadException;
import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;
import com.cherkovskiy.application_context.compiler.BundleCompiler;
import com.cherkovskiy.application_context.compiler.CompileException;
import com.cherkovskiy.application_context.configuration.environments.ConfigurationImpl;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;

public class MonolithicApplicationContext implements ApplicationContext, BundleManagerProvider.Listener {
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
    private final Object loadLock = new Object();

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
        synchronized (loadLock) {
            appBundle.load();
        }

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
        //TODO: если бандл оригинальный ещё не загружен? - всё тоже самое делаем, но только не грузим его, единственное отличие
        //то что при многократной перегрузке- все промежуточные версии можно удалять, т.к. они будут расширяться,
        //кроме api-export, т.к. может быть кейс что некий api-A экспортируется бандлом А и бандлом Б,
        //бандл А уже загружен, бандл Б - нет, релоадим бандл Б - сгенерировали api-A_v1 и загрузили ТОЛЬКО это
        //апи, для того чтобы при перезагрузки бандла А была возможность отталкиваться от api-A_v1 а не от дефолтной
        //при повтором релоаде бадла Б (хотя он ещё не загружен) - генерим api-A_v3 отталкиваясь от api-A_v2 который сгенерил
        //бандл А. Если запросили загрузку бандла Б - грузим только тело последнего

        final BundleFile bundleFileToReload;
        try {
            bundleFileToReload = new BundleFile(file);
        } catch (IOException e) {
            throw new BundleReloadException(format("Bundle %s, version %s could not be reloaded.", name, bundleVersion), e);
        }

        final BundleVersion newBundleVersion = new BundleVersionImpl(bundleFileToReload.getVersion());
        final BundleVersionName newBundleVersionName = new BundleVersionName(bundleFileToReload.getName(), newBundleVersion);

        if (!name.equalsIgnoreCase(bundleFileToReload.getName())) {
            throw new BundleReloadException(format("Declared bundle name %s doesn't correspond to name in file: %s", name, bundleFileToReload.getName()));
        }

        if (newBundleVersion.compareTo(bundleVersion) != 0) {
            throw new BundleReloadException(format("For bundle %s, declared bundle version %s doesn't correspond to version in file: %s",
                    name, bundleFileToReload.getVersion(), newBundleVersion));
        }

        synchronized (loadLock) {
            if (!localBundles.containsKey(name)) {
                throw new BundleReloadException(format("Bundle %s outside of distributive and could not be reloaded.", newBundleVersionName));
            }

            Bundle bundle = localBundles.get(name);
            if (bundle.getId().compareTo(newBundleVersionName) > 0) {
                throw new BundleReloadException(format("Bundle %s has incompatible version. Version has to be equals or higher a current one: %s.",
                        newBundleVersionName, bundle));
            }

            ApplicationResolver applicationResolver = new ApplicationResolver(System.getenv("APP_HOME"));
            ResolvedBundleArtifact resolvedBundleToReload = applicationResolver.resolveOutsideBundle(bundleFileToReload);

            //check services
            Set<ServiceDescriptor> lostServices = Sets.difference(bundle.getServices(), resolvedBundleToReload.getServices());
            if (!lostServices.isEmpty()) {
                throw new BundleReloadException(format("Bundle %s lost several services and couldn't be reloaded: %s",
                        newBundleVersionName, Sets.newHashSet(lostServices)));
            }

            //check api-common: have to be the same
            Set<ResolvedDependency> lostApiCommon = Sets.difference(bundle.getCurrentArtifact().getCommon(), resolvedBundleToReload.getCommon());
            if (!lostApiCommon.isEmpty()) {
                throw new BundleReloadException(format("Bundle %s lost several common api dependencies and couldn't be reloaded: %s",
                        newBundleVersionName, Sets.newHashSet(lostApiCommon)));
            }
            Set<ResolvedDependency> extraApiCommon = Sets.difference(resolvedBundleToReload.getCommon(), bundle.getCurrentArtifact().getCommon());
            if (!extraApiCommon.isEmpty()) {
                throw new BundleReloadException(format("Bundle %s introduce several common api dependencies and couldn't be reloaded: %s",
                        newBundleVersionName, Sets.newHashSet(extraApiCommon)));
            }

            //check ApiExport:
            //todo:

            //check ApiImport: все классы должны быть или же такими же или уже чем текущая актальная версия,
            // которая может быть перегруженной уже n раз
            //todo

            //check ImplExternal: similar with build process - they have to be with the same version with api-common
            Set<ResolvedDependency> allApiCommon = localBundles.values().stream()
                    .flatMap(bundle1 -> bundle1.getCurrentArtifact().getImplExternal().stream())
                    .collect(
                            () -> Sets.newTreeSet(ResolvedDependency.COMPARATOR),
                            TreeSet::add,
                            TreeSet::addAll
                    );
            for (ResolvedDependency extDep : resolvedBundleToReload.getImplExternal()) {
                Optional<ResolvedDependency> inApiCommonDep = allApiCommon.stream()
                        .filter(d -> Objects.equals(d.getGroup(), extDep.getGroup()) &&
                                Objects.equals(d.getName(), extDep.getName())).findAny();

                if (inApiCommonDep.isPresent() && !Objects.equals(inApiCommonDep.get().getVersion(), extDep.getVersion())) {
                    throw new BundleReloadException(format("Bundle %s has different version of external library " +
                                    "with global api-common, and couldn't be reloaded: %s, global api-common: %s",
                            newBundleVersionName,
                            Dependency.toString(extDep),
                            Dependency.toString(inApiCommonDep.get())
                    ));
                }
            }

            BundleCompiler bundleCompiler = new BundleCompiler(rootClassLoader);
            final ResolvedBundleArtifact patchedBundle;
            try {
                patchedBundle = bundleCompiler.compile(resolvedBundleToReload);
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