package com.cherkovskiy.application_context.api.configuration;

import com.cherkovskiy.application_context.api.configuration.convertors.ConverterService;
import com.cherkovskiy.application_context.api.configuration.resources.MutableResource;
import com.cherkovskiy.application_context.api.configuration.sources.PropertiesSource;

import javax.annotation.Nonnull;

public interface ConfigurableConfiguration {

    /**
     * Global sources for current jvm process.
     *
     * @return
     */
    @Nonnull
    MutableResource<PropertiesSource<?>> getGlobalPropertySources();

    /**
     * Global sources for current jvm process.
     *
     * @return
     */
    @Nonnull
    MutableResource<ConverterService> getGlobalConverterServices();

    /**
     * Sources for current bean. Other bean could't see this.
     *
     * @return
     */
    @Nonnull
    MutableResource<PropertiesSource<?>> getPropertySources();

    /**
     * Sources for current bean. Other bean could't see this.
     *
     * @return
     */
    @Nonnull
    MutableResource<ConverterService> getConverterServices();
}

