package com.cherkovskiy.gradle.plugin.api;

import java.util.Comparator;
import java.util.Set;

//todo: move to application-context (?)
public interface BundleArtifact {

    String getName();

    String getVersion();

    boolean isEmbedded();

    Set<ServiceDescriptor> getServices();

    Set<Dependency> getApiExport();

    Set<Dependency> getApiImport();

    Set<Dependency> getCommon();

    Set<Dependency> getImplExternal();

    Set<Dependency> getImplInternal();

    Comparator<? super BundleArtifact> COMPARATOR = Comparator.comparing(BundleArtifact::getName)
            .thenComparing(Comparator.comparing(BundleArtifact::getVersion));
}
