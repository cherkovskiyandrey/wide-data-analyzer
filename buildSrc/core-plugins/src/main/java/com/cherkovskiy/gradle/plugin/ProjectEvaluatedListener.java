package com.cherkovskiy.gradle.plugin;

import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;

@FunctionalInterface
public interface ProjectEvaluatedListener extends BuildListener {
    @Override
    default void buildStarted(Gradle gradle) {
    }

    @Override
    default void settingsEvaluated(Settings settings) {
    }

    @Override
    default void projectsLoaded(Gradle gradle) {
    }

    @Override
    default void buildFinished(BuildResult result) {
    }
}
