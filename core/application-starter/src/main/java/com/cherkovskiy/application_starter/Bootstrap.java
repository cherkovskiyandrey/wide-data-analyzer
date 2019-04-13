package com.cherkovskiy.application_starter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Vanilla java only.
 */
public class Bootstrap {


    //TODO: вот как делаем:

    /**
     * API и COMMON грузим в системный лоадер, т.к. пока не предполагаем что контекст - это отдельный бандл с возможность перезагрузки и т.п.
     * Создаём ApplicationContextClassLoader с возможностью перезадать ему паранта.
     * Уходим в него.
     * Там создаём ApplicationRootClassLoader, парента ему ставим системного, берём нашего ApplicationContextClassLoader и переставляем ему парента на ApplicationRootClassLoader.
     * ----------------
     * 2 вариант, менее приоритетный.
     * Всё грузим ка и было, но пишем интерфейс для IApplicationRootClassLoader, уже выполняясь в ApplicationContextClassLoader,
     * создаём бридж IApplicationRootClassLoader -> ApplicationRootClassLoader и отдаём контексту.
     * Минус: ApplicationRootClassLoader должен быть чистым от все зависимостей,
     * Плюс: не выставляем контекстное апи в системный лоадер - на будущее плюс в том что можно относится к контексту как к отдельному бандлу.
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String appHome = System.getenv("APP_HOME");
        if (appHome == null) {
            throw new IllegalStateException("Environment variable APP_HOME is not set.");
        }

        final Manifest starterManifest;
        try (InputStream manifestInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/MANIFEST.MF")) {
            starterManifest = new Manifest(manifestInputStream);
        }

        List<URL> starterApiDependencies = getURLs(starterManifest.getMainAttributes().getValue("WDA-Starter-Api-Dependencies"), appHome);
        List<URL> starterCommonDependencies = getURLs(starterManifest.getMainAttributes().getValue("WDA-Starter-Common-Dependencies"), appHome);

        final ApplicationRootClassLoaderSkeleton applicationBootstrapClassLoader = new ApplicationRootClassLoaderSkeleton(
                Stream.concat(starterApiDependencies.stream(), starterCommonDependencies.stream()).collect(Collectors.toList()),
                ClassLoader.getSystemClassLoader()
        );

        URL starter = Bootstrap.class.getProtectionDomain().getCodeSource().getLocation();
        List<URL> internalDependencies = getURLs(starterManifest.getMainAttributes().getValue("WDA-Starter-Internal-Dependencies"), appHome);
        internalDependencies.add(starter);
        List<URL> _3rdPartyDependencies = getURLs(starterManifest.getMainAttributes().getValue("WDA-Starter-3rdParty-Dependencies"), appHome);

        final ApplicationContextClassLoader applicationContextClassLoader = new ApplicationContextClassLoader(
                Stream.concat(internalDependencies.stream(), _3rdPartyDependencies.stream()).toArray(URL[]::new),
                applicationBootstrapClassLoader
        );

        Thread.currentThread().setContextClassLoader(applicationContextClassLoader);
        @SuppressWarnings("unchecked")
        Class<? extends Consumer<String[]>> bootstrap = (Class<? extends Consumer<String[]>>) applicationContextClassLoader.loadClass(
                LauncherProxy.class.getName()
        );
        bootstrap.newInstance().accept(args);
    }

    private static List<URL> getURLs(String dependencyList, String appHome) throws MalformedURLException {
        List<URL> result = new ArrayList<>();
        for (String dep : dependencyList.split(",")) {
            result.add(Paths.get(appHome, dep).toUri().toURL());
        }
        return result;
    }
}
