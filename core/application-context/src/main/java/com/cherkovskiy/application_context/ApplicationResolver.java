package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.BundleArtifact;
import com.cherkovskiy.application_context.api.BundleResolver;
import com.cherkovskiy.application_context.api.ResolvedBundleArtifact;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


//todo
class ApplicationResolver {
    //private final BundleResolver bundleResolver = new ApplicationBundleResolver(appHome);
    ApplicationResolver(String appHome) {

    }

    public ResolvedBundleArtifact resolveApplicationBundle() {
        throw new UnsupportedOperationException("It is not supported yet.");
//        File appBundleFile = Paths.get(appHome, ApplicationDirectories.APP.getPath()).toFile();
//        BundleArtifact appBundleArtifact = new BundleFile(appBundleFile);
//        return bundleResolver.resolve(appBundleArtifact);
    }

    public List<ResolvedBundleArtifact> resolveOtherBundles() {
        throw new UnsupportedOperationException("It is not supported yet.");
//        Path onBoardBundlesPath = Paths.get(appHome, ApplicationDirectories.BUNDLES.getPath());
//        List<BundleFile> bundles = Files.walk(onBoardBundlesPath, 0)
//                .filter(p -> p.getFileName().endsWith(".jar"))
//                .map(Path::toFile)
//                .map(file -> {
//                    try {
//                        return new BundleFile(file);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .collect(Collectors.toList());
//
//
//        bundles.stream().map(bundleResolver::resolve).collect(Collectors.toList());
    }
}
