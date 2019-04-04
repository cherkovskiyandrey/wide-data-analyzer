package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.bundles.BundleArtifact;
import com.cherkovskiy.application_context.api.bundles.Dependency;
import com.cherkovskiy.application_context.api.bundles.ServiceDescriptor;
import com.cherkovskiy.vfs.DirectoryFactory;
import com.cherkovskiy.vfs.zip.JarDirectoryAdapter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.util.stream.Collectors.toMap;

public class BundleFile implements BundleArtifact {
    public static final String BUNDLE_NAME = "WDA-Bundle-Name";
    public static final String BUNDLE_VERSION = "WDA-Bundle-Version";
    public static final String EXPORTED_SERVICES = "WDA-Bundle-Exported-Services";
    public static final String IS_DEPENDENCIES_EMBEDDED = "WDA-Bundle-Dependencies-Embedded";

    private final File archive;
    private final String name;
    private final String version;
    private final boolean isEmbedded;
    private final Set<ServiceDescriptor> services;
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
            this.services = ManifestServiceDescriptor.fromManifestString(attributes.getValue(new Attributes.Name(EXPORTED_SERVICES)));
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
    public File getFile() {
        return archive;
    }

    @Nonnull
    @Override
    public Set<ServiceDescriptor> getServices() {
        return services;
    }

    @Nullable
    @Override
    public String getStarter() {
        throw new UnsupportedOperationException();
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
        return ImmutableSortedSet.copyOf(Dependency.COMPARATOR, depGroupToArtifacts.get(group));
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
                ", services=" + services +
                ", depGroupToArtifacts=" + depGroupToArtifacts +
                '}';
    }
}
