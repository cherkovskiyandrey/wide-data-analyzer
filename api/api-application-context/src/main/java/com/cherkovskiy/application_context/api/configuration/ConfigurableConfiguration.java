package com.cherkovskiy.application_context.api.configuration;

import com.cherkovskiy.application_context.api.configuration.convertors.ConverterService;
import com.cherkovskiy.application_context.api.configuration.resources.MutableResource;
import com.cherkovskiy.application_context.api.configuration.sources.PropertiesSource;

import javax.annotation.Nonnull;

public interface ConfigurableConfiguration {

    @Nonnull
    MutableResource<PropertiesSource<?>> getPropertySources();

    @Nonnull
    MutableResource<ConverterService> getConverterServices();
}

