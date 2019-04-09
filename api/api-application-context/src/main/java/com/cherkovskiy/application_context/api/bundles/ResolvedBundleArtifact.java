package com.cherkovskiy.application_context.api.bundles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Comparator;
import java.util.Set;

public interface ResolvedBundleArtifact {

    @Nonnull
    String getName();

    @Nonnull
    String getVersion();

    /**
     * Used for runtime bundle reloading.
     *
     *
     * @return
     */
    int reloadNumber();

    boolean isEmbedded();

    @Nonnull
    File getFile();

    //Bundle can export only services from api dependencies without services from transitive these api dependencies
    @Nonnull
    Set<ServiceDescriptor> getServices();

    @Nullable
    String getStarter();

    @Nonnull
    Set<ResolvedDependency> getApiExport();

    @Nonnull
    Set<ResolvedDependency> getApiImport();

    @Nonnull
    Set<ResolvedDependency> getCommon();

    @Nonnull
    Set<ResolvedDependency> getImplExternal();

    @Nonnull
    Set<ResolvedDependency> getImplInternal();

    static String toString(ResolvedBundleArtifact artifact) {
        return String.join(":", artifact.getName(), artifact.getVersion());
    }

    Comparator<? super ResolvedBundleArtifact> COMPARATOR = Comparator.comparing(ResolvedBundleArtifact::getName)
            .thenComparing(Comparator.comparing(ResolvedBundleArtifact::getVersion));
}
