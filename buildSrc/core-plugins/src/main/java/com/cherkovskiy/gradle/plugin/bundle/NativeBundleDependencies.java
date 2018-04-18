package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.gradle.plugin.BundleDependencies;
import com.cherkovskiy.gradle.plugin.DependencyHolder;
import com.cherkovskiy.gradle.plugin.SubProjectTypes;
import com.google.common.collect.Lists;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class NativeBundleDependencies implements BundleDependencies {
    private final List<DependencyHolder> runtimeConfDependencies;
    private final List<DependencyHolder> apiConfDependencies;

    public NativeBundleDependencies(List<DependencyHolder> runtimeConfDependencies, List<DependencyHolder> apiConfDependencies) {
        this.runtimeConfDependencies = runtimeConfDependencies;
        this.apiConfDependencies = apiConfDependencies;
    }

    public List<DependencyHolder> getApiExport() {
        return runtimeConfDependencies.stream()
                .filter(dep -> dep.isNative() &&
                        SubProjectTypes.API == dep.getSubProjectType() &&
                        existsInApiConfig(dep)
                )
                .collect(toList());
    }

    private boolean existsInApiConfig(DependencyHolder dependencyHolder) {
        return apiConfDependencies.stream().anyMatch(dependencyHolder::isSame);
    }

    @Override
    public boolean isApiExport(DependencyHolder dependencyHolder) {
        return getApiExport().stream().anyMatch(dep -> dep.isSame(dependencyHolder));
    }

    @Override
    public List<DependencyHolder> getApiImport() {
        return runtimeConfDependencies.stream()
                .filter(dep -> dep.isNative() &&
                        SubProjectTypes.API == dep.getSubProjectType() &&
                        !existsInApiConfig(dep)
                )
                .collect(toList());
    }

    @Override
    public boolean isApiImport(DependencyHolder dependencyHolder) {
        return getApiImport().stream().anyMatch(dep -> dep.isSame(dependencyHolder));
    }

    @Override
    public List<DependencyHolder> getCommon() {
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
                .collect(toList());
    }

    @Override
    public boolean isCommon(DependencyHolder dependencyHolder) {
        return getCommon().stream().anyMatch(dep -> dep.isSame(dependencyHolder));
    }

    @Override
    public List<DependencyHolder> getExternalImpl() {
        final List<DependencyHolder> common = getCommon();
        return runtimeConfDependencies.stream()
                .filter(dep -> !dep.isNative())
                .filter(dep -> common.stream().noneMatch(cd -> cd.isSame(dep)))
                .collect(toList());
    }

    @Override
    public boolean isExternalImpl(DependencyHolder dependencyHolder) {
        return getExternalImpl().stream().anyMatch(dep -> dep.isSame(dependencyHolder));
    }

    @Override
    public List<DependencyHolder> getInternalImpl() {
        return runtimeConfDependencies.stream()
                .filter(DependencyHolder::isNative)
                .filter(dep -> SubProjectTypes.API != dep.getSubProjectType())
                .collect(toList());
    }

    @Override
    public boolean isInternalImpl(DependencyHolder dependencyHolder) {
        return getInternalImpl().stream().anyMatch(dep -> dep.isSame(dependencyHolder));
    }

    @Override
    public List<DependencyHolder> getAll() {
        return Lists.newArrayList(runtimeConfDependencies);
    }
}
