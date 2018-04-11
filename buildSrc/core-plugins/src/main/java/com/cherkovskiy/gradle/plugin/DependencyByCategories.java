package com.cherkovskiy.gradle.plugin;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class DependencyByCategories {
    private final List<DependencyHolder> dependencies;

    public DependencyByCategories(List<DependencyHolder> dependencies) {
        this.dependencies = dependencies;
    }

    public List<DependencyHolder> getApi() {
        return dependencies.stream()
                .filter(dep -> dep.isNative() && SubProjectTypes.API == dep.getSubProjectType())
                .collect(toList());
    }

    public List<DependencyHolder> getCommon() {
        return dependencies.stream()
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

    public List<DependencyHolder> getExternalImpl() {
        final List<DependencyHolder> common = getCommon();
        return dependencies.stream()
                .filter(dep -> !dep.isNative())
                .filter(dep -> !common.contains(dep))
                .collect(toList());
    }

    public List<DependencyHolder> getInternalImpl() {
        final List<DependencyHolder> api = getApi();
        return dependencies.stream()
                .filter(DependencyHolder::isNative)
                .filter(dep -> !api.contains(dep))
                .collect(toList());
    }
}
