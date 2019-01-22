package com.cherkovskiy.application_context.api.configuration.resources;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MutableResource<T extends NamedResource> extends Iterable<T> {

    boolean contains(@Nonnull String name);

    T get(@Nonnull String name);

    /**
     * Add the given property source object with highest precedence.
     */
    void addFirst(@Nonnull T resource);

    /**
     * Add the given property source object with lowest precedence.
     */
    void addLast(@Nonnull T resource);

    /**
     * Add the given property source object with precedence immediately higher
     * than the named relative property source.
     */
    void addBefore(@Nonnull String relativePropertySourceName, @Nonnull T resource);

    /**
     * Add the given property source object with precedence immediately lower
     * than the named relative property source.
     */
    void addAfter(@Nonnull String relativePropertySourceName, @Nonnull T resource);

    /**
     * Remove and return the property source with the given name, {@code null} if not found.
     *
     * @param name the name of the property source to find and remove
     */
    @Nullable
    T remove(@Nonnull String name);

    /**
     * Replace the property source with the given name with the given property source object.
     *
     * @param name     the name of the property source to find and replace
     * @param resource the replacement property source
     * @throws IllegalArgumentException if no property source with the given name is present
     * @see #contains
     */
    void replace(@Nonnull String name, @Nonnull T resource);

    /**
     * Return the number of {@link NamedResource} objects contained.
     */
    int size();
}
