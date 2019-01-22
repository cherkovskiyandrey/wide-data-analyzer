package com.cherkovskiy.application_context.configuration;


import com.cherkovskiy.application_context.api.configuration.Configuration;
import com.cherkovskiy.application_context.api.configuration.annotations.ConfigurationProperties;
import com.cherkovskiy.application_context.api.configuration.annotations.ConfigurationValue;
import com.cherkovskiy.application_context.api.configuration.annotations.NestedConfigurationProperties;

public interface ObjectMappingConfiguration extends Configuration {


    /**
     * Method try to build object by token class.
     * Use annotations:
     * <ol>
     * <li>{@link ConfigurationProperties}</li>
     * <li>{@link NestedConfigurationProperties}</li>
     * <li>{@link ConfigurationValue}</li>
     * </ol>
     * <p>
     *
     * @param token
     * @param <T>
     * @return
     * @throws IllegalStateException
     */
    <T> T resolvePropertyClass(Class<T> token) throws IllegalStateException;

}
