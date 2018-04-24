package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.gradle.plugin.*;
import com.cherkovskiy.gradle.plugin.bundle.BundlePackagerConfiguration;
import com.cherkovskiy.gradle.plugin.bundle.ProjectBundle;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.jvm.tasks.Jar;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cherkovskiy.gradle.plugin.ConfigurationTypes.API;
import static java.lang.String.format;

class OnboardResolver implements Closeable {
    public static final String ONBOARD_CONF_NAME = "onboard";

    private final Project project;
    private final ApplicationPackagerConfiguration configuration;
    private final File baseTmpDir;
    private final ResolvedDependency applicationStarter;
    private final ResolvedBundleArtifact currentBundle;
    private final Set<ResolvedBundleArtifact> bundles;

    OnboardResolver(Project project, ApplicationPackagerConfiguration configuration) throws IOException {
        this.project = project;
        this.configuration = configuration;
        this.baseTmpDir = Files.createTempDir();
        FileUtils.forceDeleteOnExit(this.baseTmpDir);

        this.bundles = Sets.newTreeSet(ResolvedBundleArtifact.COMPARATOR);
        ResolvedDependency applicationStarter = null;
        for (Dependency dependency : project.getConfigurations().getByName(ONBOARD_CONF_NAME).getDependencies()) {
            if (dependency instanceof ProjectDependency) {
                final Project depProject = ((ProjectDependency) dependency).getDependencyProject();
                if (isBundle(dependency)) {
                    this.bundles.add(getBundleFromProject(depProject));
                } else if (isApplicationStarter(dependency)) {

                    //TODO: вытащить в том числе и зависимости по классам: api, core, common + 3rd party и их разложить в ApplicationPackager
                    //applicationStarter =

                } else {
                    throw new GradleException(format("Unsupported using in \"%s\" not a bundle or application-starter dependencies: %s",
                            ONBOARD_CONF_NAME, dependency.toString()));
                }
            } else {
                if (isBundle(dependency)) {
                    this.bundles.add(getBundleFromArtifact(dependency));
                } else if (isApplicationStarter(dependency)) {

                    //TODO: вытащить в том числе и зависимости по классам: api, core, common + 3rd party и их разложить в ApplicationPackager
                    //applicationStarter = DependencyScanner.resolveDetachedOn(project, dependency).get(0);

                } else {
                    throw new GradleException(format("Unsupported using in \"%s\" not a bundle or application-starter dependencies: %s",
                            ONBOARD_CONF_NAME, dependency.toString()));
                }
            }
        }
    }

    /**
     * @return all bundles form which application get on board + embedded in application bundle
     */
    public Set<ResolvedBundleArtifact> getBundles() {
        return bundles;
    }

    private ResolvedBundleArtifact getBundleFromProject(Project depProject) {
        final DependencyScanner dependencyScanner = new DependencyScanner(depProject);
        final List<DependencyHolder> runtimeConfDependencies = dependencyScanner.getRuntimeDependencies();
        final List<DependencyHolder> apiConfDependencies = dependencyScanner.getDependenciesByType(API);
        final Jar jarTask = depProject.getTasks().withType(Jar.class).iterator().next();
        final BundlePackagerConfiguration configuration = project.getExtensions().getByType(BundlePackagerConfiguration.class);

        return new ProjectBundle(jarTask.getArchivePath(),
                jarTask.getBaseName(),
                jarTask.getVersion(),
                configuration.embeddedDependencies,
                runtimeConfDependencies,
                apiConfDependencies);
    }

    private ResolvedBundleArtifact getBundleFromArtifact(Dependency bundle) throws IOException {
        final List<DependencyHolder> dependencies = DependencyScanner.resolveDetachedOn(project, bundle);
        final DependencyHolder root = dependencies.stream()
                .filter(dh -> Objects.equals(dh.getGroup(), bundle.getGroup()) &&
                        Objects.equals(dh.getName(), bundle.getName()) &&
                        Objects.equals(dh.getVersion(), bundle.getVersion()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(""));

        final BundleFile bundleFile = new BundleFile(root.getFile());
        if (bundleFile.isEmbedded()) {
            final File bundleUnpackDir = new File(baseTmpDir, bundleFile.getName());
            FileUtils.forceMkdir(bundleUnpackDir);
            return bundleFile.resolveTo(bundleUnpackDir);

        } else {
            final List<DependencyHolder> apiExport = dependencies.stream()
                    .filter(dh -> bundleFile.getApiExport().contains(dh))
                    .collect(Collectors.toList());

            return new ProjectBundle(root.getFile(),
                    root.getName(),
                    root.getVersion(),
                    false,
                    dependencies.stream().filter(d -> !d.equals(root)).collect(Collectors.toList()),
                    apiExport);
        }
    }


    private boolean isBundle(Dependency dependency) {
        return SubProjectTypes.BUNDLE.getSubGroupName().equalsIgnoreCase(dependency.getGroup());
    }

    private boolean isApplicationStarter(Dependency dependency) {
        return configuration.starterName.equalsIgnoreCase(dependency.getName());
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(baseTmpDir);
    }

    public static void createConfiguration(Project project) {
        final Configuration onboard = project.getConfigurations().create(ONBOARD_CONF_NAME);
        final Task buildTask = project.getTasks().getAt(JavaBasePlugin.BUILD_TASK_NAME);
        buildTask.dependsOn(onboard.getTaskDependencyFromProjectDependency(true, JavaBasePlugin.BUILD_NEEDED_TASK_NAME));
    }

    public Optional<ResolvedBundleArtifact> getCurrentBundle() {
        //todo: может и не быть
        return getBundleFromProject(project);
    }

    public ResolvedDependency getApplicationStarter() {
        return applicationStarter;
    }
}
