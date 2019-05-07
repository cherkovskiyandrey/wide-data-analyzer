package com.cherkovskiy.application_context.api;

import com.cherkovskiy.application_context.BundleVersionName;
import com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.api.bundles.ServiceDescriptor;
import com.cherkovskiy.application_context.api.exceptions.BundleReloadException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public interface Bundle {

    BundleVersionName getId();

    @Nonnull
    ResolvedBundleArtifact getCurrentArtifact();

    /**
     * Load bundle
     */
    void load();

    /**
     * Reload
     */
    void reload(@Nonnull ResolvedBundleArtifact resolvedBundleArtifact) throws BundleReloadException;

    /**
     * Unload
     */
    void unload();

    boolean isLoaded();

    /**
     * @return true for remote bundles and false for local
     */
    boolean isRemote();

    /**
     * @return implemented services in this bundle if it is local bundle and null for remote
     */
    @Nonnull
    Set<ServiceDescriptor> getServices();

}
