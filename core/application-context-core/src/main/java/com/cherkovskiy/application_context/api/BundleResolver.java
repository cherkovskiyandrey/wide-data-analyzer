package com.cherkovskiy.application_context.api;

public interface BundleResolver {

    ResolvedBundleArtifact resolve(BundleArtifact artifact);
}
