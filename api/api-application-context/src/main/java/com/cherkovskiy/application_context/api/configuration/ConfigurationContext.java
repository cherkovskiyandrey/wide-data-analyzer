package com.cherkovskiy.application_context.api.configuration;

import com.cherkovskiy.application_context.api.configuration.annotations.ConfigurationValue;
import com.cherkovskiy.application_context.api.configuration.annotations.NestedConfigurationProperties;

import javax.annotation.Nonnull;

public interface ConfigurationContext extends Configuration {

    /**
     * Resolve class as configuration bean. Fill all fields
     * annotated by means of {@link ConfigurationValue} or {@link NestedConfigurationProperties}.
     *
     * @param configurationClass
     * @param <T>
     * @return
     */
    @Nonnull
    <T> T getOrResolve(@Nonnull Class<T> configurationClass) throws IllegalStateException;

}
