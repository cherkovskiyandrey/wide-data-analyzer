package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.gradle.plugin.DependencyGroup;
import com.cherkovskiy.gradle.plugin.ManifestArtifact;
import com.cherkovskiy.gradle.plugin.DependencyHolder;
import com.cherkovskiy.gradle.plugin.ServiceDescriptor;
import com.cherkovskiy.vfs.DirectoryFactory;
import com.cherkovskiy.vfs.zip.JarDirectoryAdapter;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

class BundleArchiver implements Closeable {
    private static final String BUNDLE_NAME = "WDA-Bundle-Name";
    private static final String BUNDLE_VERSION = "WDA-Bundle-Version";
    private static final String EXPORTED_SERVICES = "WDA-Bundle-Exported-Services";
    private static final String IS_DEPENDENCIES_EMBEDDED = "WDA-Bundle-Dependencies-Embedded";

    private final boolean isEmbedded;
    private final JarDirectoryAdapter jarFile;
    private final Manifest manifest;
    private final Set<ServiceDescriptor> serviceDescriptions = Sets.newHashSet();
    private final Map<DependencyGroup, Set<ManifestArtifact>> depGroupToManifestStr = Arrays.stream(DependencyGroup.values())
            .collect(Collectors.toMap(Function.identity(), dg -> Sets.newHashSet()));


    public BundleArchiver(File archivePath, boolean isEmbedded) {
        this.jarFile = new JarDirectoryAdapter(DirectoryFactory.defaultInstance().tryDetectAndOpen(archivePath.getAbsolutePath(), false));
        this.manifest = jarFile.getManifest();

        if (this.manifest == null) {
            throw new GradleException(format("Jar archive %s does not contain Manifest.", archivePath));
        }
        this.isEmbedded = isEmbedded;

        if (isEmbedded) {
            final Attributes attributes = manifest.getMainAttributes();
            attributes.put(new Attributes.Name(IS_DEPENDENCIES_EMBEDDED), Boolean.TRUE.toString());
        }
    }

    public void setBundleNameVersion(String bundleName, String bundleVersion) {
        final Attributes attributes = manifest.getMainAttributes();

        attributes.put(new Attributes.Name(BUNDLE_NAME), bundleName);
        attributes.put(new Attributes.Name(BUNDLE_VERSION), bundleVersion);
    }

    public void putApiExportDependencies(List<DependencyHolder> dependencies) throws IOException {
        putDependencies(dependencies, DependencyGroup.API_EXPORT);
    }


    public void putApiImportDependencies(List<DependencyHolder> dependencies) throws IOException {
        putDependencies(dependencies, DependencyGroup.API_IMPORT);
    }

    public void putCommonDependencies(List<DependencyHolder> dependencies) throws IOException {
        putDependencies(dependencies, DependencyGroup.COMMON);
    }

    public void putExternalImplDependencies(List<DependencyHolder> dependencies) throws IOException {
        putDependencies(dependencies, DependencyGroup.IMPL_EXTERNAL);
    }


    public void putInternalImplDependencies(List<DependencyHolder> dependencies) throws IOException {
        putDependencies(dependencies, DependencyGroup.IMPL_INTERNAL);
    }

    private void putDependencies(List<DependencyHolder> dependencies, DependencyGroup dependencyGroup) throws IOException {
        final Set<File> depArchives = dependencies.stream()
                .map(d -> d.getArtifacts().stream()
                        .filter(DependencyHolder::isArchive)
                        .findFirst()
                        .orElseThrow(() -> new GradleException(format("Dependency artifact %s could not be resolved as archive!", d))))
                .distinct()
                .collect(Collectors.toSet());

        depGroupToManifestStr.get(dependencyGroup).addAll(dependencies.stream()
                .map(DependencyHolder::descriptor)
                .collect(Collectors.toSet())
        );

        if (isEmbedded) {
            for (File dep : depArchives) {
                try (InputStream inputStream = FileUtils.openInputStream(dep)) {
                    jarFile.createIfNotExists(dependencyGroup.getPath() + dep.getName(), inputStream, null);
                }
            }
        }
    }

    public void addServices(List<ServiceDescriptor> serviceDescriptions) {
        this.serviceDescriptions.addAll(serviceDescriptions);
    }

    @Override
    public void close() throws IOException {
        final Attributes attributes = manifest.getMainAttributes();

        final String services = serviceDescriptions.stream()
                .map(ServiceDescriptor::toManifestString)
                .collect(joining(ServiceDescriptor.GROUP_SEPARATOR));
        attributes.put(new Attributes.Name(EXPORTED_SERVICES), services);

        for (Map.Entry<DependencyGroup, Set<ManifestArtifact>> entry : depGroupToManifestStr.entrySet()) {
            final String manifestStr = entry.getValue().stream()
                    .map(ManifestArtifact::toManifestString)
                    .collect(Collectors.joining(ManifestArtifact.GROUP_SEPARATOR));
            attributes.put(new Attributes.Name(entry.getKey().getAttributeName()), manifestStr);
        }

        jarFile.setManifest(manifest);

        jarFile.close();
    }
}