package com.cherkovskiy.application_context.api.configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Interface for resolving properties against any underlying source.
 *
 * @author Cherkovskiy Andrey
 */
public interface Configuration {


    /**
     * Return whether the given property key is available for resolution,
     * i.e. if the value for the given key is not {@code null}.
     */
    boolean containsProperty(@Nonnull String key);

    /**
     * Return the property value associated with the given key,
     * or {@code null} if the key cannot be resolved.
     *
     * @param key the property name to resolve
     */
    @Nullable
    String getProperty(@Nonnull String key);

    /**
     * Return the property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     *
     * @param key          the property name to resolve
     * @param defaultValue the default value to return if no value is found
     */
    @Nonnull
    String getProperty(@Nonnull String key, @Nonnull String defaultValue);


    /**
     * Return the property value associated with the given key.
     *
     * @throws IllegalStateException if the key cannot be resolved
     */
    @Nonnull
    String getRequiredProperty(@Nonnull String key) throws IllegalStateException;


    /**
     * Return the property value associated with the given key,
     * or {@code null} if the key cannot be resolved.
     *
     * @param key        the property name to resolve
     * @param targetType the expected type of the property value
     */
    @Nullable
    <T> T getProperty(@Nonnull String key, @Nonnull Class<T> targetType);


    /**
     * Return the property value associated with the given key,
     * or {@code defaultValue} if the key cannot be resolved.
     *
     * @param key          the property name to resolve
     * @param targetType   the expected type of the property value
     * @param defaultValue the default value to return if no value is found
     */
    @Nonnull
    <T> T getProperty(@Nonnull String key, @Nonnull Class<T> targetType, @Nonnull T defaultValue);


    /**
     * Return the property value associated with the given key, converted to the given
     * targetType (never {@code null}).
     *
     * @throws IllegalStateException if the given key cannot be resolved
     */
    @Nonnull
    <T> T getRequiredProperty(@Nonnull String key, @Nonnull Class<T> targetType) throws IllegalStateException;
}
