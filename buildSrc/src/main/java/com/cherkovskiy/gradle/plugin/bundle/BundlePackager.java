package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.application_context.api.annotations.Service;
import com.cherkovskiy.gradle.plugin.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.jvm.tasks.Jar;
import org.slieb.throwables.FunctionWithThrowable;
import org.slieb.throwables.SuppressedException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static com.cherkovskiy.gradle.plugin.DependencyScanner.TransitiveMode.TRANSITIVE_OFF;
import static com.cherkovskiy.gradle.plugin.DependencyType.API;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class BundlePackager implements Plugin<Project> {
    private static final String ADI_DEPENDENCY_GROUP = "api";
    private static final ImmutableList<String> ALLOWED_TO_DEPENDS_ON_LIST = new ImmutableList.Builder<String>()
            .add("api")
            .add("common")
            .build();

    @Override
    public void apply(@Nonnull Project project) {
        final BundlePackagerConfiguration configuration = project.getExtensions().create(BundlePackagerConfiguration.NAME, BundlePackagerConfiguration.class);

        project.getTasks().withType(Jar.class).forEach(jar -> {
            jar.doLast(task -> {
                final Jar jarTask = (Jar) task;
                final String rootGroupName = Utils.lookUpRootGroupName(project);

                final DependencyScanner dependencyScanner = new DependencyScanner(project);
                final List<DependencyHolder> dependencies = dependencyScanner.getDependencies();
                final List<DependencyHolder> prjApiDependencies = filterApiDependencies(dependencies, rootGroupName);

                final List<DependencyHolder> prjImplDependencies = Lists.newArrayList(dependencies);
                prjImplDependencies.removeAll(prjApiDependencies);


                //Bundle can export only services from api dependencies without transitive these api dependencies
                final List<DependencyHolder> resolvedByApiTypeDependencies = dependencyScanner.resolveAgainst(dependencies, DependencyType.API, TRANSITIVE_OFF);
                checkImportRestrictions(rootGroupName, resolvedByApiTypeDependencies);
                final List<ServiceDescription> serviceDescriptions = extractAllServicesFrom(rootGroupName, jarTask.getArchivePath(), resolvedByApiTypeDependencies);


                try (BundleArchiver bundleArchive = new BundleArchiver(jarTask.getArchivePath())) {
                    bundleArchive.setBundleNameVersion(jarTask.getBaseName(), jarTask.getVersion());
                    bundleArchive.putApiDependencies(prjApiDependencies, configuration.embeddedDependencies);
                    bundleArchive.putImplDependencies(prjImplDependencies, configuration.embeddedDependencies);
                    bundleArchive.addServices(serviceDescriptions);
                } catch (IOException e) {
                    throw new GradleException("Could not change artifact: " + jarTask.getArchivePath().getAbsolutePath(), e);
                }
            });
        });
    }

    private void checkImportRestrictions(@Nonnull String rootGroupName, @Nonnull List<DependencyHolder> dependencies) {
        final List<DependencyHolder> forbiddenDependencies = dependencies.stream()
                .filter(dep -> {
                    final String group = dep.getGroup();

                    if (StringUtils.startsWith(group, rootGroupName)) {
                        final String subGroup = StringUtils.split(group.substring(rootGroupName.length()), '.')[0];

                        if (ALLOWED_TO_DEPENDS_ON_LIST.stream().noneMatch(frb -> frb.equalsIgnoreCase(subGroup))) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(toList());

        if (!forbiddenDependencies.isEmpty()) {
            throw new GradleException(String.format("Bundle could depends only on: %s. There is forbidden dependencies: %s",
                    ALLOWED_TO_DEPENDS_ON_LIST.stream().collect(joining(", ")),
                    forbiddenDependencies.stream().map(DependencyHolder::toString).collect(joining(", "))
            ));
        }
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

        return Utils.subProjectAgainst(group, rootGroupName)
                .map(pg ->
                        ADI_DEPENDENCY_GROUP.equalsIgnoreCase(pg) &&
                                apiDependency.getType() == API) //TRANSITIVE_OFF - get only first level api dependencies
                .orElse(false);
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


    private List<DependencyHolder> filterApiDependencies(List<DependencyHolder> dependencies, @Nonnull String rootGroupName) {
        //1. any parent is api
        //2. itself api
        return dependencies.stream()
                .filter(dep -> {
                    for (Optional<DependencyHolder> d = Optional.of(dep); d.isPresent(); d = d.flatMap(DependencyHolder::getParent)) {
                        if (Utils.subProjectAgainst(d.get().getGroup(), rootGroupName)
                                .map(ADI_DEPENDENCY_GROUP::equalsIgnoreCase)
                                .orElse(false)) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(toList());
    }
}
