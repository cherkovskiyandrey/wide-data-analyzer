package com.cherkovskiy.application_context.api;

import com.cherkovskiy.application_context.MonolithicApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public interface ContextBuilder {

    @Nonnull
    ContextBuilder setArguments(@Nonnull String[] args);

    @Nonnull
    ContextBuilder setRootClassLoader(@Nonnull ApplicationRootClassLoader contextClassLoader);

    @Nonnull
    ContextBuilder setBundleManagerProvider(@Nullable BundleManagerProvider bundleManagerProvider);

    @Nonnull
    MonolithicApplicationContext build() throws IOException;


}
