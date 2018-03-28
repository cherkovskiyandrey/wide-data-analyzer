package com.cherkovskiy.vfs.tar.gz;

import com.cherkovskiy.vfs.Directory;
import com.cherkovskiy.vfs.DirectoryProvider;
import com.cherkovskiy.vfs.MutableDirectory;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class TgzDirectoryProvider implements DirectoryProvider {
    private final static ImmutableList<String> SUPPORTED_EXTENSIONS = ImmutableList.of("tgz", "tar.gz");

    @Override
    public boolean isSupportedFile(String file) {
        return SUPPORTED_EXTENSIONS.stream().anyMatch(file::endsWith);
    }

    @Override
    public Directory createInstance(String file) {
        return new TgzDirectoryImpl(file);
    }

    @Override
    public List<String> getSupportedFormats() {
        return SUPPORTED_EXTENSIONS;
    }

    @Override
    public MutableDirectory createMutableInstance(String file, boolean createIfNotExists) {
        return new TgzMutableDirectoryImpl(file, createIfNotExists);
    }
}
