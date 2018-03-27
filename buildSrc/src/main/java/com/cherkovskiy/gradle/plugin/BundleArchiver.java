package com.cherkovskiy.gradle.plugin;

import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

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

    private static final String MANIFEST_ENTRY_NAME = "META-INF/MANIFEST.MF";
    public static final String API_PATH = "embedded/api/";
    public static final String IMPL_PATH = "embedded/libs/";

    private final File archivePath;
    private final JarFile jarFile;
    private final Manifest manifest;
    private final Set<ServiceDescription> serviceDescriptions = Sets.newHashSet();
    private final Set<String> apiDependencies = Sets.newHashSet();
    private final Set<String> implDependencies = Sets.newHashSet();
    private final Set<File> apiEmbeddedDependencies = Sets.newHashSet();
    private final Set<File> implEmbeddedDependencies = Sets.newHashSet();

    public BundleArchiver(File archivePath) throws IOException {
        this.archivePath = archivePath;
        this.jarFile = new JarFile(archivePath);
        this.manifest = new Manifest(jarFile.getManifest());
    }

    public void setBundleNameVersion(String bundleName, String bundleVersion) {
        final Attributes attributes = manifest.getMainAttributes();

        attributes.put(new Attributes.Name(BUNDLE_NAME), bundleName);
        attributes.put(new Attributes.Name(BUNDLE_VERSION), bundleVersion);
    }

    public void putApiDependencies(List<DependencyHolder> dependencies, boolean isEmbedded) {
        putDependencies(dependencies, apiDependencies, apiEmbeddedDependencies, isEmbedded);
    }

    public void putImplDependencies(List<DependencyHolder> dependencies, boolean isEmbedded) {
        putDependencies(dependencies, implDependencies, implEmbeddedDependencies, isEmbedded);
    }

    private void putDependencies(List<DependencyHolder> dependencies, Set<String> asArchiveName, Set<File> asFile, boolean isEmbedded) {
        final Set<File> depArchives = dependencies.stream()
                .map(d -> d.getArtifacts().stream()
                        .filter(this::isArchive)
                        .findFirst()
                        .orElseThrow(() -> new GradleException(format("Dependency artifact %s could not be resolved as archive!", d))))
                .distinct()
                .collect(Collectors.toSet());

        asArchiveName.addAll(depArchives.stream().map(File::getName).collect(Collectors.toSet()));

        if (isEmbedded) {
            asFile.addAll(depArchives);
        }
    }

    //TODO: to enum
    private boolean isArchive(File file) {
        return file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".war");
    }

    public void addServices(List<ServiceDescription> serviceDescriptions) {
        this.serviceDescriptions.addAll(serviceDescriptions);
    }

    @Override
    public void close() throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024)) {
            writeServicesToManifest(manifest);
            writeApiDepsToManifest(manifest);
            writeImplDepsToManifest(manifest);
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
                putEmbeddedApiDeps(jarOutputStream);
                putEmbeddedImplDeps(jarOutputStream);
            }
            jarFile.close();
            Files.write(archivePath.toPath(), byteArrayOutputStream.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private void writeServicesToManifest(Manifest manifest) {
        String services = serviceDescriptions.stream()
                .map(ServiceDescription::toManifestCompatibleString)
                .collect(joining(";"));

        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(new Attributes.Name(EXPORTED_SERVICES), services);
    }

    private void writeApiDepsToManifest(Manifest manifest) {
        String allApiDeps = apiDependencies.stream().collect(Collectors.joining(","));

        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(new Attributes.Name(API_DEPENDENCIES), allApiDeps);

        if (!apiEmbeddedDependencies.isEmpty()) {
            attributes.put(new Attributes.Name(IS_API_EMBEDDED), Boolean.TRUE.toString());
        }
    }

    private void writeImplDepsToManifest(Manifest manifest) {
        String allImplDeps = implDependencies.stream().collect(Collectors.joining(","));

        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(new Attributes.Name(IMPL_DEPENDENCIES), allImplDeps);

        if (!implEmbeddedDependencies.isEmpty()) {
            attributes.put(new Attributes.Name(IS_IMPL_EMBEDDED), Boolean.TRUE.toString());
        }
    }

    private void putEmbeddedApiDeps(JarOutputStream jarOutputStream) throws IOException {
        if (!apiEmbeddedDependencies.isEmpty()) {

            for (File dep : apiEmbeddedDependencies) {
                //todo: to separate api and transitive 3-rd party api libs
                final ZipEntry zipEntry = new ZipEntry(API_PATH + dep.getName());
                jarOutputStream.putNextEntry(zipEntry);
                FileUtils.copyFile(dep, jarOutputStream);
                jarOutputStream.closeEntry();
            }
        }
    }

    private void putEmbeddedImplDeps(JarOutputStream jarOutputStream) throws IOException {
        if (!implEmbeddedDependencies.isEmpty()) {

            for (File dep : implEmbeddedDependencies) {
                final ZipEntry zipEntry = new ZipEntry(IMPL_PATH + dep.getName());
                jarOutputStream.putNextEntry(zipEntry);
                FileUtils.copyFile(dep, jarOutputStream);
                jarOutputStream.closeEntry();
            }
        }
    }

}