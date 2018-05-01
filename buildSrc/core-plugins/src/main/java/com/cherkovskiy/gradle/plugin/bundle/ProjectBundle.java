package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.application_context.api.annotations.Service;
import com.cherkovskiy.gradle.plugin.DependencyHolder;
import com.cherkovskiy.gradle.plugin.ServiceDescriptorImpl;
import com.cherkovskiy.gradle.plugin.ServicesClassLoader;
import com.cherkovskiy.gradle.plugin.SubProjectTypes;
import com.cherkovskiy.gradle.plugin.api.Dependency;
import com.cherkovskiy.gradle.plugin.api.ResolvedBundleArtifact;
import com.cherkovskiy.gradle.plugin.api.ResolvedDependency;
import com.cherkovskiy.gradle.plugin.api.ServiceDescriptor;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slieb.throwables.FunctionWithThrowable;
import org.slieb.throwables.SuppressedException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ProjectBundle implements ResolvedBundleArtifact {
    private final File archivePath;
    private final String name;
    private final String version;
    private final boolean embeddedDependencies;
    private final List<DependencyHolder> runtimeConfDependencies;
    private final List<DependencyHolder> apiConfDependencies;
    private final Set<ServiceDescriptor> services;

    public ProjectBundle(File archivePath,
                         String name,
                         String version,
                         boolean embeddedDependencies,
                         List<DependencyHolder> runtimeConfDependencies,
                         List<DependencyHolder> apiConfDependencies) {
        this.archivePath = archivePath;
        this.name = name;
        this.version = version;
        this.embeddedDependencies = embeddedDependencies;
        this.runtimeConfDependencies = runtimeConfDependencies;
        this.apiConfDependencies = apiConfDependencies;
        this.services = servicesLookUp();
    }

    private Set<ServiceDescriptor> servicesLookUp() {
        // We use parent class loader because this plugin uses outside api sources.
        // URLClassLoader has to load Service class from current loader.
        final ServicesClassLoader classLoader = new ServicesClassLoader(archivePath, runtimeConfDependencies, Thread.currentThread().getContextClassLoader());

        try {
            final List<String> allClasses = getAllClassesName(archivePath);
            return SuppressedException.unwrapSuppressedException(() -> allClasses.stream()
                            .map(FunctionWithThrowable.castFunctionWithThrowable(name -> Class.forName(name, false, classLoader)))
                            .filter(cls -> cls.isAnnotationPresent(Service.class))
                            .peek(this::checkClassRestrictions)
                            .map(cls -> toServiceDescription(cls, classLoader))
                            .collect(toSet())
                    , ClassNotFoundException.class);

        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    private void checkClassRestrictions(Class<?> cls) {
        if (cls.isLocalClass()) {
            throw new IllegalStateException("Local class could not be service!");
        }
        if (cls.isInterface()) {
            throw new IllegalStateException("Interfaces could not be service!");
        }
        if (cls.isAnonymousClass()) {
            throw new IllegalStateException("Anonymous class could not be service!");
        }
        if (cls.isMemberClass()) {
            throw new IllegalStateException("Member class could not be service!");
        }
        if (cls.isEnum()) {
            throw new IllegalStateException("Enum class could not be service!");
        }
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new IllegalStateException("Abstract class could not be service!");
        }
    }

    private ServiceDescriptorImpl toServiceDescription(Class<?> cls, ServicesClassLoader classLoader) {
        final List<Class<?>> implInterfaces = Lists.newArrayList();
        walkClass(cls, implInterfaces);

        final Service service = cls.getAnnotation(Service.class);
        final String name = StringUtils.isNotBlank(service.value()) ? service.value() :
                StringUtils.isNotBlank(service.name()) ? service.name() : "";

        final Service.LifecycleType lifecycleType = service.lifecycleType();
        final Service.InitType initType = service.initType();

        final ServiceDescriptorImpl.Builder builder = ServiceDescriptorImpl.builder()
                .setServiceImplName(cls.getName())
                .setServiceName(name)
                .setLifecycleType(lifecycleType)
                .setInitType(initType);

        implInterfaces.forEach(i -> {
            final boolean isApiDependency = classLoader.getDependencyHolder(i)
                    .map(this::isApiExport)
                    .orElse(false);

            builder.addInterface(i.getName(), isApiDependency ? ServiceDescriptorImpl.AccessType.PUBLIC : ServiceDescriptorImpl.AccessType.PRIVATE);
        });

        return builder.build();
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

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean isEmbedded() {
        return embeddedDependencies;
    }

    @Nonnull
    @Override
    public File getFile() {
        return archivePath;
    }

    @Nonnull
    @Override
    public Set<ServiceDescriptor> getServices() {
        return services;
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getApiExport() {
        return runtimeConfDependencies.stream()
                .filter(dep -> dep.isNative() &&
                        SubProjectTypes.API == dep.getSubProjectType() &&
                        existsInApiConfig(dep)
                )
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    private boolean existsInApiConfig(DependencyHolder dependencyHolder) {
        return apiConfDependencies.contains(dependencyHolder);
    }

    public boolean isApiExport(DependencyHolder dependencyHolder) {
        return getApiExport().contains(dependencyHolder);
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getApiImport() {
        return runtimeConfDependencies.stream()
                .filter(dep -> dep.isNative() &&
                        SubProjectTypes.API == dep.getSubProjectType() &&
                        !existsInApiConfig(dep)
                )
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    //TODO: точно ли будет работать, учитывая схлопывания????
    @Nonnull
    @Override
    public Set<ResolvedDependency> getCommon() {
        return runtimeConfDependencies.stream()
                .filter(dep -> {
                    if (!dep.isNative()) {
                        for (; dep != null; dep = dep.getParent().orElse(null)) {
                            if (dep.isNative() && SubProjectTypes.API == dep.getSubProjectType()) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getImplExternal() {
        final Set<ResolvedDependency> common = getCommon();
        return runtimeConfDependencies.stream()
                .filter(dep -> !dep.isNative())
                .filter(dep -> !common.contains(dep))
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getImplInternal() {
        return runtimeConfDependencies.stream()
                .filter(DependencyHolder::isNative)
                .filter(dep -> SubProjectTypes.API != dep.getSubProjectType())
                .collect(Collectors.toCollection(() -> Sets.newTreeSet(Dependency.COMPARATOR)));
    }

    @Override
    public String toString() {
        return "ProjectBundle{" +
                "archivePath=" + archivePath +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", embeddedDependencies=" + embeddedDependencies +
                ", runtimeConfDependencies=" + collectionToDeepString(runtimeConfDependencies) +
                ", apiConfDependencies=" + apiConfDependencies +
                ", services=" + services +
                '}';
    }
}
