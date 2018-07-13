package com.cherkovskiy.application_context.api.configuration;

public interface ConfigurationContext {

    <T> T getOrResolve(Class<T> configurationClass);

}
