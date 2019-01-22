package com.cherkovskiy.application_context.configuration.sources;

import com.cherkovskiy.application_context.api.configuration.sources.PropertiesSource;

import javax.annotation.Nonnull;


public abstract class AbstractPropertySource<T> implements PropertiesSource<T> {
    protected final String name;
    protected final T source;

    /**
     * Create a new {@code AbstractPropertySource} with the given name and source object.
     */
    public AbstractPropertySource(String name, T source) {
        this.name = name;
        this.source = source;
    }


    @SuppressWarnings("unchecked")
    public AbstractPropertySource(String name) {
        this(name, (T) new Object());
    }


    @Nonnull
    public String getName() {
        return this.name;
    }

    public T getSource() {
        return this.source;
    }

    public boolean containsProperty(@Nonnull String name) {
        return (getProperty(name) != null);
    }

    @Override
    public String toString() {
        return String.format("%s@%s [name='%s', properties=%s]",
                getClass().getSimpleName(), System.identityHashCode(this), this.name, this.source);
    }
}