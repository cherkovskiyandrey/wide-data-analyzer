package com.cherkovskiy.application_starter;

import com.cherkovskiy.application_context.api.ApplicationRootClassLoader;
import com.cherkovskiy.application_context.api.bundles.ResolvedDependency;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nullable
    @Override
    public ResolvedDependency getArtifactByClass(@Nonnull String className) {
        //todo
    }

    @Nonnull
    @Override
    public Collection<String> getAllVersionOfClass(@Nonnull String className) {
        //todo
    }
}
