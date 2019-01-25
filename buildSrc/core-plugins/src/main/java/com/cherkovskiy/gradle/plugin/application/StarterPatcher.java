package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.application_context.StarterDependencyGroup;
import com.cherkovskiy.application_context.api.ResolvedDependency;
import com.cherkovskiy.application_context.api.ResolvedStarterArtifact;
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
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class StarterPatcher {

    public static ResolvedStarterArtifact patch(ResolvedStarterArtifact applicationStarter, File temporaryDir) throws IOException {
        final File patchedJar = Paths.get(temporaryDir.getAbsolutePath(), applicationStarter.getFileName()).toFile();

        FileUtils.deleteQuietly(patchedJar);
        Files.copy(applicationStarter.getFile(), patchedJar);

        try (JarDirectoryAdapter jarFile = new JarDirectoryAdapter(DirectoryFactory.defaultInstance()
                .tryDetectAndOpen(patchedJar.getAbsolutePath(), false))) {
            final Manifest manifest = jarFile.getManifest() != null ? jarFile.getManifest() : new Manifest();

            addToManifest(StarterDependencyGroup.API, applicationStarter.getApi(), manifest);
            addToManifest(StarterDependencyGroup.COMMON, applicationStarter.getCommon(), manifest);
            addToManifest(StarterDependencyGroup.INTERNAL, applicationStarter.getInternal(), manifest);
            addToManifest(StarterDependencyGroup.EXTERNAL_3RD_PARTY, applicationStarter.get3rdParty(), manifest);

            jarFile.setManifest(manifest);
        }

        return new ResolvedStarterArtifact() {
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

    private static void addToManifest(@Nonnull StarterDependencyGroup group, @Nonnull Set<ResolvedDependency> dependencies, @Nonnull Manifest manifest) {
        manifest.getMainAttributes().put(
                new Attributes.Name(group.getAttributeName()),
                dependencies.stream()
                        .map(d -> group.getPath().getPath() + d.getFileName())
                        .collect(Collectors.joining(","))
        );
    }
}
