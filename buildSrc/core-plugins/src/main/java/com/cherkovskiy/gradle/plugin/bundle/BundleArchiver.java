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
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

class BundleArchiver implements Closeable {
    private static final String BUNDLE_NAME = "WDA-Bundle-Name";
    private static final String BUNDLE_VERSION = "WDA-Bundle-Version";
    private static final String EXPORTED_SERVICES = "WDA-Bundle-Exported-Services";
    private static final String API_DEPENDENCIES = "WDA-Bundle-Api-Dependencies";
    private static final String IMPL_DEPENDENCIES = "WDA-Bundle-Impl-Dependencies";
    private static final String IS_API_EMBEDDED = "WDA-Bundle-Api-Embedded";
    private static final String IS_IMPL_EMBEDDED = "WDA-Bundle-Impl-Embedded";

    public static final String API_PATH = "embedded/api/";
    public static final String IMPL_PATH = "embedded/libs/";

    private final JarDirectoryAdapter jarFile;
    private final Manifest manifest;
    private final Set<ServiceDescription> serviceDescriptions = Sets.newHashSet();
    private final Set<String> apiDependencies = Sets.newHashSet();
    private final Set<String> implDependencies = Sets.newHashSet();

    public BundleArchiver(File archivePath) {
        this.jarFile = new JarDirectoryAdapter(DirectoryFactory.defaultInstance().tryDetectAndOpen(archivePath.getAbsolutePath(), false));
        this.manifest = jarFile.getManifest();

        if (this.manifest == null) {
            throw new GradleException(format("Jar archive %s does not contain Manifest.", archivePath));
        }
    }

    public void setBundleNameVersion(String bundleName, String bundleVersion) {
        final Attributes attributes = manifest.getMainAttributes();

        attributes.put(new Attributes.Name(BUNDLE_NAME), bundleName);
        attributes.put(new Attributes.Name(BUNDLE_VERSION), bundleVersion);
    }

    public void putApiDependencies(List<DependencyHolder> dependencies, boolean isEmbedded) throws IOException {
        putDependencies(dependencies, apiDependencies, IS_API_EMBEDDED, API_PATH, isEmbedded);
    }

    public void putImplDependencies(List<DependencyHolder> dependencies, boolean isEmbedded) throws IOException {
        putDependencies(dependencies, implDependencies, IS_IMPL_EMBEDDED, IMPL_PATH, isEmbedded);
    }

    private void putDependencies(List<DependencyHolder> dependencies,
                                 Set<String> asArchiveName,
                                 String embeddedMnfFlag,
                                 String embeddedBaseDir,
                                 boolean isEmbedded) throws IOException {

        final Set<File> depArchives = dependencies.stream()
                .map(d -> d.getArtifacts().stream()
                        .filter(this::isArchive)
                        .findFirst()
                        .orElseThrow(() -> new GradleException(format("Dependency artifact %s could not be resolved as archive!", d))))
                .distinct()
                .collect(Collectors.toSet());

        asArchiveName.addAll(depArchives.stream().map(File::getName).collect(Collectors.toSet()));

        if (isEmbedded) {
            for (File dep : depArchives) {
                //todo: to separate api and transitive 3-rd party api libs
                try (InputStream inputStream = FileUtils.openInputStream(dep)) {
                    jarFile.createIfNotExists(embeddedBaseDir + dep.getName(), inputStream, null);
                }
            }
            final Attributes attributes = manifest.getMainAttributes();
            attributes.put(new Attributes.Name(embeddedMnfFlag), Boolean.TRUE.toString());
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

        final String allApiDependencies = apiDependencies.stream().collect(Collectors.joining(","));
        attributes.put(new Attributes.Name(API_DEPENDENCIES), allApiDependencies);

        final String allImplDependencies = implDependencies.stream().collect(Collectors.joining(","));
        attributes.put(new Attributes.Name(IMPL_DEPENDENCIES), allImplDependencies);

        jarFile.setManifest(manifest);

        jarFile.close();
    }
}