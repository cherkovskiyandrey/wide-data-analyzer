package com.cherkovskiy.application_context.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Comparator;
import java.util.Set;

public interface BundleArtifact {

    String getName();

    String getVersion();

    boolean isEmbedded();

    @Nonnull
    File getFile();

    @Nonnull
    Set<ServiceDescriptor> getServices();

    @Nullable
    String getStarter();

    @Nonnull
    Set<Dependency> getApiExport();

    @Nonnull
    Set<Dependency> getApiImport();

    @Nonnull
    Set<Dependency> getCommon();

    @Nonnull
    Set<Dependency> getImplExternal();

    @Nonnull
    Set<Dependency> getImplInternal();

    Comparator<? super BundleArtifact> COMPARATOR = Comparator.comparing(BundleArtifact::getName)
            .thenComparing(Comparator.comparing(BundleArtifact::getVersion));
}
