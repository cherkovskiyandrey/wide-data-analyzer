package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.application_context.api.annotations.Service;
import com.cherkovskiy.gradle.plugin.*;
import com.google.common.collect.ImmutableSet;
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
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static com.cherkovskiy.gradle.plugin.DependencyScanner.TransitiveMode.TRANSITIVE_OFF;
import static com.cherkovskiy.gradle.plugin.DependencyType.STUFF_ALL_API;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class BundlePackager implements Plugin<Project> {
    private static final ImmutableSet<SubProjectTypes> ALLOWED_TO_DEPENDS_ON_LIST = new ImmutableSet.Builder<SubProjectTypes>()
            .add(SubProjectTypes.API)
            .add(SubProjectTypes.COMMON)
            .build();

    @Override
    public void apply(@Nonnull Project project) {
        final BundlePackagerConfiguration configuration = project.getExtensions().create(BundlePackagerConfiguration.NAME, BundlePackagerConfiguration.class);

        createAndInitStuffApiConfig(project);

        project.getTasks().getAt("build").doLast(task -> {
            final Jar jarTask = project.getTasks().withType(Jar.class).iterator().next();

            final DependencyScanner dependencyScanner = new DependencyScanner(project);
            final List<DependencyHolder> dependencies = dependencyScanner.getRuntimeDependencies();

            final List<DependencyHolder> allApiDependencies = dependencyScanner.getResolvedDependenciesByType(STUFF_ALL_API);
            checkDependenciesAgainst(project, dependencies, allApiDependencies);

            final DependencyByCategories dependencyCollection = new DependencyByCategories(dependencies);

            //Bundle can export only services from api dependencies without services from transitive these api dependencies
            final List<DependencyHolder> resolvedByApiTypeDependencies = dependencyScanner.resolveAgainst(dependencies, DependencyType.API, TRANSITIVE_OFF);
            Utils.checkImportProjectsRestrictions(project, resolvedByApiTypeDependencies, ALLOWED_TO_DEPENDS_ON_LIST);
            final List<ServiceDescription> serviceDescriptions = extractAllServicesFrom(jarTask.getArchivePath(), resolvedByApiTypeDependencies);

            try (BundleArchiver bundleArchive = new BundleArchiver(jarTask.getArchivePath(), configuration.embeddedDependencies)) {
                bundleArchive.setBundleNameVersion(jarTask.getBaseName(), jarTask.getVersion());

                bundleArchive.putApiDependencies(dependencyCollection.getApi());
                bundleArchive.putCommonDependencies(dependencyCollection.getCommon());
                bundleArchive.putExternalImplDependencies(dependencyCollection.getExternalImpl());
                bundleArchive.putInternalImplDependencies(dependencyCollection.getInternalImpl());

                bundleArchive.addServices(serviceDescriptions);
            } catch (Exception e) {
                throw new GradleException("Could not change artifact: " + jarTask.getArchivePath().getAbsolutePath(), e);
            }
        });
    }

    private void createAndInitStuffApiConfig(Project project) {
        project.getConfigurations().create(STUFF_ALL_API.getGradleString(), conf -> conf.getDependencies().addAll(
                project.getRootProject().getSubprojects().stream()
                        .filter(sp -> sp.getPath().contains(":api:"))
                        .map(project.getDependencies()::create)
                        .collect(Collectors.toSet())));
    }

    private List<ServiceDescription> extractAllServicesFrom(File rootArtifactFile, List<DependencyHolder> dependencies) {
        // We use parent class loader because this plugin uses outside api sources.
        // URLClassLoader has to load Service class from current loader.
        final ServicesClassLoader classLoader = new ServicesClassLoader(rootArtifactFile, dependencies, Thread.currentThread().getContextClassLoader());

        try {
            final List<String> allClasses = getAllClassesName(rootArtifactFile);

            return SuppressedException.unwrapSuppressedException(() -> allClasses.stream()
                            .map(FunctionWithThrowable.castFunctionWithThrowable(name -> Class.forName(name, false, classLoader)))
                            .filter(cls -> cls.isAnnotationPresent(Service.class))
                            .peek(this::checkClassRestrictions)
                            .map(cls -> toServiceDescription(cls, classLoader))
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

    private ServiceDescription toServiceDescription(Class<?> cls, ServicesClassLoader classLoader) {
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
                    .map(this::isApiDependency)
                    .orElse(false);

            builder.addInterface(i.getName(), isApiDependency ? ServiceDescription.AccessType.PUBLIC : ServiceDescription.AccessType.PRIVATE);
        });

        return builder.build();
    }


    private boolean isApiDependency(@Nonnull DependencyHolder apiDependency) {
        return apiDependency.isNative() &&
                DependencyType.API.equals(apiDependency.getType()) &&
                SubProjectTypes.API.equals(apiDependency.getSubProjectType());  //TRANSITIVE_OFF - get only first level api dependencies
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

    private void checkDependenciesAgainst(Project project, List<DependencyHolder> dependencies, List<DependencyHolder> allApiDependencies) {
        final List<DependencyHolder> prjExternalDependencies = dependencies.stream()
                .filter(d -> !d.isNative())
                .collect(Collectors.toList());

        final List<DependencyHolder> allApiExternalDependencies = allApiDependencies.stream()
                .filter(d -> !d.isNative())
                .collect(Collectors.toList());

        for (final DependencyHolder prjDep : prjExternalDependencies) {
            allApiExternalDependencies.stream()
                    .filter(mDep -> Objects.equals(mDep.getGroup(), prjDep.getGroup()) &&
                            Objects.equals(mDep.getName(), prjDep.getName()))
                    .findFirst()
                    .ifPresent(dependencyHolder -> {
                        if (!Objects.equals(dependencyHolder.getVersion(), prjDep.getVersion())) {
                            throw new GradleException(format("It is not allowed to use 3rd party dependencies from scope of all api subprojects and different version. " +
                                            "Project \"%s\" has other version common dependency: \"%s\". Must be used %s!",
                                    project.getPath(), prjDep, dependencyHolder.getVersion()));
                        }
                    });

        }
    }
}
