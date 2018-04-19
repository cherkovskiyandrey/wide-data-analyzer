package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.gradle.plugin.DependencyHolder;
import com.cherkovskiy.gradle.plugin.ResolvedArtifact;
import com.cherkovskiy.gradle.plugin.ResolvedBundleArtifact;
import com.cherkovskiy.gradle.plugin.SubProjectTypes;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class ProjectBundle implements ResolvedBundleArtifact {
    private final File archivePath;
    private final String group;
    private final String name;
    private final String version;
    private final boolean embeddedDependencies;
    private final List<DependencyHolder> runtimeConfDependencies;
    private final List<DependencyHolder> apiConfDependencies;

    public ProjectBundle(File archivePath,
                         String group,
                         String name,
                         String version,
                         boolean embeddedDependencies,
                         List<DependencyHolder> runtimeConfDependencies,
                         List<DependencyHolder> apiConfDependencies) {
        this.archivePath = archivePath;
        this.group = group;
        this.name = name;
        this.version = version;
        this.embeddedDependencies = embeddedDependencies;
        this.runtimeConfDependencies = runtimeConfDependencies;
        this.apiConfDependencies = apiConfDependencies;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getArtifactFileName() {
        return archivePath.getName();
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return FileUtils.openInputStream(archivePath);
    }

    @Override
    public boolean isEmbedded() {
        return embeddedDependencies;
    }

    @Override
    public Set<ResolvedArtifact> getApiExport() {
        return runtimeConfDependencies.stream()
                .filter(dep -> dep.isNative() &&
                        SubProjectTypes.API == dep.getSubProjectType() &&
                        existsInApiConfig(dep)
                )
                //todo: reduce to set by custom comparator
                .collect(Collectors.toSet());
    }

    private boolean existsInApiConfig(DependencyHolder dependencyHolder) {
        return apiConfDependencies.stream().anyMatch(dependencyHolder::isSame);
    }

    public boolean isApiExport(DependencyHolder dependencyHolder) {
        return getApiExport().stream().anyMatch(dependencyHolder::isSame);
    }

    @Override
    public Set<ResolvedArtifact> getApiImport() {
        return runtimeConfDependencies.stream()
                .filter(dep -> dep.isNative() &&
                        SubProjectTypes.API == dep.getSubProjectType() &&
                        !existsInApiConfig(dep)
                )
                //todo: reduce to set by custom comparator
                .collect(toSet());
    }

    public boolean isApiImport(DependencyHolder dependencyHolder) {
        return getApiImport().stream().anyMatch(dependencyHolder::isSame);
    }

    @Override
    public Set<ResolvedArtifact> getCommon() {
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
                .collect(toSet());
    }

    public boolean isCommon(DependencyHolder dependencyHolder) {
        return getCommon().stream().anyMatch(dependencyHolder::isSame);
    }

    @Override
    public Set<ResolvedArtifact> getImplExternal() {
        final Set<ResolvedArtifact> common = getCommon();
        return runtimeConfDependencies.stream()
                .filter(dep -> !dep.isNative())
                .filter(dep -> common.stream().noneMatch(dep::isSame))
                .collect(toSet());
    }


    public boolean isImplExternal(DependencyHolder dependencyHolder) {
        return getImplExternal().stream().anyMatch(dependencyHolder::isSame);
    }

    @Override
    public Set<ResolvedArtifact> getImplInternal() {
        return runtimeConfDependencies.stream()
                .filter(DependencyHolder::isNative)
                .filter(dep -> SubProjectTypes.API != dep.getSubProjectType())
                .collect(toSet());
    }

    public boolean isInternalImpl(DependencyHolder dependencyHolder) {
        return getImplInternal().stream().anyMatch(dependencyHolder::isSame);
    }

    public List<DependencyHolder> getAll() {
        return Lists.newArrayList(runtimeConfDependencies);
    }

    public File getArchive() {
        return archivePath;
    }
}
