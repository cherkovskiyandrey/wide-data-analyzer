package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.gradle.plugin.*;
import com.cherkovskiy.vfs.DirectoryFactory;
import com.cherkovskiy.vfs.zip.JarDirectoryAdapter;
import com.google.common.collect.Sets;
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

import static com.cherkovskiy.gradle.plugin.BundleFile.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

class BundleArchiver implements Closeable {
    private final boolean isEmbedded;
    private final JarDirectoryAdapter jarFile;
    private final Manifest manifest;
    private final Set<ServiceDescriptor> serviceDescriptions = Sets.newHashSet();
    private final Map<DependencyGroup, Set<Artifact>> depGroupToManifestStr = Arrays.stream(DependencyGroup.values())
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

    public void putApiExportDependencies(Set<ResolvedArtifact> dependencies) throws IOException {
        putDependencies(dependencies, DependencyGroup.API_EXPORT);
    }


    public void putApiImportDependencies(Set<ResolvedArtifact> dependencies) throws IOException {
        putDependencies(dependencies, DependencyGroup.API_IMPORT);
    }

    public void putCommonDependencies(Set<ResolvedArtifact> dependencies) throws IOException {
        putDependencies(dependencies, DependencyGroup.COMMON);
    }

    public void putExternalImplDependencies(Set<ResolvedArtifact> dependencies) throws IOException {
        putDependencies(dependencies, DependencyGroup.IMPL_EXTERNAL);
    }


    public void putInternalImplDependencies(Set<ResolvedArtifact> dependencies) throws IOException {
        putDependencies(dependencies, DependencyGroup.IMPL_INTERNAL);
    }

    private void putDependencies(Set<ResolvedArtifact> dependencies, DependencyGroup dependencyGroup) throws IOException {
        depGroupToManifestStr.get(dependencyGroup).addAll(dependencies);

        if (isEmbedded) {
            for (ResolvedArtifact resolvedArtifact : dependencies) {
                try (InputStream inputStream = resolvedArtifact.openInputStream()) {
                    jarFile.createIfNotExists(dependencyGroup.getPath() + resolvedArtifact.getArtifactFileName(), inputStream, null);
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

        for (Map.Entry<DependencyGroup, Set<Artifact>> entry : depGroupToManifestStr.entrySet()) {
            final String manifestStr = entry.getValue().stream()
                    .map(ManifestArtifact::toManifestString)
                    .collect(Collectors.joining(ManifestArtifact.GROUP_SEPARATOR));

            attributes.put(new Attributes.Name(entry.getKey().getAttributeName()), manifestStr);
        }

        jarFile.setManifest(manifest);

        jarFile.close();
    }
}