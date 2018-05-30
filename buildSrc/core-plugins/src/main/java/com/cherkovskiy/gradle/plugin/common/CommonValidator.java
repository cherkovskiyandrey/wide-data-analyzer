package com.cherkovskiy.gradle.plugin.common;

import com.cherkovskiy.gradle.plugin.*;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.LogLevel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.cherkovskiy.gradle.plugin.SubProjectTypes.CORE_PROJECT_GROUP;
import static java.lang.String.format;
import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME;

public class CommonValidator implements Plugin<Project> {
    public static String COMMON_VALIDATE_TASK_NAME = "commonValidate";

    @Override
    public void apply(Project project) {


        project.getGradle().addListener((ProjectEvaluatedListener) gradle -> {

            final String rootGroupName = Utils.lookUpRootGroupName(project);
            if (!CORE_PROJECT_GROUP.equalsIgnoreCase(rootGroupName)) {
                throw new GradleException(format("Core project group must be %s", CORE_PROJECT_GROUP));
            }

            if (!project.getRootProject().equals(project)) {
                final String projectGroup = project.getGroup().toString();
                if (!StringUtils.startsWith(projectGroup, rootGroupName)) {
                    throw new GradleException(format("Invalid subproject group %s for %s. Must start from %s!", projectGroup, project.getPath(), CORE_PROJECT_GROUP));
                }

                final Optional<String> projectTypeStr = Utils.subProjectAgainst(projectGroup, rootGroupName);
                if (!projectTypeStr.isPresent() || Objects.isNull(SubProjectTypes.ofSubGroupName(projectTypeStr.get()))) {
                    throw new GradleException(format("Unsupported subproject group name %s for %s. Use only %s!",
                            projectGroup, project.getPath(), SubProjectTypes.SUB_GROUP_NAME_TO_TYPE.keySet().stream().collect(Collectors.joining(", "))));
                }
            }

            // Create new task and set in a DAG
            final Task validatorTask = project.getTasks().create(COMMON_VALIDATE_TASK_NAME);
            project.getTasks().getAt(COMPILE_JAVA_TASK_NAME).dependsOn(validatorTask);

            validatorTask.setActions(Collections.singletonList(task -> {
                final DependencyScanner dependencyScanner = new DependencyScanner(project);

                final List<DependencyHolder> prjExternalDependencies = dependencyScanner.getRuntimeDependencies().stream()
                        .filter(((Predicate<DependencyHolder>) DependencyHolder::isNative).negate())
                        .collect(Collectors.toList());

                final List<DependencyHolder> managementDependencies = dependencyScanner.getManagementedDependencies();

                for (DependencyHolder prjDep : prjExternalDependencies) {
                    final Optional<DependencyHolder> mngDep = managementDependencies.stream()
                            .filter(mDep -> Objects.equals(mDep.getGroup(), prjDep.getGroup()) &&
                                    Objects.equals(mDep.getName(), prjDep.getName()))
                            .findFirst();

                    if (!mngDep.isPresent()) {
                        project.getLogger().log(LogLevel.ERROR, "Dependency: {} does'n exists in dependencyManagement section of root build.gradle", prjDep);

                    } else if (!Objects.equals(mngDep.get().getVersion(), prjDep.getVersion())) {
                        project.getLogger().log(LogLevel.ERROR, "Dependency: {} has other version than {} in dependencyManagement section of root build.gradle. ",
                                prjDep, mngDep.get().getVersion());
                    }
                }
            }));
        });
    }
}
