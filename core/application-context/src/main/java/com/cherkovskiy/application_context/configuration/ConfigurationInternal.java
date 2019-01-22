package com.cherkovskiy.application_context.configuration;

import com.cherkovskiy.application_context.api.configuration.Configuration;

import java.util.Set;

public interface ConfigurationInternal extends Configuration {

    /**
     * Return the property value associated with the given key,
     * or {@code defaultValue} if the key cannot be resolved.
     * DefaultValue in raw form.
     *
     * @param key          the property name to resolve
     * @param targetType   the expected type of the property value
     * @param defaultValue the raw default value to convert and to return if no value is found
     */
    <T> T getPropertyWithRawDefault(String key, Class<T> targetType, Object defaultValue);

    /**
     * Return the property value associated with the given key,
     * or {@code null} if the key cannot be resolved.
     *
     * @param key          the property name to resolve
     * @param defaultValue the raw default value to return if no value is found
     * @return the raw value without convertion
     */
    Object getRawProperty(String key, String defaultValue);

    /**
     * Return the property value associated with the given key (never {@code null}).
     *
     * @throws IllegalStateException if the given key cannot be resolved
     */
    Object getRawRequiredProperty(String key) throws IllegalStateException;

    /**
     * Return all indexes for list of elements by prefix.
     * <p>
     * Example:
     * <p>
     * {@code some.properties.listStrings[default] = 0;} <br>
     * {@code some.properties.listStrings[0] = 1;} <br>
     * {@code some.properties.listStrings[10] = 2;} <br>
     * {@code some.properties.listStrings[20] = 3;} <br>
     * <p>
     * <br>
     * return list of next elements: default, 0, 10, 20
     *
     * @param fieldKeyBase
     * @return
     */
    Set<String> getIndexesByPrefix(String fieldKeyBase);
}
