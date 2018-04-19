package com.cherkovskiy.gradle.plugin;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Set;

//todo: move to application-context (?)
public class BundleFile implements BundleArtifact, Closeable {
    public static final String BUNDLE_NAME = "WDA-Bundle-Name";
    public static final String BUNDLE_VERSION = "WDA-Bundle-Version";
    public static final String EXPORTED_SERVICES = "WDA-Bundle-Exported-Services";
    public static final String IS_DEPENDENCIES_EMBEDDED = "WDA-Bundle-Dependencies-Embedded";

    public BundleFile(File archive) {
        //todo
    }

    //    //TODO: think about BundleArtifact ???
//        try (JarDirectoryAdapter bundleJar = new JarDirectoryAdapter(DirectoryFactory.defaultInstance()
//            .tryDetectAndOpen(bundleFile.getAbsolutePath(), false))) {
//
//        final Manifest manifest = bundleJar.getManifest();
//        final Attributes attributes = manifest.getMainAttributes();
//
//        //TODO: new Attributes.Name(IS_DEPENDENCIES_EMBEDDED) - тогда нужно брать прям из архива, т.к. в dependencies может и не быть, если использовался кастомный манифест!
//        String apiExportsFromManifest = attributes.getValue(new Attributes.Name(DependencyGroup.API_EXPORT.getAttributeName()));
//        final Set<ManifestArtifact> apiExport = ManifestArtifact.fromManifestString(apiExportsFromManifest);

    //todo

    @Override
    public boolean isEmbedded() {
        return false;
    }

    @Override
    public Set<Artifact> getApiExport() {
        return null;
    }

    @Override
    public Set<Artifact> getApiImport() {
        return null;
    }

    @Override
    public Set<Artifact> getCommon() {
        return null;
    }

    @Override
    public Set<Artifact> getImplExternal() {
        return null;
    }

    @Override
    public Set<Artifact> getImplInternal() {
        return null;
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void close() throws IOException {
        //todo
    }

    public ResolvedBundleArtifact asSelfResolved() {
        //todo: если embedded то можно разрезолвить!
    }
}
