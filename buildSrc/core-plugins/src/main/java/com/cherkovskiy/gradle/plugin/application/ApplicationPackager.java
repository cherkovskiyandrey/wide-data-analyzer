package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.gradle.plugin.SubProjectTypes;
import com.cherkovskiy.gradle.plugin.bundle.BundlePackager;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaBasePlugin;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;


public class ApplicationPackager implements Plugin<Project> {
    public static final String ONBOARD_CONF_NAME = "onboard";

    @Override
    public void apply(@Nonnull Project project) {
        final Configuration onboard = project.getConfigurations().create(ONBOARD_CONF_NAME);
        final Task buildTask = project.getTasks().getAt(JavaBasePlugin.BUILD_TASK_NAME);
        buildTask.dependsOn(onboard.getTaskDependencyFromProjectDependency(true, JavaBasePlugin.BUILD_NEEDED_TASK_NAME));

        project.apply(Collections.singletonMap("plugin", BundlePackager.class));

        final ApplicationPackagerConfiguration configuration = project.getExtensions().create(ApplicationPackagerConfiguration.NAME, ApplicationPackagerConfiguration.class);

        buildTask.doLast(task -> {

//            final BundleResolver bundleResolver = new BundleResolver(project, ONBOARD_CONF_NAME);
//            final Set<BundleHolder> bundles = bundleResolver.getBundles();

            //todo: compare all common by versions and against impl in each bundle + compare all api versions

            //todo: split api api from api impl for checking! - BundleResolver для внешних либ сможет прочитать манифест и определить какие api-export а какие import

            //todo: put dependencies in right places


            System.out.println("configuration.format: " + configuration.format);
        });
    }


    private boolean isBundle(Project depProject) {
        return SubProjectTypes.BUNDLE.getSubGroupName().equalsIgnoreCase(depProject.getGroup().toString());
    }
}
