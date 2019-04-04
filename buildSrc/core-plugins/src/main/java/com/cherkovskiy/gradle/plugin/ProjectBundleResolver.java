package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.application_context.BundleResolver;
import com.cherkovskiy.application_context.ResolvedBundleFile;
import com.cherkovskiy.application_context.api.bundles.BundleArtifact;
import com.cherkovskiy.application_context.api.bundles.Dependency;
import com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.api.bundles.ResolvedDependency;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class ProjectBundleResolver implements BundleResolver {

    private final List<ResolvedDependency> resolvedDependencies;

    public ProjectBundleResolver(List<ResolvedDependency> resolvedDependencies) {
        this.resolvedDependencies = resolvedDependencies;
    }

    @Override
    @Nonnull
    public ResolvedBundleArtifact resolve(@Nonnull BundleArtifact artifact) {
        return ResolvedBundleFile.builder()
                .setName(artifact.getName())
                .setVersion(artifact.getVersion())
                .setIsEmbedded(artifact.isEmbedded())
                .setFile(artifact.getFile())
                .setServices(artifact.getServices())
                .setApiExport(resolveDependency(artifact.getApiExport()))
                .setApiImport(resolveDependency(artifact.getApiImport()))
                .setCommon(resolveDependency(artifact.getCommon()))
                .setImplExternal(resolveDependency(artifact.getImplExternal()))
                .setImplInternal(resolveDependency(artifact.getImplInternal()))
                .build();
    }

    private Set<ResolvedDependency> resolveDependency(Set<Dependency> dependencies) {
        return dependencies.stream()
                .map(dependency -> resolvedDependencies.stream()
                        .filter(resolvedDependency -> Dependency.COMPARATOR.compare(dependency, resolvedDependency) == 0)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(format("Could not resolve dependency %s", Dependency.toString(dependency)))))
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }
}
