package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.gradle.plugin.*;
import com.cherkovskiy.gradle.plugin.api.BundleResolver;
import com.cherkovskiy.gradle.plugin.api.ResolvedBundleArtifact;
import com.cherkovskiy.gradle.plugin.api.ResolvedProjectArtifact;
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
    private final ResolvedProjectArtifact applicationStarter;
    private final ResolvedBundleArtifact currentBundle;
    private final Set<ResolvedBundleArtifact> bundles;

    OnboardResolver(Project project, ApplicationPackagerConfiguration configuration) throws IOException {
        this.project = project;
        this.configuration = configuration;
        this.baseTmpDir = Files.createTempDir();
        FileUtils.forceDeleteOnExit(this.baseTmpDir);

        this.bundles = Sets.newTreeSet(ResolvedBundleArtifact.COMPARATOR);
        ResolvedProjectArtifact applicationStarter = null;
        for (Dependency dependency : project.getConfigurations().getByName(ONBOARD_CONF_NAME).getDependencies()) {
            if (dependency instanceof ProjectDependency) {
                final Project depProject = ((ProjectDependency) dependency).getDependencyProject();
                if (isBundle(dependency)) {
                    this.bundles.add(getBundleFromProject(depProject));
                } else if (isApplicationStarter(dependency)) {
                    applicationStarter = getResolvedArtifactFormProject(depProject);
                } else {
                    throw new GradleException(format("Unsupported using in \"%s\" not a bundle or application-starter dependencies: %s",
                            ONBOARD_CONF_NAME, dependency.toString()));
                }
            } else {
                if (isBundle(dependency)) {
                    this.bundles.add(getBundleFromArtifact(dependency));
                } else if (isApplicationStarter(dependency)) {
                    applicationStarter = getResolvedArtifactAsDependency(dependency);
                } else {
                    throw new GradleException(format("Unsupported using in \"%s\" not a bundle or application-starter dependencies: %s",
                            ONBOARD_CONF_NAME, dependency.toString()));
                }
            }
        }

        this.applicationStarter = applicationStarter;

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


    private ResolvedProjectArtifact getResolvedArtifactFormProject(Project starter) {
        final DependencyScanner dependencyScanner = new DependencyScanner(starter);
        final List<DependencyHolder> dependencies = dependencyScanner.getRuntimeDependencies();
        final List<DependencyHolder> allResolvedApiDependencies = dependencyScanner.resolveDetachedOn(null, Utils.getAllApiSubProjects(project));
        final Jar jarTask = starter.getTasks().withType(Jar.class).iterator().next();

        return new SimpleResolvedProjectArtifact(
                starter.getGroup().toString(),
                starter.getName(),
                starter.getVersion().toString(),
                jarTask.getArchivePath(),
                dependencies,
                allResolvedApiDependencies
        );
    }

    private ResolvedBundleArtifact getBundleFromArtifact(Dependency bundle) throws IOException {
        final DependencyScanner dependencyScanner = new DependencyScanner(project);
        final List<DependencyHolder> dependencies = dependencyScanner.resolveDetachedOn(null, bundle);
        final DependencyHolder root = dependencies.stream()
                .filter(dh -> Objects.equals(dh.getGroup(), bundle.getGroup()) &&
                        Objects.equals(dh.getName(), bundle.getName()) &&
                        Objects.equals(dh.getVersion(), bundle.getVersion()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(format("Could not find root bundle in resolved dependencies on it: %s:%s:%s",
                        bundle.getGroup(), bundle.getName(), bundle.getVersion())));

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


    private ResolvedProjectArtifact getResolvedArtifactAsDependency(Dependency starter) {
        final DependencyScanner dependencyScanner = new DependencyScanner(project);
        final List<DependencyHolder> dependencies = dependencyScanner.resolveDetachedOn(null, starter);
        final List<DependencyHolder> allResolvedApiDependencies = dependencyScanner.resolveDetachedOn(null, Utils.getAllApiSubProjects(project));
        final DependencyHolder root = dependencies.stream()
                .filter(dh -> Objects.equals(dh.getGroup(), starter.getGroup()) &&
                        Objects.equals(dh.getName(), starter.getName()) &&
                        Objects.equals(dh.getVersion(), starter.getVersion()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(format("Could not find root starter in resolved dependencies on it: %s:%s:%s",
                        starter.getGroup(), starter.getName(), starter.getVersion())));
        dependencies.remove(root);

        //TODO: нужно взять из dependencies все API и получить для них все коммон и добавить их к allResolvedApiDependencies

        return new SimpleResolvedProjectArtifact(
                root.getGroup(),
                root.getName(),
                root.getVersion(),
                root.getFile(),
                dependencies,
                allResolvedApiDependencies
        );
    }


    private boolean isBundle(Dependency dependency) {
        return Utils.subProjectAgainst(dependency.getGroup(), SubProjectTypes.CORE_PROJECT_GROUP)
                .map(s -> SubProjectTypes.BUNDLE.getSubGroupName().equalsIgnoreCase(s))
                .orElse(false);
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

    public ResolvedProjectArtifact getApplicationStarter() {
        return applicationStarter;
    }
}
