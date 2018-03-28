package com.cherkovskiy.vfs;

import java.util.List;

public interface DirectoryProvider {
    boolean isSupportedFile(String file);

    Directory createInstance(String file);

    List<String> getSupportedFormats();

    MutableDirectory createMutableInstance(String file, boolean createIfNotExists);
}
