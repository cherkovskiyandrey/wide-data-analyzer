package com.cherkovskiy.gradle.plugin;

import com.google.common.collect.ImmutableMap;
import org.slieb.throwables.FunctionWithThrowable;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ServicesClassLoader extends URLClassLoader {
    private final ImmutableMap<String, DependencyHolder> urlToHolder;

    ServicesClassLoader(File rootArtifactFile, List<DependencyHolder> dependencies, ClassLoader parent) {
        super(toUrls(rootArtifactFile, dependencies), parent);

        this.urlToHolder = ImmutableMap.<String, DependencyHolder>builder()
                .putAll(toMapToUrl(dependencies))
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    private static Map<String, DependencyHolder> toMapToUrl(List<DependencyHolder> dependencies) {
        return dependencies.stream()
                .filter(d -> d.getFile().isPresent())
                .collect(Collectors.toMap(
                        FunctionWithThrowable.castFunctionWithThrowable(dh -> dh.getFile().get().toURI().toURL().toString()),
                        Function.identity(),
                        (l, r) -> r
                ));
    }

    @SuppressWarnings("ConstantConditions")
    private static URL[] toUrls(File rootArtifactFile, List<DependencyHolder> dependencies) {
        return Stream.concat(Stream.of(rootArtifactFile), dependencies.stream().filter(d -> d.getFile().isPresent()).map(d -> d.getFile().get()))
                .map(FunctionWithThrowable.castFunctionWithThrowable(artifact -> artifact.toURI().normalize().toURL()))
                .toArray(URL[]::new);
    }

    public Optional<DependencyHolder> getDependencyHolder(@Nonnull Class<?> cls) {
        String resource = cls.getName().replace('.', '/').concat(".class");

        return Optional.ofNullable(findResource(resource))
                .map(URL::toString)
                .flatMap(url -> urlToHolder.entrySet().stream()
                        .filter(entry -> url.startsWith(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst());
    }
}
