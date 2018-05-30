package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.gradle.plugin.*;
import com.google.common.collect.ImmutableSet;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.jvm.tasks.Jar;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.cherkovskiy.gradle.plugin.ConfigurationTypes.*;
import static com.cherkovskiy.gradle.plugin.Utils.getOrCreateConfig;
import static com.cherkovskiy.gradle.plugin.Utils.subProjectAgainst;
import static java.lang.String.format;
import static org.gradle.api.plugins.JavaPlugin.JAR_TASK_NAME;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public class BundlePlugin implements Plugin<Project> {
    public static String ASSEMBLE_BUNDLE_TASK_NAME = "assembleBundle";
    private static final ImmutableSet<SubProjectTypes> ALLOWED_TO_DEPENDS_ON_LIST = new ImmutableSet.Builder<SubProjectTypes>()
            .add(SubProjectTypes.API)
            .add(SubProjectTypes.COMMON)
            .build();

    @Override
    public void apply(@Nonnull Project project) {
        final BundlePackagerConfiguration configuration = getOrCreateConfig(project, BundlePackagerConfiguration.NAME, BundlePackagerConfiguration.class);

        project.getGradle().addListener((ProjectEvaluatedListener) gradle -> {

            //Without init, means don't make configuration depends on tasks from api projects.
            createAndPopulateStuffApiConfig(project);

            //To have all ready depended jar before start own jar task
            project.getTasks().getAt(JAR_TASK_NAME).dependsOn(project.getConfigurations()
                    .getByName(RUNTIME_CLASSPATH.getGradleString()).getTaskDependencyFromProjectDependency(true, JAR_TASK_NAME));

            // Create new task and set in a DAG
            final Task assembleBundleTask = project.getTasks().create(ASSEMBLE_BUNDLE_TASK_NAME);
            assembleBundleTask.dependsOn(project.getTasks().getAt(JAR_TASK_NAME));
            project.getTasks().getAt(ASSEMBLE_TASK_NAME).dependsOn(assembleBundleTask);

            assembleBundleTask.setActions(Collections.singletonList(task -> {
                final Jar jarTask = project.getTasks().withType(Jar.class).iterator().next();

                final DependencyScanner dependencyScanner = new DependencyScanner(project);
                final List<DependencyHolder> runtimeConfDependencies = dependencyScanner.getRuntimeDependencies();
                final List<DependencyHolder> allApiDependencies = dependencyScanner.getResolvedDependenciesByType(STUFF_ALL_API);

                Utils.checkImportProjectsRestrictions(project, runtimeConfDependencies, ALLOWED_TO_DEPENDS_ON_LIST);
                checkDependenciesAgainst(project, runtimeConfDependencies, allApiDependencies);

                final List<DependencyHolder> apiConfDependencies = dependencyScanner.getDependenciesByType(API);
                final ProjectBundle bundleArtifact = new ProjectBundle(jarTask.getArchivePath(),
                        jarTask.getBaseName(),
                        jarTask.getVersion(),
                        configuration.embeddedDependencies,
                        runtimeConfDependencies,
                        apiConfDependencies);

                if (configuration.failIfNotBundle && bundleArtifact.getServices().isEmpty()) {
                    throw new GradleException(format("Bundle must has at least one service. There are not any services in bundle: %s",
                            project.getPath()));
                }

                try (BundlePackager bundleArchive = new BundlePackager(bundleArtifact.getFile(), bundleArtifact.isEmbedded())) {
                    bundleArchive.setBundleNameVersion(bundleArtifact.getName(), bundleArtifact.getVersion());

                    bundleArchive.putApiExportDependencies(bundleArtifact.getApiExport());
                    bundleArchive.putApiImportDependencies(bundleArtifact.getApiImport());
                    bundleArchive.putCommonDependencies(bundleArtifact.getCommon());
                    bundleArchive.putExternalImplDependencies(bundleArtifact.getImplExternal());
                    bundleArchive.putInternalImplDependencies(bundleArtifact.getImplInternal());

                    bundleArchive.addServices(bundleArtifact.getServices());
                } catch (Exception e) {
                    throw new GradleException("Could not change artifact: " + jarTask.getArchivePath().getAbsolutePath(), e);
                }
            }));
        });
    }

    private void createAndPopulateStuffApiConfig(Project project) {
        final String rootGroupName = Utils.lookUpRootGroupName(project);
        project.getConfigurations().create(STUFF_ALL_API.getGradleString(), conf -> conf.getDependencies().addAll(
                project.getRootProject().getSubprojects().stream()
                        .filter(sp -> subProjectAgainst(sp.getGroup().toString(), rootGroupName)
                                .map(sg -> SubProjectTypes.API.getSubGroupName().equalsIgnoreCase(sg))
                                .orElse(false))
                        .map(project.getDependencies()::create)
                        .collect(Collectors.toSet())));
    }

    private void checkDependenciesAgainst(Project project, List<DependencyHolder> dependencies, List<DependencyHolder> allApiDependencies) {
        final List<DependencyHolder> prjExternalDependencies = dependencies.stream()
                .filter(d -> !d.isNative())
                .collect(Collectors.toList());

        final List<DependencyHolder> allApiExternalDependencies = allApiDependencies.stream()
                .filter(d -> !d.isNative())
                .collect(Collectors.toList());

        for (final DependencyHolder prjDep : prjExternalDependencies) {
            allApiExternalDependencies.stream()
                    .filter(mDep -> Objects.equals(mDep.getGroup(), prjDep.getGroup()) &&
                            Objects.equals(mDep.getName(), prjDep.getName()))
                    .forEach(dependencyHolder -> {
                        if (!Objects.equals(dependencyHolder.getVersion(), prjDep.getVersion())) {
                            throw new GradleException(format("It is not allowed to use 3rd party dependencies from scope of all api subprojects and different version. " +
                                            "Project \"%s\" has other version common dependency: \"%s\". Must be used %s!",
                                    project.getPath(), prjDep, dependencyHolder.getVersion()));
                        }
                    });

        }
    }
}
