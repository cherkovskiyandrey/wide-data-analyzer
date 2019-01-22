package com.cherkovskiy.application_context.configuration;

import com.cherkovskiy.application_context.api.configuration.ConfigurableConfiguration;
import com.cherkovskiy.application_context.api.configuration.convertors.ConverterService;
import com.cherkovskiy.application_context.api.configuration.resources.MutableResource;
import com.cherkovskiy.application_context.api.configuration.sources.PropertiesSource;

import javax.annotation.Nonnull;

public class ConfigurableConfigurationProxy implements ConfigurableConfiguration {
    @Nonnull
    private final ConfigurableConfiguration configurableConfiguration;

    public ConfigurableConfigurationProxy(@Nonnull ConfigurableConfiguration configurableConfiguration) {
        this.configurableConfiguration = configurableConfiguration;
    }

    @Nonnull
    @Override
    public MutableResource<PropertiesSource<?>> getPropertySources() {
        return configurableConfiguration.getPropertySources();
    }

    @Nonnull
    @Override
    public MutableResource<ConverterService> getConverterServices() {
        return configurableConfiguration.getConverterServices();
    }
}
