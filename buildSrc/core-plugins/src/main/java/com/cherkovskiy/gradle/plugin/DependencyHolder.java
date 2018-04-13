package com.cherkovskiy.gradle.plugin;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

//Don't override equals and hashcode - DependencyScanner assume these methods are default
public class DependencyHolder {

    private final String group;
    private final String name;
    private final String version;
    private final List<File> files;
    private final ConfigurationTypes type;
    private final DependencyHolder parent;
    private final DependencyHolder root;

    private DependencyHolder(Builder builder) {
        this.group = builder.group;
        this.name = builder.name;
        this.version = builder.version;
        this.files = builder.files;
        this.type = builder.type;
        this.parent = builder.parent;
        this.root = parent != null ? parent.getRoot() : this;
    }


    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public String getGroup() {
        return group;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getVersion() {
        return version;
    }

    @Nonnull
    public ConfigurationTypes getConfigurationType() {
        return type;
    }

    public List<File> getArtifacts() {
        return files;
    }

    public Optional<DependencyHolder> getParent() {
        return Optional.ofNullable(parent);
    }

    public boolean isTransitive() {
        return parent != null;
    }

    /**
     * If dependency belongs to product.
     * (either or exists into current project or as external dependency from other team in scope of this product)
     *
     * @return
     */
    public boolean isNative() {
        return StringUtils.startsWith(group, SubProjectTypes.CORE_PROJECT_GROUP);
    }

    @Nullable
    public SubProjectTypes getSubProjectType() {
        return Utils.subProjectAgainst(group, SubProjectTypes.CORE_PROJECT_GROUP).map(SubProjectTypes::ofSubGroupName).orElse(null);
    }

    /**
     * @return root dependency of tree or itself if it is root
     */
    @Nonnull
    public DependencyHolder getRoot() {
        return root;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append(group).append(":");
        result.append(name).append(":");
        result.append(version);

        if (parent != null) {
            result.append(" <- ").append(parent.toString());
        }

        return result.toString();
    }

    public boolean isSame(DependencyHolder dep) {
        return Objects.equals(dep.getGroup(), getGroup()) &&
                Objects.equals(dep.getName(), getName()) &&
                Objects.equals(dep.getVersion(), getVersion());
    }

    public DependencyDescriptor descriptor() {
        return new DependencyDescriptor(getGroup(), getName(), getVersion(), getArtifacts().stream()
                .filter(DependencyHolder::isArchive)
                .map(File::getName)
                .findFirst()
                .orElse(null)
        );
    }

    public static boolean isArchive(File file) {
        return file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".war");
    }

    public static class Builder {
        private String group;
        private String name;
        private String version;
        private List<File> files;
        private ConfigurationTypes type;
        private DependencyHolder parent;

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setFiles(List<File> files) {
            this.files = files;
            return this;
        }

        public Builder setConfigurationType(ConfigurationTypes type) {
            this.type = type;
            return this;
        }

        public Builder setParent(DependencyHolder parent) {
            this.parent = parent;
            return this;
        }

        public Builder copyOf(DependencyHolder orig) {
            this.group = orig.group;
            this.name = orig.name;
            this.version = orig.version;
            this.files = orig.files;
            this.type = orig.type;
            this.parent = orig.parent;
            return this;
        }

        public DependencyHolder build() {
            Objects.requireNonNull(group);
            Objects.requireNonNull(name);
            Objects.requireNonNull(version);
            Objects.requireNonNull(type);

            return new DependencyHolder(this);
        }
    }
}
