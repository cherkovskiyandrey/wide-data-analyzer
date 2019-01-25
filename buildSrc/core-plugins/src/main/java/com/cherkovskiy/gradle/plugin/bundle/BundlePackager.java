package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.application_context.ManifestArtifact;
import com.cherkovskiy.application_context.ManifestServiceDescriptor;
import com.cherkovskiy.application_context.api.ResolvedDependency;
import com.cherkovskiy.application_context.api.ServiceDescriptor;
import com.cherkovskiy.vfs.DirectoryFactory;
import com.cherkovskiy.vfs.zip.JarDirectoryAdapter;
import com.cherkovskiy.application_context.BundleDependencyGroup;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static com.cherkovskiy.application_context.BundleFile.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

class BundlePackager implements Closeable {
    private final boolean isEmbedded;
    private final JarDirectoryAdapter jarFile;
    private final Manifest manifest;
    private final Set<ServiceDescriptor> serviceDescriptions = Sets.newHashSet();
    private final Map<BundleDependencyGroup, Set<ResolvedDependency>> depGroupToManifestStr = Arrays.stream(BundleDependencyGroup.values())
            .collect(Collectors.toMap(Function.identity(), dg -> Sets.newHashSet()));


    public BundlePackager(File archivePath, boolean isEmbedded) {
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

    public void putApiExportDependencies(Set<ResolvedDependency> dependencies) throws IOException {
        putDependencies(dependencies, BundleDependencyGroup.API_EXPORT);
    }


    public void putApiImportDependencies(Set<ResolvedDependency> dependencies) throws IOException {
        putDependencies(dependencies, BundleDependencyGroup.API_IMPORT);
    }

    public void putCommonDependencies(Set<ResolvedDependency> dependencies) throws IOException {
        putDependencies(dependencies, BundleDependencyGroup.COMMON);
    }

    public void putExternalImplDependencies(Set<ResolvedDependency> dependencies) throws IOException {
        putDependencies(dependencies, BundleDependencyGroup.IMPL_EXTERNAL);
    }


    public void putInternalImplDependencies(Set<ResolvedDependency> dependencies) throws IOException {
        putDependencies(dependencies, BundleDependencyGroup.IMPL_INTERNAL);
    }

    private void putDependencies(Set<ResolvedDependency> dependencies, BundleDependencyGroup dependencyGroup) throws IOException {
        depGroupToManifestStr.get(dependencyGroup).addAll(dependencies);

        if (isEmbedded) {
            for (ResolvedDependency resolvedArtifact : dependencies) {
                try (InputStream inputStream = FileUtils.openInputStream(resolvedArtifact.getFile())) {
                    jarFile.createIfNotExists(dependencyGroup.getPath() + resolvedArtifact.getFile().getName(), inputStream, null);
                }
            }
        }
    }

    public void addServices(Set<ServiceDescriptor> serviceDescriptions) {
        this.serviceDescriptions.addAll(serviceDescriptions);
    }

    @Override
    public void close() throws IOException {
        final Attributes attributes = manifest.getMainAttributes();

        final String services = serviceDescriptions.stream()
                .map(ManifestServiceDescriptor::toManifestString)
                .collect(joining(ManifestServiceDescriptor.GROUP_SEPARATOR));
        attributes.put(new Attributes.Name(EXPORTED_SERVICES), services);

        for (Map.Entry<BundleDependencyGroup, Set<ResolvedDependency>> entry : depGroupToManifestStr.entrySet()) {
            final String manifestStr = entry.getValue().stream()
                    .map(ManifestArtifact::toManifestString)
                    .collect(Collectors.joining(ManifestArtifact.GROUP_SEPARATOR));

            attributes.put(new Attributes.Name(entry.getKey().getAttributeName()), manifestStr);
        }

        jarFile.setManifest(manifest);

        jarFile.close();
    }
}