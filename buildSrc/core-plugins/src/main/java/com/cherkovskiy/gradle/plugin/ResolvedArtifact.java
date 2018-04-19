package com.cherkovskiy.gradle.plugin;

import java.io.IOException;
import java.io.InputStream;

public interface ResolvedArtifact extends Artifact {

    String getArtifactFileName();

    InputStream openInputStream() throws IOException;

}
