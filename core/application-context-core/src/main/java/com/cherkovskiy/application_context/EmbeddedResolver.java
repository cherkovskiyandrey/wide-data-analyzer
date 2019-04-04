package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.*;
import com.cherkovskiy.application_context.api.bundles.BundleArtifact;
import com.cherkovskiy.application_context.api.bundles.Dependency;
import com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.api.bundles.ResolvedDependency;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.DirectoryFactory;
import com.cherkovskiy.vfs.zip.JarDirectoryAdapter;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;

public class EmbeddedResolver implements BundleResolver {
    private final File bundleUnpackDir;

    public EmbeddedResolver(@Nonnull File bundleUnpackDir) {
        this.bundleUnpackDir = bundleUnpackDir;
    }

    @Override
    @Nonnull
    public ResolvedBundleArtifact resolve(@Nonnull BundleArtifact artifact) {
        if (!artifact.isEmbedded()) {
            throw new IllegalStateException(format("Could not resolve not embedded bundle file: %s", artifact.getFile().getAbsolutePath()));
        }

        try (JarDirectoryAdapter bundleJar = new JarDirectoryAdapter(DirectoryFactory.defaultInstance()
                .tryDetectAndOpen(artifact.getFile().getAbsolutePath(), false))) {

            return ResolvedBundleFile.builder()
                    .setName(artifact.getName())
                    .setVersion(artifact.getVersion())
                    .setIsEmbedded(artifact.isEmbedded())
                    .setFile(artifact.getFile())
                    .setServices(artifact.getServices())
                    .setApiExport(unpackDependency(BundleDependencyGroup.API_EXPORT, artifact.getApiExport(), bundleJar))
                    .setApiImport(unpackDependency(BundleDependencyGroup.API_IMPORT, artifact.getApiImport(), bundleJar))
                    .setCommon(unpackDependency(BundleDependencyGroup.COMMON, artifact.getCommon(), bundleJar))
                    .setImplExternal(unpackDependency(BundleDependencyGroup.IMPL_EXTERNAL, artifact.getImplExternal(), bundleJar))
                    .setImplInternal(unpackDependency(BundleDependencyGroup.IMPL_INTERNAL, artifact.getImplInternal(), bundleJar))
                    .build();

        } catch (IOException e) {
            throw new IllegalStateException(format("Could not resolve not embedded bundle file: %s", artifact.getFile().getAbsolutePath()), e);
        }
    }

    private Set<ResolvedDependency> unpackDependency(BundleDependencyGroup group,
                                                     Set<Dependency> dependencies,
                                                     JarDirectoryAdapter bundleJar) throws IOException {

        Set<ResolvedDependency> result = Sets.newTreeSet(Dependency.COMPARATOR);
        for (Dependency dependency : dependencies) {
            final String depFileInJar = group.getPath() + dependency.getFileName();
            final DirectoryEntry dep = bundleJar.findByName(depFileInJar);
            if (dep == null) {
                throw new IOException(format("Corrupted bundle file %s. Could not find file: %s",
                        bundleJar.getMainFile().getAbsoluteFile(), depFileInJar));
            }

            final File depFile = new File(bundleUnpackDir, depFileInJar);
            try (InputStream inputStream = dep.getInputStream()) {
                if (Objects.isNull(inputStream)) {
                    throw new IOException(format("Corrupted bundle file %s. Could not read file: %s",
                            bundleJar.getMainFile().getAbsoluteFile(), depFileInJar));
                }
                FileUtils.copyToFile(inputStream, depFile);
            }
            result.add(new ResolvedArtifactAdapter(depFile, dependency));
        }
        return result;
    }

    private static class ResolvedArtifactAdapter implements ResolvedDependency {
        private final File archive;
        private final Dependency dependency;

        ResolvedArtifactAdapter(File archive, Dependency dependency) {
            this.archive = archive;
            this.dependency = dependency;
        }

        @Nonnull
        @Override
        public String getGroup() {
            return dependency.getGroup();
        }

        @Nonnull
        @Override
        public String getName() {
            return dependency.getName();
        }

        @Nonnull
        @Override
        public String getVersion() {
            return dependency.getVersion();
        }

        @Nullable
        @Override
        public String getFileName() {
            return dependency.getName();
        }

        @Nonnull
        @Override
        public File getFile() {
            return archive;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResolvedArtifactAdapter that = (ResolvedArtifactAdapter) o;
            return Objects.equals(dependency, that.dependency);
        }

        @Override
        public int hashCode() {

            return Objects.hash(dependency);
        }
    }
}
