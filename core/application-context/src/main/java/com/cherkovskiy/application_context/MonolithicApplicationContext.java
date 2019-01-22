package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.ApplicationContext;
import com.cherkovskiy.application_context.api.BundleVersion;
import com.cherkovskiy.application_context.api.configuration.ConfigurableConfiguration;
import com.cherkovskiy.application_context.api.configuration.ConfigurationContext;
import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;
import com.cherkovskiy.application_context.configuration.ConfigurableConfigurationProxy;
import com.cherkovskiy.application_context.configuration.ConfigurationContextProxy;
import com.cherkovskiy.application_context.configuration.environments.StandardConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MonolithicApplicationContext implements ApplicationContext {
    private final StandardConfiguration standardConfiguration;
    private final ConfigurationContext configurationContext;
    private final ConfigurableConfiguration configurableConfiguration;

    MonolithicApplicationContext() {
        this.standardConfiguration = new StandardConfiguration();
        this.configurationContext = new ConfigurationContextProxy(this.standardConfiguration);
        this.configurableConfiguration = new ConfigurableConfigurationProxy(this.standardConfiguration);
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
        return configurationContext;
    }
}