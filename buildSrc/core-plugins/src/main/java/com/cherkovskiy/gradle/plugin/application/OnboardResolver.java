package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.gradle.plugin.*;
import com.cherkovskiy.gradle.plugin.api.BundleResolver;
import com.cherkovskiy.gradle.plugin.api.ResolvedBundleArtifact;
import com.cherkovskiy.gradle.plugin.api.ResolvedDependency;
import com.google.common.collect.Lists;
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

        final ResolvedBundleArtifact currentBundle = getBundleFromProject(project);
        this.currentBundle = currentBundle.getServices().isEmpty() ? null : currentBundle;
    }

    /**
     * @return all bundles form which application get on board + embedded in application bundle
     */
    public Set<ResolvedBundleArtifact> getBundles() {
        return bundles;
    }

    private ResolvedBundleArtifact getBundleFromProject(Project depProject) throws IOException {
        final DependencyScanner dependencyScanner = new DependencyScanner(depProject);
        final List<DependencyHolder> dependencies = dependencyScanner.getRuntimeDependencies();
        final BundleResolver resolver = new ProjectBundleResolver(Lists.newArrayList(dependencies));
        final Jar jarTask = depProject.getTasks().withType(Jar.class).iterator().next();
        final BundleFile bundleFile = new BundleFile(jarTask.getArchivePath());

        return resolver.resolve(bundleFile);
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
        final BundleResolver resolver;
        if (bundleFile.isEmbedded()) {
            final File bundleUnpackDir = new File(baseTmpDir, bundleFile.getName());
            FileUtils.forceMkdir(bundleUnpackDir);
            resolver = new EmbeddedResolver(bundleUnpackDir);
        } else {
            resolver = new ProjectBundleResolver(Lists.newArrayList(dependencies));
        }
        return resolver.resolve(bundleFile);
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
        return Optional.ofNullable(currentBundle);
    }

    public ResolvedDependency getApplicationStarter() {
        return applicationStarter;
    }
}
