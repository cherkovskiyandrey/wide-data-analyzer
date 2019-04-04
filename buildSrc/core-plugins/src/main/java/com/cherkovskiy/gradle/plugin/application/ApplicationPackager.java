package com.cherkovskiy.gradle.plugin.application;

import com.cherkovskiy.application_context.ApplicationDirectories;
import com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.api.bundles.ResolvedDependency;
import com.cherkovskiy.vfs.Directory;
import com.cherkovskiy.vfs.DirectoryFactory;
import com.cherkovskiy.vfs.DirectoryUtils;
import com.cherkovskiy.vfs.MutableDirectory;
import com.cherkovskiy.vfs.dir.SimpleDirectoryProvider;
import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static java.lang.String.format;

class ApplicationPackager implements Closeable {
    private final MutableDirectory directory;

    ApplicationPackager(String targetArtifact) {
        this.directory = DirectoryFactory.defaultInstance().tryDetectAndOpen(targetArtifact, true);
        createDirectories(directory);
    }

    @Override
    public void close() throws IOException {
        directory.close();
    }

    public void copyResources(File resourcesPath, String to) throws IOException {
        if (!resourcesPath.exists() || !resourcesPath.isDirectory()) {
            throw new GradleException(format("Could not file bin resources in %s", resourcesPath.getAbsoluteFile()));
        }

        try (Directory binResources = new SimpleDirectoryProvider().createInstance(resourcesPath.getAbsolutePath())) {
            DirectoryUtils.copyRecursive(binResources, "", directory, to);
        }
    }

    public void putApi(Set<ResolvedDependency> api) throws IOException {
        putDependencies(ApplicationDirectories.API, api);
    }

    public void putCommon(Set<ResolvedDependency> common) throws IOException {
        putDependencies(ApplicationDirectories.LIB_COMMON, common);
    }

    public void putExternal(Set<ResolvedDependency> external) throws IOException {
        putDependencies(ApplicationDirectories.LIB, external);
    }

    public void putInternal(Set<ResolvedDependency> internal) throws IOException {
        putDependencies(ApplicationDirectories.LIB_INTERNAL, internal);
    }

    public void putApplicationStarter(File applicationStarter) throws IOException {
        try (InputStream inputStream = FileUtils.openInputStream(applicationStarter)) {
            directory.createIfNotExists(ApplicationDirectories.BIN.getPath() + applicationStarter.getName(), inputStream, null);
        }
    }

    public void putAppBundle(ResolvedBundleArtifact artifact) throws IOException {
        try (InputStream inputStream = FileUtils.openInputStream(artifact.getFile())) {
            directory.createIfNotExists(ApplicationDirectories.APP.getPath() + artifact.getFile().getName(), inputStream, null);
        }
    }

    public void putBundles(Set<ResolvedBundleArtifact> bundles) throws IOException {
        for (ResolvedBundleArtifact bundle : bundles) {
            try (InputStream inputStream = FileUtils.openInputStream(bundle.getFile())) {
                directory.createIfNotExists(ApplicationDirectories.BUNDLES.getPath() + bundle.getFile().getName(), inputStream, null);
            }
        }
    }

    private void putDependencies(ApplicationDirectories where, Set<ResolvedDependency> dependencies) throws IOException {
        for (ResolvedDependency dependency : dependencies) {
            try (InputStream inputStream = FileUtils.openInputStream(dependency.getFile())) {
                directory.createIfNotExists(where.getPath() + dependency.getFile().getName(), inputStream, null);
            }
        }
    }

    private void createDirectories(MutableDirectory directory) {
        for (ApplicationDirectories dir : ApplicationDirectories.values()) {
            directory.createIfNotExists(dir.getPath(), null, null);
        }
    }
}
