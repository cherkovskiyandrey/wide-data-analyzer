package com.cherkovskiy.gradle.plugin;

import com.cherkovskiy.gradle.plugin.api.Dependency;
import com.cherkovskiy.gradle.plugin.api.ResolvedBundleArtifact;
import com.cherkovskiy.gradle.plugin.api.ResolvedDependency;
import com.cherkovskiy.gradle.plugin.api.ServiceDescriptor;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Set;

public class ResolvedBundleFile implements ResolvedBundleArtifact {
    private File file;
    private String name;
    private String version;
    private boolean isEmbedded;

    private Set<ServiceDescriptor> services;
    private Set<ResolvedDependency> apiExport;
    private Set<ResolvedDependency> apiImport;
    private Set<ResolvedDependency> common;
    private Set<ResolvedDependency> implExternal;
    private Set<ResolvedDependency> implInternal;

    private ResolvedBundleFile(Builder builder) {
        this.file = builder.file;
        this.name = builder.name;
        this.version = builder.version;
        this.isEmbedded = builder.isEmbedded;

        this.services = ImmutableSet.copyOf(builder.services);
        this.apiExport = ImmutableSortedSet.copyOf(Dependency.COMPARATOR, builder.apiExport);
        this.apiImport = ImmutableSortedSet.copyOf(Dependency.COMPARATOR, builder.apiImport);
        this.common = ImmutableSortedSet.copyOf(Dependency.COMPARATOR, builder.common);
        this.implExternal = ImmutableSortedSet.copyOf(Dependency.COMPARATOR, builder.implExternal);
        this.implInternal = ImmutableSortedSet.copyOf(Dependency.COMPARATOR, builder.implInternal);
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
    public boolean isEmbedded() {
        return isEmbedded;
    }

    @Nonnull
    @Override
    public File getFile() {
        return file;
    }

    @Nonnull
    @Override
    public Set<ServiceDescriptor> getServices() {
        return services;
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getApiExport() {
        return apiExport;
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getApiImport() {
        return apiImport;
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getCommon() {
        return common;
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getImplExternal() {
        return implExternal;
    }

    @Nonnull
    @Override
    public Set<ResolvedDependency> getImplInternal() {
        return implInternal;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private File file;
        private String name;
        private String version;
        private boolean isEmbedded;

        private Set<ServiceDescriptor> services;
        private Set<ResolvedDependency> apiExport;
        private Set<ResolvedDependency> apiImport;
        private Set<ResolvedDependency> common;
        private Set<ResolvedDependency> implExternal;
        private Set<ResolvedDependency> implInternal;

        public Builder setServices(Set<ServiceDescriptor> services) {
            this.services = services;
            return this;
        }

        public Builder setApiExport(Set<ResolvedDependency> apiExport) {
            this.apiExport = apiExport;
            return this;
        }

        public ResolvedBundleArtifact build() {
            return new ResolvedBundleFile(this);
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setIsEmbedded(boolean isEmbedded) {
            this.isEmbedded = isEmbedded;
            return this;
        }

        public Builder setApiImport(Set<ResolvedDependency> apiImport) {
            this.apiImport = apiImport;
            return this;
        }

        public Builder setCommon(Set<ResolvedDependency> common) {
            this.common = common;
            return this;
        }

        public Builder setImplExternal(Set<ResolvedDependency> implExternal) {
            this.implExternal = implExternal;
            return this;
        }

        public Builder setImplInternal(Set<ResolvedDependency> implInternal) {
            this.implInternal = implInternal;
            return this;
        }

        public Builder setFile(File file) {
            this.file = file;
            return this;
        }
    }
}
