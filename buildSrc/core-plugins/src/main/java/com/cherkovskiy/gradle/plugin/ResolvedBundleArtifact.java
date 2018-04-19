package com.cherkovskiy.gradle.plugin;

import java.util.Set;

public interface ResolvedBundleArtifact extends ResolvedArtifact {

    boolean isEmbedded();

    Set<ResolvedArtifact> getApiExport();

    Set<ResolvedArtifact> getApiImport();

    Set<ResolvedArtifact> getCommon();

    Set<ResolvedArtifact> getImplExternal();

    Set<ResolvedArtifact> getImplInternal();
}
