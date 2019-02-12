package com.cherkovskiy.application_starter;

import com.cherkovskiy.application_context.Launcher;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class LauncherProxy implements Consumer<String[]> {

    @Override
    public void accept(String[] args) {
        startApplication(args);
    }

    private void startApplication(@Nonnull String[] args) {
        final ApplicationContextClassLoader applicationContextClassLoader = (ApplicationContextClassLoader) Thread.currentThread().getContextClassLoader();
        final ApplicationRootClassLoaderSkeleton applicationRootClassLoaderSkeleton = (ApplicationRootClassLoaderSkeleton) applicationContextClassLoader.getParent();

        final Launcher launcher = new Launcher(
                new ApplicationRootClassLoaderBridge(applicationRootClassLoaderSkeleton),
                applicationContextClassLoader,
                args
        );
        launcher.startApplication();
    }

}
