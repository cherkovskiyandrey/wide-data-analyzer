package com.cherkovskiy.gradle.plugin;

import com.google.common.collect.Lists;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cherkovskiy.gradle.plugin.ConfigurationTypes.RUNTIME_CLASSPATH;
import static java.lang.String.format;

public class DependencyScanner {

    private final Project project;

    public DependencyScanner(Project project) {
        this.project = project;
    }

    public List<DependencyHolder> getRuntimeDependencies() {
        return getResolvedDependenciesByType(RUNTIME_CLASSPATH);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public List<DependencyHolder> getManagementedDependencies() {
        return resolveDetachedOn(
                ConfigurationTypes.IMPLEMENTATION,
                ((Map<String, Dependency>) project.getProperties().get("dependencyManagement")).values().toArray(new Dependency[]{})
        );
    }

    public List<DependencyHolder> getResolvedDependenciesByType(ConfigurationTypes confType) {
        if (!confType.couldBeResolved()) {
            throw new IllegalArgumentException(format("Dependency lifecycleType %s could not be resolved.", confType.getGradleString()));
        }

        final List<DependencyHolder> dependencies = Lists.newArrayList();
        for (ResolvedDependency resolvedDependency : project.getConfigurations()
                .getByName(confType.getGradleString())
                .getResolvedConfiguration()
                .getFirstLevelModuleDependencies()) {
            walkDependency(resolvedDependency, dependencies, confType, null);
        }
        return dependencies;
    }


    public List<DependencyHolder> getDependenciesByType(ConfigurationTypes confName) {
        return project.getConfigurations()
                .getByName(confName.getGradleString())
                .getDependencies()
                .stream()
                .map(d -> DependencyHolder.builder()
                        .setGroup(d.getGroup())
                        .setName(d.getName())
                        .setVersion(d.getVersion())
                        .setConfigurationType(confName)
                        .build())
                .collect(Collectors.toList());
    }

    private static void walkDependency(ResolvedDependency resolvedDependency, List<DependencyHolder> dependencies, ConfigurationTypes dependencyType, DependencyHolder parent) {
        final DependencyHolder holder = DependencyHolder.builder()
                .setGroup(resolvedDependency.getModule().getId().getGroup())
                .setName(resolvedDependency.getModule().getId().getName())
                .setVersion(resolvedDependency.getModule().getId().getVersion())
                .setFiles(extractArtifacts(resolvedDependency.getModuleArtifacts()))
                .setConfigurationType(dependencyType)
                .setParent(parent)
                .build();

        dependencies.add(holder);
        resolvedDependency.getChildren().forEach(childDependency -> walkDependency(childDependency, dependencies, dependencyType, holder));
    }

    private static List<File> extractArtifacts(Set<ResolvedArtifact> moduleArtifacts) {
        return moduleArtifacts.stream()
                .map(ResolvedArtifact::getFile)
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<DependencyHolder> resolveDetachedOn(@Nullable ConfigurationTypes configurationTypes, @Nonnull Dependency... dependency) {
        final List<DependencyHolder> dependencies = Lists.newArrayList();
        for (ResolvedDependency resolvedDependency : project.getConfigurations()
                .detachedConfiguration(dependency)
                .getResolvedConfiguration()
                .getFirstLevelModuleDependencies()) {

            walkDependency(
                    resolvedDependency,
                    dependencies,
                    Optional.ofNullable(configurationTypes).orElse(ConfigurationTypes.UNKNOWN),
                    null
            );
        }

        return dependencies;
    }
}
