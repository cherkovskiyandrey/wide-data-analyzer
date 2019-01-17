package com.cherkovskiy.gradle.plugin;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.slieb.throwables.FunctionWithThrowable;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ServicesClassLoader extends URLClassLoader {
    private final ImmutableMap<String, DependencyHolder> urlToHolder;

    public ServicesClassLoader(File rootArtifactFile, List<DependencyHolder> dependencies, ClassLoader parent) {
        super(toUrls(rootArtifactFile, dependencies), parent);

        this.urlToHolder = ImmutableMap.<String, DependencyHolder>builder()
                .putAll(toMapToUrl(dependencies))
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    private static Map<String, DependencyHolder> toMapToUrl(List<DependencyHolder> dependencies) {
        return dependencies.stream()
                .flatMap(d -> d.getArtifacts().stream().map(artifact -> Pair.of(artifact, d)))
                .collect(Collectors.toMap(
                        FunctionWithThrowable.castFunctionWithThrowable(dh -> unwrapUrl(dh.getLeft().toURI().normalize().toURL())),
                        Pair::getRight,
                        (l, r) -> r
                ));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static String unwrapUrl(URL url) {
        while (true) {
            try {
                //unfortunately only this approach is worked or custom parser
                // I didn't use resourceUrl.toString().contain(knownUrl.toString()) because:
                // -->> /usr/gradle/caches/modules-2/files-2/
                // -->> jar:file:/usr/gradle/usr/gradle/caches/modules-2/files-2/api-neural-network-1.0-SNAPSHOT.jar!/com/cherkovskiy/neuron_networks/api/NeuronNetworkService.class
                url = new URL(url.getFile());
            } catch (MalformedURLException e) {
                return url.getPath();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static URL[] toUrls(File rootArtifactFile, List<DependencyHolder> dependencies) {
        return Stream.concat(Stream.of(rootArtifactFile), dependencies.stream().flatMap(d -> d.getArtifacts().stream()))
                .distinct()
                .map(FunctionWithThrowable.castFunctionWithThrowable(artifact -> artifact.toURI().normalize().toURL()))
                .toArray(URL[]::new);
    }

    public Optional<DependencyHolder> getDependencyHolder(@Nonnull Class<?> cls) {
        String resource = cls.getName().replace('.', '/').concat(".class");

        return Optional.ofNullable(findResource(resource))
                .map(ServicesClassLoader::unwrapUrl)
                .flatMap(url -> urlToHolder.entrySet().stream()
                        .filter(entry -> url.startsWith(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst());
    }
}

