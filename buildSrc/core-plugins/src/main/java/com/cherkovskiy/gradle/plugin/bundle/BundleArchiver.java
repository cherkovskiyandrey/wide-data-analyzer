package com.cherkovskiy.gradle.plugin.bundle;

import com.cherkovskiy.gradle.plugin.DependencyHolder;
import com.cherkovskiy.gradle.plugin.ServiceDescription;
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
    private final Set<ServiceDescription> serviceDescriptions = Sets.newHashSet();
    private final Map<DependencyGroup, Set<String>> depGroupToDepsName = Arrays.stream(DependencyGroup.values())
            .collect(Collectors.toMap(Function.identity(), dg -> Sets.newHashSet()));

    private enum DependencyGroup {
        API("WDA-Bundle-Api-Dependencies", "embedded/api/"),
        COMMON("WDA-Bundle-Common-Dependencies", "embedded/libs/common/"),
        IMPL_INTERNAL("WDA-Bundle-Impl-Internal-Dependencies", "embedded/libs/wda/"),
        IMPL_EXTERNAL("WDA-Bundle-Impl-External-Dependencies", "embedded/libs/"),;

        private final String attributeName;
        private final String path;

        DependencyGroup(String attributeName, String path) {
            this.attributeName = attributeName;
            this.path = path;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public String getPath() {
            return path;
        }
    }


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

    public void putApiDependencies(List<DependencyHolder> dependencies) throws IOException {
        putDependencies(dependencies, DependencyGroup.API);
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
                        .filter(this::isArchive)
                        .findFirst()
                        .orElseThrow(() -> new GradleException(format("Dependency artifact %s could not be resolved as archive!", d))))
                .distinct()
                .collect(Collectors.toSet());

        depGroupToDepsName.get(dependencyGroup).addAll(depArchives.stream().map(File::getName).collect(Collectors.toSet()));

        if (isEmbedded) {
            for (File dep : depArchives) {
                try (InputStream inputStream = FileUtils.openInputStream(dep)) {
                    jarFile.createIfNotExists(dependencyGroup.getPath() + dep.getName(), inputStream, null);
                }
            }
        }
    }

    private boolean isArchive(File file) {
        return file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".war");
    }

    public void addServices(List<ServiceDescription> serviceDescriptions) {
        this.serviceDescriptions.addAll(serviceDescriptions);
    }

    @Override
    public void close() throws IOException {
        final Attributes attributes = manifest.getMainAttributes();

        final String services = serviceDescriptions.stream()
                .map(ServiceDescription::toManifestCompatibleString)
                .collect(joining(";"));
        attributes.put(new Attributes.Name(EXPORTED_SERVICES), services);

        for (Map.Entry<DependencyGroup, Set<String>> entry : depGroupToDepsName.entrySet()) {
            final String allApiDependencies = entry.getValue().stream().collect(Collectors.joining(","));
            attributes.put(new Attributes.Name(entry.getKey().getAttributeName()), allApiDependencies);
        }
        jarFile.setManifest(manifest);

        jarFile.close();
    }
}