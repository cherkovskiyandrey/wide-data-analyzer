package com.cherkovskiy.gradle.plugin;

import com.google.common.collect.Lists;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.cherkovskiy.gradle.plugin.DependencyScanner.TransitiveMode.TRANSITIVE_ON;
import static com.cherkovskiy.gradle.plugin.DependencyType.RUNTIME_CLASSPATH;
import static java.lang.String.format;

public class DependencyScanner {

    public enum TransitiveMode {
        TRANSITIVE_OFF,
        TRANSITIVE_ON
    }

    private final Project project;

    public DependencyScanner(Project project) {
        this.project = project;
    }

    public List<DependencyHolder> getRuntimeDependencies() {
        return getResolvedDependenciesByType(RUNTIME_CLASSPATH);
    }

    public List<DependencyHolder> resolveAgainst(List<DependencyHolder> dependencies, DependencyType type, TransitiveMode transitiveMode) {
        if (transitiveMode == TRANSITIVE_ON) {
            throw new UnsupportedOperationException("Transitive resolving is not supported yet.");
        }

        final List<DependencyHolder> resolvedDependencies = Lists.newArrayList(dependencies);

        final List<DependencyHolder> specificDependencies = type.couldBeResolved() ? getResolvedDependenciesByType(type) : getDependenciesByType(type)
                .stream()
                .filter(((Predicate<DependencyHolder>) DependencyHolder::isTransitive).negate())
                .collect(Collectors.toList());

        final List<DependencyHolder> depToRecreate = specificDependencies.stream()
                .flatMap(sd -> resolvedDependencies.stream().filter(dep -> isTheSame(dep, sd)))
                .collect(Collectors.toList());

        for (DependencyHolder dep : depToRecreate) {
            List<DependencyHolder> transitiveTree = resolvedDependencies.stream().filter(d -> d.getRoot().equals(dep)).collect(Collectors.toList());
            transitiveTree.forEach(resolvedDependencies::remove);

            transitiveTree = copyWithNewType(transitiveTree, type);
            resolvedDependencies.addAll(transitiveTree);
        }
        return resolvedDependencies;
    }


    @SuppressWarnings("unchecked")
    public List<DependencyHolder> getManagementedDependencies() {
        final List<DependencyHolder> dependencies = Lists.newArrayList();

        for (ResolvedDependency resolvedDependency : project.getConfigurations()
                .detachedConfiguration(
                        ((Map<String, Dependency>) project.getProperties().get("dependencyManagement")).values().toArray(new Dependency[]{}))
                .getResolvedConfiguration()
                .getFirstLevelModuleDependencies()) {

            walkDependency(resolvedDependency, dependencies, DependencyType.IMPLEMENTATION, null);
        }

        return dependencies;
    }


    private List<DependencyHolder> copyWithNewType(List<DependencyHolder> transitiveTree, DependencyType typeOfRoot) {
        final List<DependencyHolder> result = Lists.newArrayList();
        final DependencyHolder root = transitiveTree.get(0).getRoot();

        copyTo(root, null, typeOfRoot, transitiveTree, result);

        return result;
    }

    private void copyTo(DependencyHolder dep,
                        DependencyHolder newParent,
                        DependencyType type,
                        List<DependencyHolder> origTree,
                        List<DependencyHolder> resultTree) {

        DependencyHolder newDep = DependencyHolder.builder()
                .copyOf(dep)
                .setType(type)
                .setParent(newParent)
                .build();

        resultTree.add(newDep);

        List<DependencyHolder> firstLevelChildren = origTree.stream()
                .filter(td -> td.getParent().map(tdp -> tdp.equals(dep)).orElse(false))
                .collect(Collectors.toList());

        for (DependencyHolder child : firstLevelChildren) {
            copyTo(child, newDep, child.getType(), origTree, resultTree);
        }
    }


    private boolean isTheSame(DependencyHolder dep, DependencyHolder specDep) {
        return Objects.equals(dep.getGroup(), specDep.getGroup()) &&
                Objects.equals(dep.getName(), specDep.getName()) &&
                Objects.equals(dep.getVersion(), dep.getVersion());
    }


    public List<DependencyHolder> getResolvedDependenciesByType(DependencyType dependencyType) {
        if (!dependencyType.couldBeResolved()) {
            throw new IllegalArgumentException(format("Dependency type %s could not be resolved.", dependencyType.getGradleString()));
        }

        final List<DependencyHolder> dependencies = Lists.newArrayList();
        for (ResolvedDependency resolvedDependency : project.getConfigurations()
                .getByName(dependencyType.getGradleString())
                .getResolvedConfiguration()
                .getFirstLevelModuleDependencies()) {

            walkDependency(resolvedDependency, dependencies, dependencyType, null);
        }
        return dependencies;
    }

    private void walkDependency(ResolvedDependency resolvedDependency, List<DependencyHolder> dependencies, DependencyType dependencyType, DependencyHolder parent) {
        final DependencyHolder holder = DependencyHolder.builder()
                .setGroup(resolvedDependency.getModule().getId().getGroup())
                .setName(resolvedDependency.getModule().getId().getName())
                .setVersion(resolvedDependency.getModule().getId().getVersion())
                .setFile(extractArtifacts(resolvedDependency.getModuleArtifacts()))
                .setType(dependencyType)
                .setParent(parent)
                .build();

        dependencies.add(holder);
        resolvedDependency.getChildren().forEach(childDependency -> walkDependency(childDependency, dependencies, dependencyType, holder));
    }

    private List<File> extractArtifacts(Set<ResolvedArtifact> moduleArtifacts) {
        return moduleArtifacts.stream()
                .map(ResolvedArtifact::getFile)
                .collect(Collectors.toList());
    }

    private List<DependencyHolder> getDependenciesByType(DependencyType dependencyType) {
        return project.getConfigurations()
                .getByName(dependencyType.getGradleString())
                .getDependencies()
                .stream()
                .map(d -> DependencyHolder.builder()
                        .setGroup(d.getGroup())
                        .setName(d.getName())
                        .setVersion(d.getVersion())
                        .setType(dependencyType)
                        .build())
                .collect(Collectors.toList());
    }


}
