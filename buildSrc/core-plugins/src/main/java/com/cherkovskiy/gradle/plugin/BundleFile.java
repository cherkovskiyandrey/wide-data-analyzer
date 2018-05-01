package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.gradle.plugin.api.BundleArtifact;
import com.cherkovskiy.gradle.plugin.api.Dependency;
import com.cherkovskiy.gradle.plugin.api.ResolvedBundleArtifact;
import com.cherkovskiy.gradle.plugin.api.ResolvedDependency;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.DirectoryFactory;
import com.cherkovskiy.vfs.zip.JarDirectoryAdapter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slieb.throwables.FunctionWithThrowable;
import org.slieb.throwables.SuppressedException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

//todo: move to application-context (?)
public class BundleFile implements BundleArtifact {
    public static final String BUNDLE_NAME = "WDA-Bundle-Name";
    public static final String BUNDLE_VERSION = "WDA-Bundle-Version";
    public static final String EXPORTED_SERVICES = "WDA-Bundle-Exported-Services";
    public static final String IS_DEPENDENCIES_EMBEDDED = "WDA-Bundle-Dependencies-Embedded";

    private final File archive;
    private final String name;
    private final String version;
    private final boolean isEmbedded;
    private final Map<BundleDependencyGroup, Set<ManifestArtifact>> depGroupToArtifacts;

    public BundleFile(File archive) throws IOException {
        this.archive = archive;
        try (JarDirectoryAdapter bundleJar = new JarDirectoryAdapter(DirectoryFactory.defaultInstance()
                .tryDetectAndOpen(archive.getAbsolutePath(), false))) {
            final Manifest manifest = bundleJar.getManifest();
            final Attributes attributes = Objects.requireNonNull(manifest).getMainAttributes();

            this.name = attributes.getValue(new Attributes.Name(BUNDLE_NAME));
            this.version = attributes.getValue(new Attributes.Name(BUNDLE_VERSION));
            this.isEmbedded = Boolean.parseBoolean(attributes.getValue(new Attributes.Name(IS_DEPENDENCIES_EMBEDDED)));
            this.depGroupToArtifacts = new ImmutableMap.Builder<BundleDependencyGroup, Set<ManifestArtifact>>()
                    .putAll(Arrays.stream(BundleDependencyGroup.values())
                            .collect(toMap(Function.identity(),
                                    dg -> parseDependency(attributes, dg),
                                    (l, r) -> l)))
                    .build();
        }
    }

    private Set<ManifestArtifact> parseDependency(Attributes attributes, BundleDependencyGroup dependencyGroup) {
        String dependenciesFromManifest = attributes.getValue(new Attributes.Name(dependencyGroup.getAttributeName()));
        return ManifestArtifact.fromManifestString(dependenciesFromManifest);
    }

    @Override
    public boolean isEmbedded() {
        return isEmbedded;
    }

    @Nonnull
    @Override
    public Set<Dependency> getApiExport() {
        return createCopy(BundleDependencyGroup.API_EXPORT);
    }

    @Nonnull
    @Override
    public Set<Dependency> getApiImport() {
        return createCopy(BundleDependencyGroup.API_IMPORT);
    }

    @Nonnull
    @Override
    public Set<Dependency> getCommon() {
        return createCopy(BundleDependencyGroup.COMMON);
    }

    @Nonnull
    @Override
    public Set<Dependency> getImplExternal() {
        return createCopy(BundleDependencyGroup.IMPL_EXTERNAL);
    }

    @Nonnull
    @Override
    public Set<Dependency> getImplInternal() {
        return createCopy(BundleDependencyGroup.IMPL_INTERNAL);
    }

    private Set<Dependency> createCopy(BundleDependencyGroup group) {
        Set<Dependency> result = Sets.newTreeSet(Dependency.COMPARATOR);
        result.addAll(depGroupToArtifacts.get(group));
        return result;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "BundleFile{" +
                "archive=" + archive +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", isEmbedded=" + isEmbedded +
                '}';
    }

    public ResolvedBundleArtifact resolveTo(File targetDirectory) throws IOException {
        if (!isEmbedded()) {
            throw new IllegalStateException(format("Could not resolve is not embedded bundle file: %s", archive.getAbsoluteFile()));
        }

        return new ResolvedAdapter(targetDirectory);
    }

    private class ResolvedAdapter implements ResolvedBundleArtifact {
        private final Map<BundleDependencyGroup, Set<ResolvedDependency>> depGroupToResolvedDep;

        ResolvedAdapter(File targetDirectory) throws IOException {
            try (JarDirectoryAdapter bundleJar = new JarDirectoryAdapter(DirectoryFactory.defaultInstance()
                    .tryDetectAndOpen(archive.getAbsolutePath(), false))) {

                this.depGroupToResolvedDep =
                        SuppressedException.unwrapSuppressedException(() ->
                                        BundleFile.this.depGroupToArtifacts.entrySet().stream()
                                                .map(entry -> Pair.of(
                                                        entry.getKey(),
                                                        entry.getValue().stream()
                                                                .map(FunctionWithThrowable.castFunctionWithThrowable(
                                                                        art -> unpackDependency(entry.getKey(), art, bundleJar, targetDirectory)))
                                                                .collect(toSet()))
                                                )
                                                .collect(toMap(Pair::getLeft, Pair::getRight, (l, r) -> l))
                                , IOException.class);
            }
        }

        private ResolvedDependency unpackDependency(BundleDependencyGroup group,
                                                    ManifestArtifact art,
                                                    JarDirectoryAdapter bundleJar,
                                                    File targetDirectory) throws IOException {

            final String depFileInJar = group.getPath() + art.getFileName();
            final DirectoryEntry dep = bundleJar.findByName(depFileInJar);
            if (dep == null) {
                throw new IOException(format("Corrupted bundle file %s. Could not find file: %s",
                        bundleJar.getMainFile().getAbsoluteFile(), depFileInJar));
            }

            final File depFile = new File(targetDirectory, depFileInJar);
            try (InputStream inputStream = dep.getInputStream()) {
                if (Objects.isNull(inputStream)) {
                    throw new IOException(format("Corrupted bundle file %s. Could not read file: %s",
                            bundleJar.getMainFile().getAbsoluteFile(), depFileInJar));
                }
                FileUtils.copyToFile(inputStream, depFile);
            }

            return new ResolvedArtifactAdapter(depFile, art);
        }


        @Override
        public boolean isEmbedded() {
            return BundleFile.this.isEmbedded();
        }

        @Nonnull
        @Override
        public File getFile() {
            return BundleFile.this.archive;
        }

        @Nonnull
        @Override
        public Set<ResolvedDependency> getApiExport() {
            return createCopy(BundleDependencyGroup.API_EXPORT);
        }

        @Nonnull
        @Override
        public Set<ResolvedDependency> getApiImport() {
            return createCopy(BundleDependencyGroup.API_IMPORT);
        }

        @Nonnull
        @Override
        public Set<ResolvedDependency> getCommon() {
            return createCopy(BundleDependencyGroup.COMMON);
        }

        @Nonnull
        @Override
        public Set<ResolvedDependency> getImplExternal() {
            return createCopy(BundleDependencyGroup.IMPL_EXTERNAL);
        }

        @Nonnull
        @Override
        public Set<ResolvedDependency> getImplInternal() {
            return createCopy(BundleDependencyGroup.IMPL_INTERNAL);
        }

        private Set<ResolvedDependency> createCopy(BundleDependencyGroup group) {
            Set<ResolvedDependency> result = Sets.newTreeSet(Dependency.COMPARATOR);
            result.addAll(depGroupToResolvedDep.get(group));
            return result;
        }

        @Nonnull
        @Override
        public String getName() {
            return BundleFile.this.getName();
        }

        @Nonnull
        @Override
        public String getVersion() {
            return BundleFile.this.getVersion();
        }
    }

    private static class ResolvedArtifactAdapter implements ResolvedDependency {
        private final File archive;
        private final ManifestArtifact manifestArtifact;

        ResolvedArtifactAdapter(File archive, ManifestArtifact manifestArtifact) {
            this.archive = archive;
            this.manifestArtifact = manifestArtifact;
        }

        @Nonnull
        @Override
        public String getGroup() {
            return manifestArtifact.getGroup();
        }

        @Nonnull
        @Override
        public String getName() {
            return manifestArtifact.getName();
        }

        @Nonnull
        @Override
        public String getVersion() {
            return manifestArtifact.getVersion();
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
            return Objects.equals(manifestArtifact, that.manifestArtifact);
        }

        @Override
        public int hashCode() {
            return Objects.hash(manifestArtifact);
        }
    }
}
