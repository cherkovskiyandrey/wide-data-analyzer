package com.cherkovskiy.application_context.api;

import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ApplicationContext {

    @Nonnull
    <T> T getService(@Nonnull Class<T> clsToken) throws ServiceNotFoundException;

    @Nonnull
    <T> T getService(@Nonnull Class<T> clsToken, @Nonnull String serviceName) throws ServiceNotFoundException;

    @Nonnull
    <T> T getService(@Nonnull Class<T> clsToken, @Nonnull String bundleName, @Nullable BundleVersion bundleVersion) throws ServiceNotFoundException;

    @Nonnull
    <T> T getService(@Nonnull Class<T> clsToken, @Nonnull String serviceName, @Nonnull String bundleName, @Nullable BundleVersion bundleVersion) throws ServiceNotFoundException;
}
