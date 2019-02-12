package com.cherkovskiy.application_context.api;

import com.cherkovskiy.application_context.MonolithicApplicationContext;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface ContextBuilder {

    @Nonnull
    ContextBuilder setArguments(String[] args);

    @Nonnull
    ContextBuilder setRootClassLoader(@Nonnull ApplicationRootClassLoader contextClassLoader);

    @Nonnull
    MonolithicApplicationContext build() throws IOException;


}
