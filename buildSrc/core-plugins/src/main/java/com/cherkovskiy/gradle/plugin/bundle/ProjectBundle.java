package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.gradle.plugin.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectBundle implements ResolvedBundleArtifact {
    private final File archivePath;
    private final String name;
    private final String version;
    private final boolean embeddedDependencies;
    private final List<DependencyHolder> runtimeConfDependencies;
    private final List<DependencyHolder> apiConfDependencies;

    public ProjectBundle(File archivePath,
                         String name,
                         String version,
                         boolean embeddedDependencies,
                         List<DependencyHolder> runtimeConfDependencies,
                         List<DependencyHolder> apiConfDependencies) {
        this.archivePath = archivePath;
        this.name = name;
        this.version = version;
        this.embeddedDependencies = embeddedDependencies;
        this.runtimeConfDependencies = runtimeConfDependencies;
        this.apiConfDependencies = apiConfDependencies;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isEmbedded() {
        return embeddedDependencies;
    }

    @Nonnull
    @Override
    public File getFile() {
        return archivePath;
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getApiExport() {
        return runtimeConfDependencies.stream()
                .filter(dep -> dep.isNative() &&
                        SubProjectTypes.API == dep.getSubProjectType() &&
                        existsInApiConfig(dep)
                )
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    private boolean existsInApiConfig(DependencyHolder dependencyHolder) {
        return apiConfDependencies.contains(dependencyHolder);
    }

    public boolean isApiExport(DependencyHolder dependencyHolder) {
        return getApiExport().contains(dependencyHolder);
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getApiImport() {
        return runtimeConfDependencies.stream()
                .filter(dep -> dep.isNative() &&
                        SubProjectTypes.API == dep.getSubProjectType() &&
                        !existsInApiConfig(dep)
                )
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    public boolean isApiImport(DependencyHolder dependencyHolder) {
        return getApiImport().contains(dependencyHolder);
    }

    //TODO: точно ли будет работать, учитывая схлопывания????
    @Nonnull
    @Override
    public Set<ResolvedDependency> getCommon() {
        return runtimeConfDependencies.stream()
                .filter(dep -> {
                    if (!dep.isNative()) {
                        for (; dep != null; dep = dep.getParent().orElse(null)) {
                            if (dep.isNative() && SubProjectTypes.API == dep.getSubProjectType()) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    public boolean isCommon(DependencyHolder dependencyHolder) {
        return getCommon().contains(dependencyHolder);
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getImplExternal() {
        final Set<ResolvedDependency> common = getCommon();
        return runtimeConfDependencies.stream()
                .filter(dep -> !dep.isNative())
                .filter(dep -> !common.contains(dep))
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }


    public boolean isImplExternal(DependencyHolder dependencyHolder) {
        return getImplExternal().contains(dependencyHolder);
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getImplInternal() {
        return runtimeConfDependencies.stream()
                .filter(DependencyHolder::isNative)
                .filter(dep -> SubProjectTypes.API != dep.getSubProjectType())
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    public boolean isInternalImpl(DependencyHolder dependencyHolder) {
        return getImplInternal().contains(dependencyHolder);
    }

    public List<DependencyHolder> getAll() {
        return Lists.newArrayList(runtimeConfDependencies);
    }

    @Override
    public String toString() {
        return "ProjectBundle{" +
                "archivePath=" + archivePath +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", embeddedDependencies=" + embeddedDependencies +
                '}';
    }
}
