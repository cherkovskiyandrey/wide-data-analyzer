package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.bundles.BundleArtifact;
import com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact;

import javax.annotation.Nonnull;

public interface BundleResolver {

    @Nonnull
    ResolvedBundleArtifact resolve(@Nonnull BundleArtifact artifact);
}
