package com.cherkovskiy.gradle.plugin;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.jvm.tasks.Jar;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DependencyManagementChecker implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().withType(Jar.class).forEach(jar -> {
            jar.doLast(task -> {

                final String rootGroupName = Utils.lookUpRootGroupName(project);
                final DependencyScanner dependencyScanner = new DependencyScanner(project);

                final List<DependencyHolder> prjExternalDependencies = dependencyScanner.getDependencies().stream()
                        .filter(d -> !StringUtils.startsWith(d.getGroup(), rootGroupName))
                        .collect(Collectors.toList());

                final List<DependencyHolder> managementDependencies = dependencyScanner.getManagementedDependencies();

                for (DependencyHolder prjDep : prjExternalDependencies) {
                    final Optional<DependencyHolder> mngDep = managementDependencies.stream()
                            .filter(mDep -> Objects.equals(mDep.getGroup(), prjDep.getGroup()) &&
                                    Objects.equals(mDep.getName(), prjDep.getName()))
                            .findFirst();

                    if (!mngDep.isPresent()) {
                        project.getLogger().log(LogLevel.ERROR, "Artifact: {} does'n exists in dependencyManagement section of root build.gradle", prjDep);

                    } else if (!Objects.equals(mngDep.get().getVersion(), prjDep.getVersion())) {
                        project.getLogger().log(LogLevel.ERROR, "Artifact: {} has other version than {} in dependencyManagement section of root build.gradle. ",
                                prjDep, mngDep.get().getVersion());
                    }
                }
            });
        });
    }
}
