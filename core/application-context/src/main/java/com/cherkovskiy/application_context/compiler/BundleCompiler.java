package com.cherkovskiy.application_context.compiler;


import com.cherkovskiy.application_context.api.ClassesProvider;
import com.cherkovskiy.application_context.api.bundles.ResolvedBundleArtifact;

import javax.annotation.Nonnull;

public class BundleCompiler {

    public BundleCompiler(@Nonnull ClassesProvider classesProvider) {
        //todo
    }

    /**
     * Recompile origin bundle and take into account current loaded bundles.
     * Some export API dependencies and bundle itself can be changed.
     * In this case {@link ResolvedBundleArtifact#reloadNumber()} can be incremented.
     *
     * @param origBundle
     * @return
     * @throws CompileException
     */
    @Nonnull
    public ResolvedBundleArtifact compile(@Nonnull ResolvedBundleArtifact origBundle) throws CompileException {
        //todo
    }
}
