package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.ApplicationContext;
import com.cherkovskiy.application_context.api.Bundle;
import com.cherkovskiy.application_context.api.BundleVersion;
import com.cherkovskiy.application_context.api.ServiceDescriptor;
import com.cherkovskiy.application_context.api.configuration.ConfigurationContext;
import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class MonolithicApplicationContext implements ApplicationContext {
    //private final ConfigurationImpl globalConfiguration;
    @Nonnull
    private final Bundle appBundle;
    @Nonnull
    private final List<Bundle> localBundles;

    //TODO: This code move to bundleContainer.getCurrentBundle()
    //----------------
//    private final ConfigurationContext configurationContext;
//    private final ConfigurableConfiguration configurableConfiguration;
    //----------------
    //private final BundleContainer bundleContainer;

    MonolithicApplicationContext(@Nonnull Bundle appBundle, @Nonnull List<Bundle> localBundles) {
        this.appBundle = appBundle;
        this.localBundles = localBundles;
        //this.globalConfiguration = StandardConfiguration.create();
        //TODO: This code move to bundleContainer.getCurrentBundle()
        //----------------
//        this.configurationContext = new ConfigurationContextProxy(this.globalConfiguration);
//        this.configurableConfiguration = new ConfigurableConfigurationProxy(this.globalConfiguration);
        //----------------
        //this.bundleContainer = new BundleContainer(this.globalConfiguration);
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
}