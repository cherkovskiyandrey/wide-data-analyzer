package com.cherkovskiy.gradle.plugin.api_module;

import com.cherkovskiy.gradle.plugin.DependencyHolder;
import com.cherkovskiy.gradle.plugin.DependencyScanner;
import com.cherkovskiy.gradle.plugin.SubProjectTypes;
import com.cherkovskiy.gradle.plugin.Utils;
import com.google.common.collect.ImmutableSet;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class ApiDependencyChecker implements Plugin<Project> {

    private static final ImmutableSet<SubProjectTypes> ALLOWED_TO_DEPENDS_ON_LIST = new ImmutableSet.Builder<SubProjectTypes>()
            .add(SubProjectTypes.API)
            .build();

    @Override
    public void apply(Project project) {

        project.getTasks().getAt("assemble").doLast(task -> {

            final DependencyScanner dependencyScanner = new DependencyScanner(project);

            final List<DependencyHolder> dependencies = dependencyScanner.getRuntimeDependencies();
            Utils.checkImportProjectsRestrictions(project, dependencies, ALLOWED_TO_DEPENDS_ON_LIST);

            final List<DependencyHolder> prjExternalDependencies = dependencies.stream()
                    .filter(((Predicate<DependencyHolder>) DependencyHolder::isNative).negate())
                    .collect(Collectors.toList());

            final List<DependencyHolder> managementDependencies = dependencyScanner.getManagementedDependencies();

            for (DependencyHolder prjDep : prjExternalDependencies) {
                final Optional<DependencyHolder> mngDep = managementDependencies.stream()
                        .filter(mDep -> Objects.equals(mDep.getGroup(), prjDep.getGroup()) &&
                                Objects.equals(mDep.getName(), prjDep.getName()))
                        .findFirst();

                if (!mngDep.isPresent()) {
                    throw new GradleException(format("For all api projects it is not allowed to use 3rd party dependencies which does not exists in dependencyManagement section. " +
                                    "For project \"%s\" artifact: \"%s\" does'n exists in dependencyManagement section of root build.gradle",
                            project.getPath(), prjDep));

                } else if (!Objects.equals(mngDep.get().getVersion(), prjDep.getVersion())) {
                    throw new GradleException(format("For all api projects it is not allowed to use 3rd party dependencies with different version from dependencyManagement section. " +
                            "Project \"%s\" has other version 3rd-party dependency: \"%s\". Must be used %s!", project.getPath(), prjDep, mngDep.get().getVersion()));
                }
            }
        });
    }
}
