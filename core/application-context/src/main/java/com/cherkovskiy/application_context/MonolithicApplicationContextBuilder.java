package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        return new MonolithicApplicationContext();
    }
}
