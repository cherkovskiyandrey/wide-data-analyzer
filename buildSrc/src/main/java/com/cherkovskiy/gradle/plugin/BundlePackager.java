package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.application_context.api.annotations.Service;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.jvm.tasks.Jar;
import org.slieb.throwables.FunctionWithThrowable;
import org.slieb.throwables.SuppressedException;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static com.cherkovskiy.gradle.plugin.DependencyScanner.TransitiveMode.TRANSITIVE_OFF;
import static com.cherkovskiy.gradle.plugin.DependencyType.API;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class BundlePackager implements Plugin<Project> {
    private static final String BUNDLE_NAME = "BUNDLE_NAME";
    private static final String BUNDLE_VERSION = "BUNDLE_VERSION";
    private static final String EXPORTED_SERVICES = "EXPORTED_SERVICES";
    private static final String MANIFEST_ENTRY_NAME = "META-INF/MANIFEST.MF";
    private static final String ADI_DEPENDENCY_GROUP = "api";

    private static final ImmutableList<String> FORBIDDEN_TO_DEPENDS_ON_LIST = new ImmutableList.Builder<String>()
            .add("application")
            .add("bundle")
            .add("core")
            .add("plugin")
            .build();


    //Obtain dependency by name from dependencyManagement:
    //((Map<String, Dependency>)project.getProperties().get("dependencyManagement")).get("com.google.guava:guava").getName()


    @Override
    public void apply(@Nonnull Project project) {
        project.getTasks().withType(Jar.class).forEach(jar -> {
            jar.doLast(task -> {

                final Jar jarTask = (Jar) task;

                final String rootGroupName = lookUpRootGroupName(project);

                final DependencyScanner dependencyScanner = new DependencyScanner(project);
                final List<DependencyHolder> dependencies = dependencyScanner.getDependencies();

                //Bundle can export only services from api dependencies without transitive these api dependencies
                final List<DependencyHolder> resolvedByTypeDependencies = dependencyScanner.resolveAgainst(dependencies, DependencyType.API, TRANSITIVE_OFF);

                checkImportRestrictions(rootGroupName, resolvedByTypeDependencies);

                final List<ServiceDescription> serviceDescriptions = extractAllServicesFrom(rootGroupName, jarTask.getArchivePath(), resolvedByTypeDependencies);
                addToManifest(jarTask.getArchivePath(), jarTask.getBaseName(), jarTask.getVersion(), serviceDescriptions);
            });
        });
    }

    private void checkImportRestrictions(@Nonnull String rootGroupName, @Nonnull List<DependencyHolder> dependencies) {
        final List<DependencyHolder> forbiddenDependencies = dependencies.stream()
                .filter(dep -> {
                    final String group = dep.getGroup();

                    if (StringUtils.startsWith(group, rootGroupName)) {
                        final String subGroup = StringUtils.split(group.substring(rootGroupName.length()), '.')[0];

                        if (FORBIDDEN_TO_DEPENDS_ON_LIST.stream().anyMatch(frb -> frb.equalsIgnoreCase(subGroup))) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(toList());

        if (!forbiddenDependencies.isEmpty()) {
            throw new GradleException(String.format("Bundle could not depends on: %s. There is forbidden dependencies: %s",
                    FORBIDDEN_TO_DEPENDS_ON_LIST.stream().collect(joining(", ")),
                    forbiddenDependencies.stream().map(DependencyHolder::toString).collect(joining(", "))
            ));
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private String lookUpRootGroupName(@Nonnull Project project) {
        for (; project.getParent() != null; project = project.getParent()) {
        }
        return (String) project.getGroup();
    }

    private List<ServiceDescription> extractAllServicesFrom(String rootGroupName, File rootArtifactFile, List<DependencyHolder> dependencies) {
        // We use parent class loader because this plugin uses outside api sources.
        // URLClassLoader has to load Service class from current loader.
        final ServicesClassLoader classLoader = new ServicesClassLoader(rootArtifactFile, dependencies, Thread.currentThread().getContextClassLoader());

        try {
            final List<String> allClasses = getAllClassesName(rootArtifactFile);

            return SuppressedException.unwrapSuppressedException(() -> allClasses.stream()
                            .map(FunctionWithThrowable.castFunctionWithThrowable(name -> Class.forName(name, false, classLoader)))
                            .filter(cls -> cls.isAnnotationPresent(Service.class))
                            .peek(this::checkClassRestrictions)
                            .map(cls -> toServiceDescription(cls, classLoader, rootGroupName))
                            .collect(toList())
                    , ClassNotFoundException.class);

        } catch (IOException | ClassNotFoundException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }

    private void checkClassRestrictions(Class<?> cls) {
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

    private ServiceDescription toServiceDescription(Class<?> cls, ServicesClassLoader classLoader, String rootGroupName) {
        final List<Class<?>> implInterfaces = Lists.newArrayList();
        walkClass(cls, implInterfaces);

        final Service service = cls.getAnnotation(Service.class);
        final String name = StringUtils.isNotBlank(service.value()) ? service.value() :
                StringUtils.isNotBlank(service.name()) ? service.name() : "";

        final Service.Type type = service.type();
        final Service.InitType initType = service.initType();

        final ServiceDescription.Builder builder = ServiceDescription.builder()
                .setServiceImplName(cls.getName())
                .setServiceName(name)
                .setType(type)
                .setInitType(initType);

        implInterfaces.forEach(i -> {
            final boolean isApiDependency = classLoader.getDependencyHolder(i)
                    .map(dh -> isApiDependency(rootGroupName, dh))
                    .orElse(false);

            builder.addInterface(i.getName(), isApiDependency ? ServiceDescription.AccessType.PUBLIC : ServiceDescription.AccessType.PRIVATE);
        });

        return builder.build();
    }


    private boolean isApiDependency(@Nonnull String rootGroupName, DependencyHolder apiDependency) {
        final String group = apiDependency.getGroup();

        if (StringUtils.startsWith(group, rootGroupName)) {
            final String subGroup = StringUtils.split(group.substring(rootGroupName.length()), '.')[0];

            return ADI_DEPENDENCY_GROUP.equalsIgnoreCase(subGroup) && apiDependency.getType() == API;
        }

        return false;
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
                    .collect(toList());
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
                        .collect(joining(";"));

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
