package com.cherkovskiy.application_context.api.configuration.sources;

import com.cherkovskiy.application_context.api.configuration.resources.NamedResource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public interface PropertiesSource<T> extends NamedResource {
    @Nonnull
    String getName();

    /**
     * Return the underlying source object for this {@code PropertySource}.
     */
    T getSource();

    /**
     * Return whether this {@code PropertySource} contains the given name.
     * <p>This implementation simply checks for a {@code null} return value
     * from {@link #getProperty(String)}. Subclasses may wish to implement
     * a more efficient algorithm if possible.
     *
     * @param name the property name to find
     */
    boolean containsProperty(@Nonnull String name);

    /**
     * Return the value associated with the given name,
     * or {@code null} if not found.
     *
     * @param name the property to find
     */
    @Nullable
    Object getProperty(@Nonnull String name);


    /**
     * Return all names of known properties.
     *
     * @return
     */
    @Nonnull
    Set<String> getAllPropertiesKeys();
}
