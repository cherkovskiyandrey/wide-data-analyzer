package com.cherkovskiy.application_context.configuration;

import com.cherkovskiy.application_context.api.configuration.ConfigurationContext;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentMap;

public class ConfigurationContextProxy implements ConfigurationContext {
    private final ConcurrentMap<Class<?>, Object> configCache = Maps.newConcurrentMap();

    @Nonnull
    private final ObjectMappingConfiguration objectMappingConfiguration;

    public ConfigurationContextProxy(@Nonnull ObjectMappingConfiguration objectMappingConfiguration) {
        this.objectMappingConfiguration = objectMappingConfiguration;
    }

    @Override
    public boolean containsProperty(@Nonnull String key) {
        return objectMappingConfiguration.containsProperty(key);
    }

    @Override
    public String getProperty(@Nonnull String key) {
        return objectMappingConfiguration.getProperty(key);
    }

    @Nonnull
    @Override
    public String getProperty(@Nonnull String key, @Nonnull String defaultValue) {
        return objectMappingConfiguration.getProperty(key, defaultValue);
    }

    @Nonnull
    @Override
    public String getRequiredProperty(@Nonnull String key) throws IllegalStateException {
        return objectMappingConfiguration.getRequiredProperty(key);
    }

    @Override
    public <T> T getProperty(@Nonnull String key, @Nonnull Class<T> targetType) {
        return objectMappingConfiguration.getProperty(key, targetType);
    }

    @Nonnull
    @Override
    public <T> T getProperty(@Nonnull String key, @Nonnull Class<T> targetType, @Nonnull T defaultValue) {
        return objectMappingConfiguration.getProperty(key, targetType, defaultValue);
    }

    @Nonnull
    @Override
    public <T> T getRequiredProperty(@Nonnull String key, @Nonnull Class<T> targetType) throws IllegalStateException {
        return objectMappingConfiguration.getRequiredProperty(key, targetType);
    }

    @Nonnull
    @Override
    public <T> T getOrResolve(@Nonnull Class<T> configurationClass) {
        return configurationClass.cast(
                configCache.computeIfAbsent(configurationClass, objectMappingConfiguration::resolvePropertyClass)
        );
    }

    public void refresh() {
        configCache.keySet().forEach(cls -> configCache.put(cls, objectMappingConfiguration.resolvePropertyClass(cls)));
    }
}
