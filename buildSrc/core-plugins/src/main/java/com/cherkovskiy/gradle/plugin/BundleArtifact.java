package com.cherkovskiy.gradle.plugin;

import java.util.Set;

//todo: move to application-context (?)
public interface BundleArtifact extends Artifact {

    boolean isEmbedded();

    Set<Artifact> getApiExport();

    Set<Artifact> getApiImport();

    Set<Artifact> getCommon();

    Set<Artifact> getImplExternal();

    Set<Artifact> getImplInternal();
}
