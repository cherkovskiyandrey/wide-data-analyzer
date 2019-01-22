package com.cherkovskiy.application_context.configuration.environments;

import com.cherkovskiy.application_context.api.configuration.resources.MutableResource;
import com.cherkovskiy.application_context.api.configuration.sources.PropertiesSource;
import com.cherkovskiy.application_context.api.configuration.ConfigurableConfiguration;
import com.cherkovskiy.application_context.configuration.ObjectMappingConfiguration;
import com.cherkovskiy.application_context.configuration.PropertySourcesPropertyResolver;
import com.cherkovskiy.application_context.api.configuration.convertors.ConverterService;
import com.cherkovskiy.application_context.configuration.convertors.StandardConverterService;
import com.cherkovskiy.application_context.configuration.mappers.simple.SimpleObjectMapper;
import com.cherkovskiy.application_context.configuration.resources.MutableResourcesImpl;

import javax.annotation.Nonnull;

public abstract class AbstractConfiguration implements ConfigurableConfiguration, ObjectMappingConfiguration {

    protected final MutableResource<PropertiesSource<?>> propertySources;
    protected final MutableResource<ConverterService> converterServices;
    protected final PropertySourcesPropertyResolver propertyResolver;


    public AbstractConfiguration() {
        this.propertySources = new MutableResourcesImpl<>();
        this.converterServices = new MutableResourcesImpl<>();
        this.converterServices.addFirst(new StandardConverterService());
        this.propertyResolver = new PropertySourcesPropertyResolver(
                this.propertySources,
                this.converterServices,
                new SimpleObjectMapper()
        );
    }

    @Nonnull
    @Override
    public MutableResource<PropertiesSource<?>> getPropertySources() {
        return propertySources;
    }

    @Nonnull
    @Override
    public MutableResource<ConverterService> getConverterServices() {
        return converterServices;
    }

    @Override
    public boolean containsProperty(@Nonnull String key) {
        return propertyResolver.containsProperty(key);
    }

    @Override
    public String getProperty(@Nonnull String key) {
        return propertyResolver.getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return propertyResolver.getProperty(key, defaultValue);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return propertyResolver.getProperty(key, targetType);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return propertyResolver.getProperty(key, targetType, defaultValue);
    }

    @Nonnull
    @Override
    public String getRequiredProperty(@Nonnull String key) throws IllegalStateException {
        return propertyResolver.getRequiredProperty(key);
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return propertyResolver.getRequiredProperty(key, targetType);
    }


    @Override
    public <T> T resolvePropertyClass(Class<T> token) throws IllegalStateException {
        return propertyResolver.resolvePropertyClass(token);
    }
}
