package com.cherkovskiy.application_context.api;

import com.cherkovskiy.application_context.api.bundles.ResolvedDependency;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface ApplicationRootClassLoader extends ClassesProvider {

    /**
     * For external jars - just check version - it prohibited to change.
     * For internal - check all classes - only extended for interfaces is permitted.
     *
     * @param dependencies
     */
    void updateClasses(@Nonnull Collection<ResolvedDependency> dependencies);

    /**
     *
     * @return underlying class loader
     */
    @Nonnull
    ClassLoader getUnderlyingClassLoader();
}
