package com.cherkovskiy.application_starter;

import com.cherkovskiy.application_context.api.ApplicationRootClassLoader;
import com.cherkovskiy.application_context.api.bundles.ResolvedDependency;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

class ApplicationRootClassLoaderBridge implements ApplicationRootClassLoader {
    public ApplicationRootClassLoaderBridge(@Nonnull ApplicationRootClassLoaderSkeleton applicationRootClassLoaderSkeleton) {
        //todo
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Override
    public void updateClasses(@Nonnull Collection<ResolvedDependency> dependencies) {
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Nonnull
    @Override
    public ClassLoader getUnderlyingClassLoader() {
        return null;
    }

    @Nullable
    @Override
    public ResolvedDependency getArtifactByClass(@Nonnull String className) {
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Nonnull
    @Override
    public Collection<String> getAllVersionOfClass(@Nonnull String className) {
        throw new UnsupportedOperationException("It is not supported yet.");
    }
}
