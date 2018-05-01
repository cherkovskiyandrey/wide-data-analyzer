package com.cherkovskiy.gradle.plugin.api;

public interface BundleResolver {

    ResolvedBundleArtifact resolve(BundleArtifact artifact);
}
