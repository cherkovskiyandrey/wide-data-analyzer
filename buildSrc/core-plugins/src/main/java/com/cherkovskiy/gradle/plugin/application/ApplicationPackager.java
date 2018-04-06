package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.gradle.plugin.Utils;
import com.cherkovskiy.gradle.plugin.bundle.BundlePackager;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.jvm.tasks.Jar;

import javax.annotation.Nonnull;
import java.util.Collections;


public class ApplicationPackager implements Plugin<Project> {


    @Override
    public void apply(@Nonnull Project project) {
        project.getConfigurations().create("onboard");

        project.apply(Collections.singletonMap("plugin", BundlePackager.class));

        final ApplicationPackagerConfiguration configuration = project.getExtensions().create(ApplicationPackagerConfiguration.NAME, ApplicationPackagerConfiguration.class);

        project.getTasks().getAt("build").doLast(task -> {
            final Jar jarTask = project.getTasks().withType(Jar.class).iterator().next();
            final String rootGroupName = Utils.lookUpRootGroupName(project);

            System.out.println("configuration.format: " + configuration.format);
        });
    }
}
