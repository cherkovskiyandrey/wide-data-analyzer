package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.application_context.api.annotations.Service;
import com.cherkovskiy.gradle.plugin.*;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.jvm.tasks.Jar;
import org.slieb.throwables.FunctionWithThrowable;
import org.slieb.throwables.SuppressedException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static com.cherkovskiy.gradle.plugin.ConfigurationTypes.API;
import static com.cherkovskiy.gradle.plugin.ConfigurationTypes.STUFF_ALL_API;
import static com.cherkovskiy.gradle.plugin.Utils.subProjectAgainst;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class BundlePlugin implements Plugin<Project> {
    private static final ImmutableSet<SubProjectTypes> ALLOWED_TO_DEPENDS_ON_LIST = new ImmutableSet.Builder<SubProjectTypes>()
            .add(SubProjectTypes.API)
            .add(SubProjectTypes.COMMON)
            .build();

    @Override
    public void apply(@Nonnull Project project) {
        final BundlePackagerConfiguration configuration = project.getExtensions().create(BundlePackagerConfiguration.NAME, BundlePackagerConfiguration.class);

        project.getGradle().addListener(new BuildListener() {
            @Override
            public void buildStarted(Gradle gradle) {
            }

            @Override
            public void settingsEvaluated(Settings settings) {

            }

            @Override
            public void projectsLoaded(Gradle gradle) {
            }

            @Override
            public void projectsEvaluated(Gradle gradle) {
                //Without init, means don't make configuration depends on tasks from api projects.
                createAndPopulateStuffApiConfig(project);
            }

            @Override
            public void buildFinished(BuildResult result) {

            }
        });

        project.getTasks().getAt(JavaBasePlugin.BUILD_TASK_NAME).doLast(task -> {
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
                    .findFirst()
                    .ifPresent(dependencyHolder -> {
                        if (!Objects.equals(dependencyHolder.getVersion(), prjDep.getVersion())) {
                            throw new GradleException(format("It is not allowed to use 3rd party dependencies from scope of all api subprojects and different version. " +
                                            "Project \"%s\" has other version common dependency: \"%s\". Must be used %s!",
                                    project.getPath(), prjDep, dependencyHolder.getVersion()));
                        }
                    });

        }
    }
}
