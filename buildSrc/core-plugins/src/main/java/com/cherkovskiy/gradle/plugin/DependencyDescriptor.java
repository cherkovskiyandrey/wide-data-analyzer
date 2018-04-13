package com.cherkovskiy.gradle.plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyDescriptor {
    public static final String GROUP_SEPARATOR = ";";
    private final static Pattern MAVEN_PATTERN = Pattern.compile("^([^:]+):([^:]+):([^:]+):([^:]*)");

    private final String group;
    private final String name;
    private final String version;
    private final String fileName;

    public DependencyDescriptor(@Nonnull String group, @Nonnull String name, @Nonnull String version, @Nullable String fileName) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.fileName = fileName;
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

    @Nullable
    public String getFileName() {
        return fileName;
    }

    @Nonnull
    public String toManifestCompatibleString() {
        final StringBuilder stringBuilder = new StringBuilder(1024);

        stringBuilder.append(group).append(":");
        stringBuilder.append(name).append(":");
        stringBuilder.append(version).append(":");

        if (Objects.nonNull(fileName)) {
            stringBuilder.append(fileName);
        }

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DependencyDescriptor that = (DependencyDescriptor) o;
        return Objects.equals(group, that.group) &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {

        return Objects.hash(group, name, version);
    }

    public static DependencyDescriptor fromManifestString(String manifestString) {
        Matcher matcher = MAVEN_PATTERN.matcher(manifestString);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid format: " + manifestString);
        }

        return new DependencyDescriptor(matcher.replaceFirst("$1"),
                matcher.replaceFirst("$2"),
                matcher.replaceFirst("$3"),
                matcher.replaceFirst("$4")
        );
    }
}
