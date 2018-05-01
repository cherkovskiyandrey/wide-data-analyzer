package com.cherkovskiy.gradle.plugin;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import javax.annotation.Nonnull;
import java.util.*;

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
                    forbiddenDependencies.stream().map(DependencyHolder::toString).collect(joining(", "))
            ));
        }
    }


    public static <T> String collectionToDeepString(Collection<T> collection) {
    return collectionToDeepStringHelper();
    }
        public static <T> String collectionToDeepString(Collection<T> collection) {
        return collection.stream()
                .map(t -> {
                    if (t instanceof Collection) {
                        return collectionToDeepString(t);
                    } else if (t instanceof Map) {
                        return collectionToDeepString(t);
                    } else if (t.getClass().isArray()) {
                        return collectionToDeepString(Arrays.asList(t));
                    }
                    return t.toString();
                })
                .collect(joining(", "));
    }
}
