package com.cherkovskiy.gradle.plugin;

import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.jvm.tasks.Jar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class BundlePackager implements Plugin<Project> {

    private static final String MANIFEST_ENTRY_NAME = "META-INF/MANIFEST.MF";

    @Override
    public void apply(Project project) {
        project.getTasks().withType(Jar.class).forEach(jar -> {
            jar.doLast(task -> {
                final Jar jarTask = (Jar) task;

                //TODO: запрещать ипортировать другие бандлы, плагины и приложения
                // заодно разобраться с зависимостями

                final List<File> dependencies = getDependencies(project);

                final Map<String, String> extraAttributes = extractAllServicesFrom(jarTask.getArchivePath(), dependencies);
                addToManifest(jarTask.getArchivePath(), extraAttributes);
            });
        });
    }

    private void addToManifest(File archivePath, Map<String, String> extraAttributes) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024)) {
            try (final JarFile jarFile = new JarFile(archivePath)) {

                final Manifest manifest = new Manifest(jarFile.getManifest());
                final Attributes attributes = manifest.getMainAttributes();
                extraAttributes.forEach((key, value) -> attributes.put(new Attributes.Name(key), value));

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
            }
            Files.write(archivePath.toPath(), byteArrayOutputStream.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new GradleException("Could not open artifact: " + archivePath.getAbsolutePath(), e);
        }
    }


    private Map<String, String> extractAllServicesFrom(File archivePath, List<File> dependencies) {
        //TODO: создаём класс лоадер и грузим все классы из archivePath и только для классов из archivePath забираем сервисы
        throw new UnsupportedOperationException("");
    }

    private List<File> getDependencies(Project project) {
        //todo
        //https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_separation
        System.out.println(project.getConfigurations().getByName("compile").getResolvedConfiguration().getFirstLevelModuleDependencies().size());
        System.out.println(project.getConfigurations().getByName("compileOnly").getIncoming().getResolutionResult().getAllDependencies().size());
        throw new UnsupportedOperationException("");
    }
}
