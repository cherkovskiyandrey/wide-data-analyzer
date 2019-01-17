package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.gradle.plugin.api.Dependency;
import com.cherkovskiy.gradle.plugin.api.ResolvedDependency;
import com.cherkovskiy.gradle.plugin.api.ResolvedProjectArtifact;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleResolvedProjectArtifact implements ResolvedProjectArtifact {

    private final String group;
    private final String name;
    private final String version;
    private final File path;
    private final List<DependencyHolder> dependencies;
    private final List<DependencyHolder> allResolvedApiDependencies;

    public SimpleResolvedProjectArtifact(@Nonnull String group,
                                         @Nonnull String name,
                                         @Nonnull String version,
                                         @Nonnull File path,
                                         @Nonnull List<DependencyHolder> dependencies,
                                         @Nonnull List<DependencyHolder> allResolvedApiDependencies) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.path = path;
        this.dependencies = Lists.newArrayList(dependencies);
        this.allResolvedApiDependencies = allResolvedApiDependencies;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return group;
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

    @Nullable
    @Override
    public String getFileName() {
        return path.getName();
    }

    @Nonnull
    @Override
    public File getFile() {
        return path;
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getApi() {
        return dependencies.stream()
                .filter(rd -> rd.isNative() && SubProjectTypes.API == rd.getSubProjectType())
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getCommon() {
        return dependencies.stream()
                .filter(((Predicate<? super DependencyHolder>) DependencyHolder::isNative).negate())
                .filter(allResolvedApiDependencies::contains)
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> get3rdParty() {
        final Set<ResolvedDependency> common = getCommon();
        return dependencies.stream()
                .filter(rd -> !rd.isNative() && !common.contains(rd))
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getInternal() {
        final Set<ResolvedDependency> api = getApi();
        return dependencies.stream()
                .filter(rd -> rd.isNative() && !api.contains(rd))
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }
}
