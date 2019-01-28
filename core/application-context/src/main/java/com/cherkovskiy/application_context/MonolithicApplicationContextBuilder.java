package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.ApplicationContext;
import com.cherkovskiy.application_context.api.Bundle;
import com.cherkovskiy.application_context.api.ContextBuilder;
import com.cherkovskiy.application_context.api.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.configuration.environments.ConfigurationImpl;
import com.cherkovskiy.application_context.configuration.environments.StandardConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MonolithicApplicationContextBuilder implements ContextBuilder {
    @Override
    public ContextBuilder setArguments(String[] args) {
        throw new UnsupportedOperationException("It is not supported yet.");
    }


    //TODO: разобраться с исключениями
    @Override
    public ApplicationContext build() throws IOException {
        //TODO: реализация спеки пункт 7 (вся логика загрузки и инициализаиции тут. Потом растащу по классам и интерфейсам)
        //TODO: реализация всех рантайм проверок описанных в спеке

        //TODO: Собираем все бандлы в виде ResolvedBundleArtifact
        String appHome = System.getenv("APP_HOME");
        ApplicationResolver applicationResolver = new ApplicationResolver(appHome);
        ResolvedBundleArtifact resolvedAppBundleArtifact = applicationResolver.resolveApplicationBundle();
        List<ResolvedBundleArtifact> resolvedBundles = applicationResolver.resolveOtherBundles();

        //TODO: Идём по всем бандлам создаём для каждого Bundle объект
        final ConfigurationImpl globalConfiguration = StandardConfiguration.create();
        final Bundle appBundle = new LocalBundle(resolvedAppBundleArtifact, globalConfiguration);
        final List<Bundle> localBundles = resolvedBundles.stream()
                .map(resolvedBundleArtifact -> new LocalBundle(resolvedBundleArtifact, globalConfiguration))
                .collect(Collectors.toList());

        //TODO: грузим import сервисы
        //List<Bundle> remoteBundle = //TODO

        MonolithicApplicationContext context = new MonolithicApplicationContext(appBundle, localBundles);
        context.init();
        return context;
    }
}
