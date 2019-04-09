package com.cherkovskiy.application_context.api;

import com.cherkovskiy.application_context.api.bundles.ResolvedDependency;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface ClassesProvider {

    /**
     * Return artifact where provided class is located.
     *
     * @param className
     * @return
     */
    @Nullable
    ResolvedDependency getArtifactByClass(@Nonnull String className);

    /**
     * Return all loaded versions of current class.
     * Used usually for hot-bundle-reloading.
     * Example: className = "com.cherkovskiy.A" => return {"com.cherkovskiy.A_generated_v1", "com.cherkovskiy.A_generated_v2"}
     *
     * @param className
     * @return
     */
    @Nonnull
    Collection<String> getAllVersionOfClass(@Nonnull String className);
}
