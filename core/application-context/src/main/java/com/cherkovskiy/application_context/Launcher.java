package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.ApplicationRootClassLoader;
import com.cherkovskiy.application_context.api.ContextBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URLClassLoader;

public class Launcher {
    @Nonnull
    private final ApplicationRootClassLoader applicationRootClassLoader;
    @Nonnull
    private final URLClassLoader applicationContextClassLoader;
    @Nonnull
    private final String[] args;

    public Launcher(
            @Nonnull ApplicationRootClassLoader applicationRootClassLoader,
            @Nonnull URLClassLoader applicationContextClassLoader,
            @Nonnull String[] args) {
        this.applicationRootClassLoader = applicationRootClassLoader;
        this.applicationContextClassLoader = applicationContextClassLoader;
        this.args = args;
    }

    public void startApplication() {
        final ContextBuilder contextBuilder = new MonolithicApplicationContextBuilder();

        final MonolithicApplicationContext context;
        try {
            context = contextBuilder
                    .setArguments(args)
                    .setRootClassLoader(applicationRootClassLoader)
                    .build();
            context.init();
        } catch (IOException e) {
            //todo: to log file
            throw new RuntimeException(e);//todo
        }

        Runtime.getRuntime().removeShutdownHook(new Thread(() -> {
            context.destroy();
            try {
                applicationContextClassLoader.close();
            } catch (IOException e) {
                //todo: to log file
            }
        }));
    }
}
