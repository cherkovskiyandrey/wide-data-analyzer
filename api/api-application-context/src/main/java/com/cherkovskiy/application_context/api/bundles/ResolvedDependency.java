package com.cherkovskiy.application_context.api.bundles;

import javax.annotation.Nonnull;
import java.io.File;

public interface ResolvedDependency extends Dependency {

    @Nonnull
    File getFile();

}
