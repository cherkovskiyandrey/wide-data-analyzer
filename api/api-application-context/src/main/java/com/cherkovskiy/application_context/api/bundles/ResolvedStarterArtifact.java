package com.cherkovskiy.application_context.api.bundles;

import com.cherkovskiy.application_context.api.bundles.ResolvedDependency;

import javax.annotation.Nonnull;
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
