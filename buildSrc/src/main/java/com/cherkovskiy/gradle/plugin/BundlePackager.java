package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.application_context.api.annotations.Service;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.jvm.tasks.Jar;
import org.slieb.throwables.FunctionWithThrowable;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class BundlePackager implements Plugin<Project> {
    private final static String BUNDLE_NAME = "BUNDLE_NAME";
    private final static String BUNDLE_VERSION = "BUNDLE_VERSION";
    private final static String EXPORTED_SERVICES = "EXPORTED_SERVICES";
    private static final String MANIFEST_ENTRY_NAME = "META-INF/MANIFEST.MF";

    @Override
    public void apply(@Nonnull Project project) {
        project.getTasks().withType(Jar.class).forEach(jar -> {
            jar.doLast(task -> {
                final Jar jarTask = (Jar) task;

                final String rootGroupName = lookUpRootGroupName(project);
                //TODO: запрещать ипортировать другие бандлы, плагины и приложения

                final List<File> dependencies = getCompileDependencies(project);

                final List<ServiceDescription> serviceDescriptions = extractAllServicesFrom(jarTask.getArchivePath(), dependencies);
                addToManifest(jarTask.getArchivePath(), jarTask.getBaseName(), jarTask.getVersion(), serviceDescriptions);
            });
        });
    }


    private String lookUpRootGroupName(Project project) {
        for (Project parent = project; ; parent = parent.getParent()) {
            if (parent.getParent() == null) {
                return (String) parent.getGroup();
            }
        }
    }


    private List<File> getCompileDependencies(Project project) {
        final List<File> dependencies = Lists.newArrayList();
        for (ResolvedDependency resolvedDependency : project.getConfigurations()
                .getByName("compileClasspath")
                .getResolvedConfiguration()
                .getFirstLevelModuleDependencies()) {

            walkDependency(resolvedDependency, dependencies);
        }
        return dependencies;
    }

    private void walkDependency(ResolvedDependency resolvedDependency, List<File> dependencies) {
        dependencies.add(resolvedDependency.getModuleArtifacts().iterator().next().getFile());
        resolvedDependency.getChildren().forEach(childDependency -> walkDependency(childDependency, dependencies));
    }


    private List<ServiceDescription> extractAllServicesFrom(File rootArtifactFile, List<File> dependencies) {
        final URL[] jars = Stream.concat(Stream.of(rootArtifactFile), dependencies.stream())
                .map(FunctionWithThrowable.castFunctionWithThrowable(jar -> jar.toURI().normalize().toURL()))
                .toArray(URL[]::new);

        // We use parent class loader because this plugin uses outside sources.
        // URLClassLoader has to load Service class from current loader.
        final URLClassLoader classLoader = new URLClassLoader(jars, Thread.currentThread().getContextClassLoader());

        try {
            return getAllClassesName(rootArtifactFile).stream()
                    .map(FunctionWithThrowable.castFunctionWithThrowable(name -> Class.forName(name, false, classLoader)))
                    .filter(cls -> cls.isAnnotationPresent(Service.class))
                    .peek(this::checkRestrictions)
                    .map(cls -> toServiceDescription(cls, classLoader))
                    .collect(Collectors.toList());


        } catch (IOException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }

    private void checkRestrictions(Class<?> cls) {
        if (cls.isLocalClass()) {
            throw new GradleException("Local class could not be service!");
        }
        if (cls.isInterface()) {
            throw new GradleException("Interfaces could not be service!");
        }
        if (cls.isAnonymousClass()) {
            throw new GradleException("Anonymous class could not be service!");
        }
        if (cls.isMemberClass()) {
            throw new GradleException("Member class could not be service!");
        }
        if (cls.isEnum()) {
            throw new GradleException("Enum class could not be service!");
        }
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new GradleException("Abstract class could not be service!");
        }
    }

    private ServiceDescription toServiceDescription(Class<?> cls, URLClassLoader classLoader) {
        final List<Class<?>> implInterfaces = Lists.newArrayList();
        walkClass(cls, implInterfaces);

        final Service service = cls.getAnnotation(Service.class);
        final String name = StringUtils.isNotBlank(service.value()) ? service.value() :
                StringUtils.isNotBlank(service.name()) ? service.name() : "";

        final Service.Type type = service.type();
        final Service.InitType initType = service.initType();

        return ServiceDescription.builder()
                .setServiceImplName(cls.getName())
                .setServiceName(name)
                .setType(type)
                .setInitType(initType)
                .setInterfaces(implInterfaces.stream().map(Class::getName).collect(Collectors.toList()))
                .build();
    }

    private void walkClass(Class<?> cls, List<Class<?>> implInterfaces) {
        if (!Object.class.equals(cls)) {
            walkAllInterfaces(cls, implInterfaces);
            walkClass(cls.getSuperclass(), implInterfaces);
        }
    }

    private void walkAllInterfaces(Class<?> cls, List<Class<?>> implInterfaces) {
        implInterfaces.addAll(Arrays.asList(cls.getInterfaces()));
        Arrays.asList(cls.getInterfaces()).forEach(i -> walkAllInterfaces(i, implInterfaces));
    }

    private List<String> getAllClassesName(File rootArtifact) throws IOException {
        try (JarFile jarFile = new JarFile(rootArtifact)) {
            return jarFile.stream()
                    .map(ZipEntry::getName)
                    .filter(name -> name.endsWith(".class"))
                    .map(name -> name.replace(".class", ""))
                    .map(name -> name.replace('/', '.'))
                    .collect(Collectors.toList());
        }
    }

    private void addToManifest(File archivePath, String bundleName, String bundleVersion, List<ServiceDescription> serviceDescriptions) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024)) {
            try (final JarFile jarFile = new JarFile(archivePath)) {

                final Manifest manifest = new Manifest(jarFile.getManifest());
                final Attributes attributes = manifest.getMainAttributes();

                attributes.put(new Attributes.Name(BUNDLE_NAME), bundleName);
                attributes.put(new Attributes.Name(BUNDLE_VERSION), bundleVersion);

                final String services = serviceDescriptions.stream()
                        .map(ServiceDescription::toManifestCompatibleString)
                        .collect(Collectors.joining(";"));

                attributes.put(new Attributes.Name(EXPORTED_SERVICES), services);

                try (JarOutputStream jarOutputStream = new JarOutputStream(byteArrayOutputStream, manifest)) {
                    jarFile.stream()
                            .filter(jarEntry -> !MANIFEST_ENTRY_NAME.equalsIgnoreCase(jarEntry.getName()))
                            .forEach(jarEntry -> {
                                try {
                                    jarOutputStream.putNextEntry(jarEntry);
                                    try (final InputStream jarEntryStream = jarFile.getInputStream(jarEntry)) {
                                        IOUtils.copyLarge(jarEntryStream, jarOutputStream);
                                    }
                                    jarOutputStream.closeEntry();
                                } catch (IOException e) {
                                    throw new GradleException("Could not copy artifact: " + archivePath.getAbsolutePath(), e);
                                }
                            });
                }
            }
            Files.write(archivePath.toPath(), byteArrayOutputStream.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new GradleException("Could not open artifact: " + archivePath.getAbsolutePath(), e);
        }
    }
}
