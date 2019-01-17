package com.cherkovskiy.gradle.plugin;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Utils {

    public static String lookUpRootGroupName(@Nonnull Project project) {
        return project.getRootProject().getGroup().toString();
    }

    public static Optional<String> subProjectAgainst(String group, String rootGroupName) {
        if (StringUtils.startsWith(group, rootGroupName)) {
            String[] prjGroup = StringUtils.split(group.substring(rootGroupName.length()), '.');
            return prjGroup.length > 0 ? Optional.of(prjGroup[0]) : Optional.empty();
        }
        return Optional.empty();
    }

    public static void checkImportProjectsRestrictions(@Nonnull Project project, @Nonnull List<DependencyHolder> dependencies, @Nonnull Set<SubProjectTypes> allowedSubProjects) {
        final List<DependencyHolder> forbiddenDependencies = dependencies.stream()
                .filter(dep -> dep.isNative() && !allowedSubProjects.contains(dep.getSubProjectType()))
                .collect(toList());

        if (!forbiddenDependencies.isEmpty()) {
            throw new GradleException(format("%s could depends only on: %s. There is forbidden dependencies: %s",
                    project.getPath(),
                    allowedSubProjects.stream().map(SubProjectTypes::getSubGroupName).collect(joining(", ")),
                    forbiddenDependencies.stream().map(dependencyHolder -> dependencyHolder.toString()).collect(joining(", "))
            ));
        }
    }

    public static <T> T getOrCreateConfig(Project project, String name, Class<T> token) {
        T existed = project.getExtensions().findByType(token);
        if (Objects.nonNull(existed)) {
            return existed;
        }
        return project.getExtensions().create(name, token);
    }

    public static Dependency[] getAllApiSubProjects(Project project) {
        final String rootGroupName = Utils.lookUpRootGroupName(project);
        return project.getRootProject().getSubprojects().stream()
                .filter(sp -> subProjectAgainst(sp.getGroup().toString(), rootGroupName)
                        .map(sg -> SubProjectTypes.API.getSubGroupName().equalsIgnoreCase(sg))
                        .orElse(false))
                .map(project.getDependencies()::create)
                .toArray(Dependency[]::new);
    }
}
