package com.cherkovskiy.gradle.plugin;

import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

class BundleArchive implements Closeable {

    private static final String BUNDLE_NAME = "BUNDLE_NAME";
    private static final String BUNDLE_VERSION = "BUNDLE_VERSION";
    private static final String EXPORTED_SERVICES = "BUNDLE_EXPORTED_SERVICES";
    private static final String API_DEPENDENCIES = "BUNDLE_API_DEPENDENCIES";
    private static final String IMPL_DEPENDENCIES = "BUNDLE_IMPL_DEPENDENCIES";
    private static final String MANIFEST_ENTRY_NAME = "META-INF/MANIFEST.MF";

    private final File archivePath;
    private final JarFile jarFile;
    private final Manifest manifest;

    BundleArchive(File archivePath) throws IOException {
        this.archivePath = archivePath;
        this.jarFile = new JarFile(archivePath);
        this.manifest = new Manifest(jarFile.getManifest());
    }

    void setBundleNameVersion(String bundleName, String bundleVersion) {
        final Attributes attributes = manifest.getMainAttributes();

        attributes.put(new Attributes.Name(BUNDLE_NAME), bundleName);
        attributes.put(new Attributes.Name(BUNDLE_VERSION), bundleVersion);
    }

    void addApiDependencies(List<DependencyHolder> dependencies) {
        String apiDepsAsStr = dependencies.stream()
                .map(d -> d.getArtifacts().stream()
                        .filter(this::isArchive)
                        .findFirst()
                        .orElseThrow(() -> new GradleException(format("API dependency artifact %s could not be resolved as archive!", d)))
                        .getName())
                .distinct()
                .collect(Collectors.joining(","));


        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(new Attributes.Name(API_DEPENDENCIES), apiDepsAsStr);
    }

    void addImplDependencies(List<DependencyHolder> dependencies) {
        String apiDepsAsStr = dependencies.stream()
                .map(d -> d.getArtifacts().stream()
                        .filter(this::isArchive)
                        .findFirst()
                        .orElseThrow(() -> new GradleException(format("IMPL dependency artifact %s could not be resolved as archive!", d)))
                        .getName())
                .distinct()
                .collect(Collectors.joining(","));

        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(new Attributes.Name(IMPL_DEPENDENCIES), apiDepsAsStr);
    }


    //TODO: to enum
    private boolean isArchive(File file) {
        return file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".war");
    }


    public void addServices(List<ServiceDescription> serviceDescriptions) {
        final String services = serviceDescriptions.stream()
                .map(ServiceDescription::toManifestCompatibleString)
                .collect(joining(";"));

        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(new Attributes.Name(EXPORTED_SERVICES), services);
    }

    @Override
    public void close() throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024)) {
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
            jarFile.close();
            Files.write(archivePath.toPath(), byteArrayOutputStream.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
}