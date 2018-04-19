package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.gradle.plugin.ResolvedBundleArtifact;
import com.cherkovskiy.gradle.plugin.bundle.BundlePackager;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaBasePlugin;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static com.cherkovskiy.gradle.plugin.application.OnboardResolver.ONBOARD_CONF_NAME;


public class ApplicationPackager implements Plugin<Project> {

    @Override
    public void apply(@Nonnull Project project) {
        final Configuration onboard = project.getConfigurations().create(ONBOARD_CONF_NAME);
        final Task buildTask = project.getTasks().getAt(JavaBasePlugin.BUILD_TASK_NAME);
        buildTask.dependsOn(onboard.getTaskDependencyFromProjectDependency(true, JavaBasePlugin.BUILD_NEEDED_TASK_NAME));

        project.apply(Collections.singletonMap("plugin", BundlePackager.class));

        final ApplicationPackagerConfiguration configuration = project.getExtensions().create(ApplicationPackagerConfiguration.NAME, ApplicationPackagerConfiguration.class);

        buildTask.doLast(task -> {

            try (final OnboardResolver onboardResolver = new OnboardResolver(project)) {
                final Set<ResolvedBundleArtifact> bundles = onboardResolver.getBundles();

                //todo: compare all common by versions and against impl in each bundle + compare all api versions

                //todo: put dependencies in right places

            } catch (IOException e) {
                throw new GradleException(e.getMessage(), e);
            }
            System.out.println("configuration.format: " + configuration.format);
        });
    }

}
