package com.cherkovskiy.gradle.plugin.api;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Comparator;
import java.util.Set;

//todo: move to application-context (?)
public interface BundleArtifact {

    String getName();

    String getVersion();

    boolean isEmbedded();

    @Nonnull
    File getFile();

    @Nonnull
    Set<ServiceDescriptor> getServices();

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
