package com.cherkovskiy.application_starter;

import com.cherkovskiy.application_context.api.ApplicationRootClassLoader;
import com.cherkovskiy.application_context.api.ResolvedDependency;

import javax.annotation.Nonnull;
import java.util.Collection;

class ApplicationRootClassLoaderBridge implements ApplicationRootClassLoader {
    public ApplicationRootClassLoaderBridge(@Nonnull ApplicationRootClassLoaderSkeleton applicationRootClassLoaderSkeleton) {
        //todo
    }

    @Override
    public void updateClasses(@Nonnull Collection<ResolvedDependency> dependencies) {

    }

    @Nonnull
    @Override
    public ClassLoader getUnderlyingClassLoader() {
        return null;
    }
}
