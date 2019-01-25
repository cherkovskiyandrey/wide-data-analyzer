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

public class ConfigurationImpl implements ConfigurableConfiguration, ObjectMappingConfiguration {

    private final MutableResource<PropertiesSource<?>> globalPropertySources;
    private final MutableResource<ConverterService> globalConverterServices;
    private final MutableResource<PropertiesSource<?>> propertySources;
    private final MutableResource<ConverterService> converterServices;
    private final PropertySourcesPropertyResolver propertyResolver;


    public ConfigurationImpl() {
        this.globalPropertySources = new MutableResourcesImpl<>();
        this.globalConverterServices = new MutableResourcesImpl<>();
        this.propertySources = new MutableResourcesImpl<>();
        this.converterServices = new MutableResourcesImpl<>();
        this.globalConverterServices.addFirst(new StandardConverterService());
        this.propertyResolver = new PropertySourcesPropertyResolver(
                this.globalPropertySources,
                this.globalConverterServices,
                this.propertySources,
                this.converterServices,
                new SimpleObjectMapper()
        );
    }

    public ConfigurationImpl(
            @Nonnull MutableResource<PropertiesSource<?>> globalPropertySources,
            @Nonnull MutableResource<ConverterService> globalConverterServices,
            @Nonnull MutableResource<PropertiesSource<?>> propertySources,
            @Nonnull MutableResource<ConverterService> converterServices) {
        this.globalPropertySources = globalPropertySources;
        this.globalConverterServices = globalConverterServices;
        this.propertySources = propertySources;
        this.converterServices = converterServices;
        this.propertyResolver = new PropertySourcesPropertyResolver(
                this.globalPropertySources,
                this.globalConverterServices,
                this.propertySources,
                this.converterServices,
                new SimpleObjectMapper()
        );
    }

    @Nonnull
    @Override
    public MutableResource<PropertiesSource<?>> getGlobalPropertySources() {
        return globalPropertySources;
    }

    @Nonnull
    @Override
    public MutableResource<ConverterService> getGlobalConverterServices() {
        return globalConverterServices;
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

    @Nonnull
    @Override
    public String getProperty(@Nonnull String key, @Nonnull String defaultValue) {
        return propertyResolver.getProperty(key, defaultValue);
    }

    @Override
    public <T> T getProperty(@Nonnull String key, @Nonnull Class<T> targetType) {
        return propertyResolver.getProperty(key, targetType);
    }

    @Nonnull
    @Override
    public <T> T getProperty(@Nonnull String key, @Nonnull Class<T> targetType, @Nonnull T defaultValue) {
        return propertyResolver.getProperty(key, targetType, defaultValue);
    }

    @Nonnull
    @Override
    public String getRequiredProperty(@Nonnull String key) throws IllegalStateException {
        return propertyResolver.getRequiredProperty(key);
    }

    @Nonnull
    @Override
    public <T> T getRequiredProperty(@Nonnull String key, @Nonnull Class<T> targetType) throws IllegalStateException {
        return propertyResolver.getRequiredProperty(key, targetType);
    }


    @Override
    public <T> T resolvePropertyClass(Class<T> token) throws IllegalStateException {
        return propertyResolver.resolvePropertyClass(token);
    }
}
