package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.gradle.plugin.ProjectEvaluatedListener;
import com.cherkovskiy.application_context.api.Dependency;
import com.cherkovskiy.application_context.api.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.api.ResolvedDependency;
import com.cherkovskiy.application_context.api.ResolvedStarterArtifact;
import com.cherkovskiy.gradle.plugin.bundle.BundlePackagerConfiguration;
import com.cherkovskiy.gradle.plugin.bundle.BundlePlugin;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.jvm.tasks.Jar;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cherkovskiy.gradle.plugin.Utils.getOrCreateConfig;
import static com.cherkovskiy.gradle.plugin.bundle.BundlePlugin.ASSEMBLE_BUNDLE_TASK_NAME;
import static java.lang.String.format;
import static java.util.stream.Collectors.*;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;


public class ApplicationPlugin implements Plugin<Project> {
    public static String ASSEMBLE_APPLICATION_TASK_NAME = "assembleApplication";
    public static final String APP_POSTFIX = "app";

    @Override
    public void apply(@Nonnull Project project) {
        OnboardResolver.createConfiguration(project);

        final BundlePackagerConfiguration bundlePluginConfig = getOrCreateConfig(project, BundlePackagerConfiguration.NAME, BundlePackagerConfiguration.class);
        bundlePluginConfig.failIfNotBundle = false;
        project.apply(ImmutableMap.of("plugin", BundlePlugin.class));

        final ApplicationPackagerConfiguration configuration = getOrCreateConfig(project, ApplicationPackagerConfiguration.NAME, ApplicationPackagerConfiguration.class);
        project.getGradle().addListener((ProjectEvaluatedListener) gradle -> {

            // Create new task and set in a DAG
            final Task assembleApplicationTask = project.getTasks().create(ASSEMBLE_APPLICATION_TASK_NAME);
            assembleApplicationTask.dependsOn(project.getTasks().getAt(ASSEMBLE_BUNDLE_TASK_NAME));
            project.getTasks().getAt(ASSEMBLE_TASK_NAME).dependsOn(assembleApplicationTask);

            assembleApplicationTask.setActions(Collections.singletonList(task -> {
                try (final OnboardResolver onboardResolver = new OnboardResolver(project, configuration)) {
                    final Optional<ResolvedBundleArtifact> currentBundle = onboardResolver.getCurrentBundle();
                    final Set<ResolvedBundleArtifact> bundles = onboardResolver.getBundles();
                    ResolvedStarterArtifact applicationStarter = onboardResolver.getApplicationStarter();
                    final Set<ResolvedBundleArtifact> allBundles = Sets.newHashSet(bundles);
                    currentBundle.ifPresent(allBundles::add);

                    if (Objects.isNull(applicationStarter)) {
                        throw new GradleException(format("Could not declared application starter in %s configuration", OnboardResolver.ONBOARD_CONF_NAME));
                    }

                    final boolean isCorrected = checkCommonDependencies(allBundles, configuration.failOnErrors, project.getLogger());

                    if (isCorrected) {
                        checkImplExternalDependencies(allBundles, configuration.failOnErrors, project.getLogger());
                    }
                    checkApiVersions(allBundles, configuration.failOnErrors, project.getLogger());

                    //TODO: логика обработки remote services см. пункт 4.2 спеки
                    //--------------
                    checkUnprovidedApi(allBundles, applicationStarter.getApi(), configuration.failOnErrors, project.getLogger());
                    //--------------

                    applicationStarter = StarterPatcher.patch(applicationStarter, task.getTemporaryDir());
                    final Jar jarTask = project.getTasks().withType(Jar.class).iterator().next();

                    //[baseName]-[appendix]-[version]-[classifier]
                    final String targetArtifact = Paths.get(jarTask.getDestinationDir().getAbsolutePath(),
                            Joiner.on("-").skipNulls().join(
                                    StringUtils.defaultIfBlank(jarTask.getBaseName(), null),
                                    StringUtils.defaultIfBlank(jarTask.getAppendix(), null),
                                    StringUtils.defaultIfBlank(jarTask.getVersion(), null),
                                    StringUtils.defaultIfBlank(jarTask.getClassifier(), null),
                                    APP_POSTFIX
                            ) + "." + configuration.format)
                            .toFile().getAbsolutePath();

                    try (ApplicationPackager applicationPackager = new ApplicationPackager(targetArtifact)) {
                        final String rootProjectPath = project.getRootProject().getRootDir().getAbsolutePath();
                        final File resourcesPath = Paths.get(rootProjectPath, configuration.pathToBinsResources).toFile();

                        applicationPackager.copyResources(resourcesPath, "bin");
                        applicationPackager.putApplicationStarter(applicationStarter.getFile());

                        applicationPackager.putApi(getDependenciesBy(allBundles, artifact ->
                                ImmutableList.<ResolvedDependency>builder()
                                        .addAll(artifact.getApiExport())
                                        .addAll(artifact.getApiImport())
                                        .build()));

                        if (currentBundle.isPresent()) {
                            applicationPackager.putAppBundle(currentBundle.get());
                        }
                        applicationPackager.putBundles(bundles);

                        applicationPackager.putCommon(applicationStarter.getCommon());
                        applicationPackager.putCommon(getDependenciesBy(allBundles, ResolvedBundleArtifact::getCommon));

                        applicationPackager.putExternal(applicationStarter.get3rdParty());
                        applicationPackager.putExternal(getDependenciesBy(allBundles, ResolvedBundleArtifact::getImplExternal));

                        applicationPackager.putInternal(applicationStarter.getInternal());
                        applicationPackager.putInternal(getDependenciesBy(allBundles, ResolvedBundleArtifact::getImplInternal));
                    }
                } catch (IOException e) {
                    throw new GradleException(e.getMessage(), e);
                }
            }));
        });
    }

    private Set<ResolvedDependency> getDependenciesBy(Set<ResolvedBundleArtifact> allBundles, Function<ResolvedBundleArtifact, Collection<ResolvedDependency>> extractor) {
        return allBundles.stream()
                .flatMap(artifact -> extractor.apply(artifact).stream())
                .collect(toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    private boolean checkUnprovidedApi(Set<ResolvedBundleArtifact> allBundles, Set<ResolvedDependency> excludedApi, boolean failOnErrors, Logger logger) {
        final Set<ResolvedDependency> exportApi = allBundles.stream()
                .flatMap(bundle -> bundle.getApiExport().stream())
                .collect(toCollection(() -> Sets.newTreeSet(ResolvedDependency.COMPARATOR)));

        final Map<ResolvedDependency, Set<ResolvedBundleArtifact>> importApi = allBundles.stream()
                .flatMap(bundle -> bundle.getApiImport().stream().map(api -> Pair.of(api, bundle)))
                .collect(groupingBy(Pair::getLeft,
                        () -> new TreeMap<>(ResolvedDependency.COMPARATOR),
                        mapping(Pair::getRight, toCollection(() -> Sets.newTreeSet(ResolvedBundleArtifact.COMPARATOR)))));


        final List<String> errors = Lists.newArrayList();
        for (Map.Entry<ResolvedDependency, Set<ResolvedBundleArtifact>> entry : importApi.entrySet()) {
            if (!exportApi.contains(entry.getKey()) && !excludedApi.contains(entry.getKey())) {
                errors.add(format("Next bundles required of api \"%s\". But there is not bundle with implementations. Bundles: %s",
                        Dependency.toString(entry.getKey()),
                        entry.getValue().stream().map(artifact -> ResolvedBundleArtifact.toString(artifact)).collect(joining("; "))
                ));
            }
        }

        String errorsAsStr = errors.stream().collect(Collectors.joining(System.lineSeparator()));
        if (StringUtils.isNotBlank(errorsAsStr)) {
            if (failOnErrors) {
                throw new GradleException(errorsAsStr);
            }

            logger.log(LogLevel.ERROR, errorsAsStr);
            return false;
        }

        return true;
    }

    private boolean checkCommonDependencies(Set<ResolvedBundleArtifact> allBundles, boolean failOnErrors, Logger logger) {
        return checkUniquesDependencies(allBundles, bundle -> bundle.getCommon().stream(), failOnErrors, logger);
    }

    private boolean checkApiVersions(Set<ResolvedBundleArtifact> allBundles, boolean failOnErrors, Logger logger) {
        return checkUniquesDependencies(allBundles, bundle -> Stream.concat(bundle.getApiExport().stream(), bundle.getApiImport().stream()), failOnErrors, logger);
    }


    private boolean checkUniquesDependencies(Set<ResolvedBundleArtifact> allBundles,
                                             Function<ResolvedBundleArtifact, Stream<ResolvedDependency>> dependencyProducer,
                                             boolean failOnErrors,
                                             Logger logger) {
        final Map<String, List<Pair<ResolvedBundleArtifact, ResolvedDependency>>> depToBundle = allBundles.stream()
                .flatMap(bundle -> dependencyProducer.apply(bundle).map(dep -> Pair.of(bundle, dep)))
                .collect(groupingBy(pair -> String.join(":", pair.getRight().getGroup(), pair.getRight().getName()), toList()));

        final List<String> errors = Lists.newArrayList();
        for (Map.Entry<String, List<Pair<ResolvedBundleArtifact, ResolvedDependency>>> entry : depToBundle.entrySet()) {
            String version = null;

            for (Pair<ResolvedBundleArtifact, ResolvedDependency> dep : entry.getValue()) {
                if (Objects.isNull(version)) {
                    version = dep.getRight().getVersion();

                } else if (!version.equalsIgnoreCase(dep.getRight().getVersion())) {
                    errors.add(format("Conflict version of dependency in bundle %s. Use everywhere %s, but in this bundle - %s.",
                            dep.getLeft().getName(),
                            String.join(":", entry.getKey(), version),
                            String.join(":", entry.getKey(), dep.getRight().getVersion()))
                    );
                }
            }
        }

        String errorsAsStr = errors.stream().collect(Collectors.joining(System.lineSeparator()));
        if (StringUtils.isNotBlank(errorsAsStr)) {
            if (failOnErrors) {
                throw new GradleException(errorsAsStr);
            }

            logger.log(LogLevel.ERROR, errorsAsStr);
            return false;
        }

        return true;
    }


    private boolean checkImplExternalDependencies(Set<ResolvedBundleArtifact> allBundles, boolean failOnErrors, Logger logger) {
        final Set<ResolvedDependency> allCommon = allBundles.stream()
                .flatMap(b -> b.getCommon().stream())
                .collect(toCollection(() -> Sets.newTreeSet(ResolvedDependency.COMPARATOR)));

        final Map<ResolvedDependency, Set<ResolvedBundleArtifact>> depImplToBundles = allBundles.stream()
                .flatMap(bundle -> bundle.getImplExternal().stream().map(impl -> Pair.of(impl, bundle)))
                .collect(groupingBy(Pair::getLeft,
                        () -> new TreeMap<>(ResolvedDependency.COMPARATOR),
                        mapping(Pair::getRight, toCollection(() -> Sets.newTreeSet(ResolvedBundleArtifact.COMPARATOR)))));


        final List<String> errors = Lists.newArrayList();
        for (ResolvedDependency common : allCommon) {

            depImplToBundles.entrySet().stream()
                    .filter(impl -> Objects.equals(impl.getKey().getName(), common.getName()) &&
                            Objects.equals(impl.getKey().getGroup(), common.getGroup()) &&
                            !Objects.equals(impl.getKey().getVersion(), common.getVersion()))
                    .forEach(impl -> {
                        for (ResolvedBundleArtifact bundle : impl.getValue()) {
                            errors.add(format("Conflict version between common and impl external dependency in bundle %s. Common %s, impl: %s.",
                                    bundle.getName(),
                                    Dependency.toString(common),
                                    Dependency.toString(impl.getKey())));
                        }
                    });
        }

        String errorsAsStr = errors.stream().collect(Collectors.joining(System.lineSeparator()));
        if (StringUtils.isNotBlank(errorsAsStr)) {
            if (failOnErrors) {
                throw new GradleException(errorsAsStr);
            }

            logger.log(LogLevel.ERROR, errorsAsStr);
            return false;
        }

        return true;
    }

}
