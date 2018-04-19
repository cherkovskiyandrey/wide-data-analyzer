package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.gradle.plugin.*;
import com.cherkovskiy.gradle.plugin.bundle.BundlePackagerConfiguration;
import com.cherkovskiy.gradle.plugin.bundle.ProjectBundle;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.jvm.tasks.Jar;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cherkovskiy.gradle.plugin.ConfigurationTypes.API;
import static java.lang.String.format;

class OnboardResolver implements Closeable {
    public static final String ONBOARD_CONF_NAME = "onboard";

    private final Project project;
    private final List<BundleFile> openedBundles = Lists.newArrayList();

    public OnboardResolver(Project project) {
        this.project = project;
    }

    /**
     * @return all bundles form which application get on board + embedded in application bundle
     */
    public Set<ResolvedBundleArtifact> getBundles() throws IOException {
        final Set<ResolvedBundleArtifact> result = Sets.newHashSet();

        for (Dependency dependency : project.getConfigurations().getByName(ONBOARD_CONF_NAME).getDependencies()) {
            if (dependency instanceof ProjectDependency) {
                final Project depProject = ((ProjectDependency) dependency).getDependencyProject();

                if (isBundle(depProject)) {
                    result.add(getBundleFromProject(depProject));
                }
                //todo: other project elements
            } else {
                if (isBundle(dependency)) {
                    result.add(getBundleFromArtifact(dependency));
                }
                throw new GradleException(format("Unsupported using in \"%s\" not a bundle dependencies: %s",
                        ONBOARD_CONF_NAME, dependency.toString()));
            }
        }

        return result;
    }

    private ResolvedBundleArtifact getBundleFromArtifact(Dependency bundle) throws IOException {
        final List<DependencyHolder> dependencies = DependencyScanner.resolveDetachedOn(project, bundle);
        final DependencyHolder root = dependencies.stream()
                .filter(dh -> Objects.equals(dh.getGroup(), bundle.getGroup()) &&
                        Objects.equals(dh.getName(), bundle.getName()) &&
                        Objects.equals(dh.getVersion(), bundle.getVersion()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(""));

        BundleFile bundleFile = null;
        boolean isEmbedded = false;
        try {
            bundleFile = new BundleFile(root.getArchive());
            isEmbedded = bundleFile.isEmbedded();
            if (isEmbedded) {
                openedBundles.add(bundleFile);
                return bundleFile.asSelfResolved();

            } else {
                final BundleFile bundleFileRef = bundleFile;
                final List<DependencyHolder> apiExport = dependencies.stream()
                        .filter(dh ->
                                bundleFileRef.getApiExport().stream()
                                        .anyMatch(ma -> Objects.equals(dh.getGroup(), ma.getGroup()) &&
                                                Objects.equals(dh.getName(), ma.getName()) &&
                                                Objects.equals(dh.getVersion(), ma.getVersion()))
                        )
                        .collect(Collectors.toList());

                return new ProjectBundle(root.getArchive(),
                        root.getGroup(),
                        root.getName(),
                        root.getVersion(),
                        false,
                        dependencies.stream().filter(d -> !d.equals(root)).collect(Collectors.toList()),
                        apiExport);
            }
        } finally {
            if (bundleFile != null && !isEmbedded) {
                bundleFile.close();
            }
        }
    }

    private ResolvedBundleArtifact getBundleFromProject(Project depProject) {
        final DependencyScanner dependencyScanner = new DependencyScanner(depProject);
        final List<DependencyHolder> runtimeConfDependencies = dependencyScanner.getRuntimeDependencies();
        final List<DependencyHolder> apiConfDependencies = dependencyScanner.getDependenciesByType(API);
        final Jar jarTask = depProject.getTasks().withType(Jar.class).iterator().next();
        final BundlePackagerConfiguration configuration = project.getExtensions().getByType(BundlePackagerConfiguration.class);

        return new ProjectBundle(jarTask.getArchivePath(),
                project.getGroup().toString(),
                jarTask.getBaseName(),
                jarTask.getVersion(),
                configuration.embeddedDependencies,
                runtimeConfDependencies,
                apiConfDependencies);
    }

    private boolean isBundle(Dependency dependency) {
        return SubProjectTypes.BUNDLE.getSubGroupName().equalsIgnoreCase(dependency.getGroup());
    }

    private boolean isBundle(Project depProject) {
        return SubProjectTypes.BUNDLE.getSubGroupName().equalsIgnoreCase(depProject.getGroup().toString());
    }

    @Override
    public void close() throws IOException {
        for (BundleFile bundleFile : openedBundles) {
            bundleFile.close();
        }
    }
}
