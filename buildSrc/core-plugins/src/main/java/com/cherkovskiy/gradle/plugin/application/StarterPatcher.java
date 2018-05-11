package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.gradle.plugin.api.ResolvedDependency;
import com.cherkovskiy.gradle.plugin.api.ResolvedProjectArtifact;
import com.cherkovskiy.vfs.DirectoryFactory;
import com.cherkovskiy.vfs.zip.JarDirectoryAdapter;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static java.util.jar.Attributes.Name.CLASS_PATH;

public class StarterPatcher {

    public static ResolvedProjectArtifact patch(ResolvedProjectArtifact applicationStarter, File temporaryDir) throws IOException {
        final File patchedJar = Paths.get(temporaryDir.getAbsolutePath(), applicationStarter.getFileName()).toFile();

        FileUtils.deleteQuietly(patchedJar);
        Files.copy(applicationStarter.getFile(), patchedJar);

        try (JarDirectoryAdapter jarFile = new JarDirectoryAdapter(DirectoryFactory.defaultInstance()
                .tryDetectAndOpen(applicationStarter.getFile().getAbsolutePath(), false))) {
            final Manifest manifest = jarFile.getManifest();

            final String classPath = new ClassPathBuilder()
                    .append(applicationStarter.getApi(), ApplicationDirectories.API)
                    .append(applicationStarter.getCommon(), ApplicationDirectories.LIB_COMMON)
                    .append(applicationStarter.getInternal(), ApplicationDirectories.LIB_INTERNAL)
                    .append(applicationStarter.get3rdParty(), ApplicationDirectories.LIB)
                    .build();

            manifest.getMainAttributes().put(CLASS_PATH, classPath);
            jarFile.setManifest(manifest);
        }

        return new ResolvedProjectArtifact() {
            @Nonnull
            @Override
            public String getGroup() {
                return applicationStarter.getGroup();
            }

            @Nonnull
            @Override
            public String getName() {
                return applicationStarter.getName();
            }

            @Nonnull
            @Override
            public String getVersion() {
                return applicationStarter.getVersion();
            }

            @Nullable
            @Override
            public String getFileName() {
                return patchedJar.getName();
            }

            @Nonnull
            @Override
            public File getFile() {
                return patchedJar;
            }

            @Nonnull
            @Override
            public Set<ResolvedDependency> getApi() {
                return applicationStarter.getApi();
            }

            @Nonnull
            @Override
            public Set<ResolvedDependency> getCommon() {
                return applicationStarter.getCommon();
            }

            @Nonnull
            @Override
            public Set<ResolvedDependency> get3rdParty() {
                return applicationStarter.get3rdParty();
            }

            @Nonnull
            @Override
            public Set<ResolvedDependency> getInternal() {
                return applicationStarter.getInternal();
            }
        };
    }

    private static class ClassPathBuilder {
        private final StringBuilder stringBuilder = new StringBuilder(1024);

        ClassPathBuilder append(Set<ResolvedDependency> dependencies, ApplicationDirectories directory) {
            if (!stringBuilder.toString().isEmpty() && !dependencies.isEmpty()) {
                stringBuilder.append(",");
            }
            stringBuilder.append(dependencies.stream()
                    .map(d -> directory + d.getFileName())
                    .collect(Collectors.joining(",")));
            return this;
        }

        public String build() {
            return stringBuilder.toString();
        }
    }
}
