package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.ApplicationContext;
import com.cherkovskiy.application_context.api.BundleVersion;
import com.cherkovskiy.application_context.api.configuration.ConfigurationContext;
import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MonolithicApplicationContext implements ApplicationContext {


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

    @Override
    public ConfigurationContext getConfigurationContext() {
        throw new UnsupportedOperationException("It is not supported yet.");
    }
}