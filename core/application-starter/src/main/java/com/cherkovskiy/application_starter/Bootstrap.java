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
import java.util.stream.Stream;

/**
 * Vanilla java only.
 */
public class Bootstrap {


    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String appHome = System.getenv("APP_HOME");
        if (appHome == null) {
            throw new IllegalStateException("Environment variable APP_HOME is not set.");
        }

        final Manifest starterManifest;
        try (InputStream manifestInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/MANIFEST.MF")) {
            starterManifest = new Manifest(manifestInputStream);
        }

        //TODO: убрать по возможности дублировани кода: com.cherkovskiy.application_context.StarterDependencyGroup
        List<URL> starterApiDependencies = getURLs(starterManifest.getMainAttributes().getValue("WDA-Starter-Api-Dependencies"), appHome);
        List<URL> starterCommonDependencies = getURLs(starterManifest.getMainAttributes().getValue("WDA-Starter-Common-Dependencies"), appHome);

        final ApplicationBootstrapClassLoader applicationBootstrapClassLoader = new ApplicationBootstrapClassLoader(
                Stream.concat(starterApiDependencies.stream(), starterCommonDependencies.stream()).toArray(URL[]::new),
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
                Starter.class.getName()
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
