package com.cherkovskiy.gradle.plugin;

import java.util.List;

public interface BundleDependencies {

    List<DependencyHolder> getApiExport();

    boolean isApiExport(DependencyHolder dependencyHolder);

    List<DependencyHolder> getApiImport();

    boolean isApiImport(DependencyHolder dependencyHolder);

    List<DependencyHolder> getCommon();

    boolean isCommon(DependencyHolder dependencyHolder);

    List<DependencyHolder> getExternalImpl();

    boolean isExternalImpl(DependencyHolder dependencyHolder);

    List<DependencyHolder> getInternalImpl();

    boolean isInternalImpl(DependencyHolder dependencyHolder);

    List<DependencyHolder> getAll();
}
