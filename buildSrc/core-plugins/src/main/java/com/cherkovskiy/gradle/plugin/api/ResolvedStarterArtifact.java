package com.cherkovskiy.gradle.plugin.api;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Set;

public interface ResolvedStarterArtifact extends ResolvedDependency {

    @Nonnull
    Set<ResolvedDependency> getApi();

    @Nonnull
    Set<ResolvedDependency> getCommon(); //TODO: rename because name is confused with common in SubProjectTypes

    @Nonnull
    Set<ResolvedDependency> get3rdParty();

    @Nonnull
    Set<ResolvedDependency> getInternal();

}
