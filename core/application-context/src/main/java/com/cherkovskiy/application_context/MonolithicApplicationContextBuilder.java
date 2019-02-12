package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.*;
import com.cherkovskiy.application_context.configuration.environments.ConfigurationImpl;
import com.cherkovskiy.application_context.configuration.environments.StandardConfiguration;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MonolithicApplicationContextBuilder implements ContextBuilder {

    @Nonnull
    private ApplicationRootClassLoader rootClassLoader;

    @Override
    public ContextBuilder setArguments(String[] args) {
        //TODO: запихать аргументы в ConfigurationImpl
        return this;
    }

    @Override
    public ContextBuilder setRootClassLoader(ApplicationRootClassLoader rootClassLoader) {
        this.rootClassLoader = rootClassLoader;
        return this;
    }


    //TODO: разобраться с исключениями
    @Override
    public MonolithicApplicationContext build() throws IOException {
        Preconditions.checkNotNull(rootClassLoader);

        String appHome = System.getenv("APP_HOME");
        ApplicationResolver applicationResolver = new ApplicationResolver(appHome);
        ResolvedBundleArtifact resolvedAppBundleArtifact = applicationResolver.resolveApplicationBundle();
        List<ResolvedBundleArtifact> resolvedBundles = applicationResolver.resolveOtherBundles();

        final ConfigurationImpl globalConfiguration = StandardConfiguration.create();
        final Bundle appBundle = new LocalBundle(resolvedAppBundleArtifact, rootClassLoader, globalConfiguration);
        final List<Bundle> localBundles = resolvedBundles.stream()
                .map(resolvedBundleArtifact -> new LocalBundle(resolvedBundleArtifact, rootClassLoader, globalConfiguration))
                .collect(Collectors.toList());

        //TODO: грузим import сервисы
        //List<Bundle> remoteBundle = //TODO

        return new MonolithicApplicationContext(appBundle, localBundles);
    }
}
