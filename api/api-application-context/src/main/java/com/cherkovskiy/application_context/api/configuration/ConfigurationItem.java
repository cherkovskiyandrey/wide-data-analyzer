package com.cherkovskiy.application_context.api.configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ConfigurationItem<T> {

    @Nullable
    T getValue();

    @Nonnull
    T getValueOr(@Nonnull T defaultValue);

    boolean hasDefaultValue();

    @Nonnull
    T getDefaultValue();

    @Nonnull
    String getPath();
}
