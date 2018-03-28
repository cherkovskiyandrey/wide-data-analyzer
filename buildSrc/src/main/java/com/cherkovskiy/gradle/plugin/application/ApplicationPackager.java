package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.gradle.plugin.Utils;
import com.cherkovskiy.gradle.plugin.bundle.BundlePackager;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.jvm.tasks.Jar;

public class ApplicationPackager implements Plugin<Project> {


    @Override
    public void apply(Project project) {
        // Apply BundlePackager plugin first of all
        project.getPluginManager().apply(BundlePackager.class);

        project.getTasks().withType(Jar.class).forEach(jar -> {
            jar.doLast(task -> {
                final Jar jarTask = (Jar) task;
                final String rootGroupName = Utils.lookUpRootGroupName(project);

                //todo

            });
        });
    }
}
