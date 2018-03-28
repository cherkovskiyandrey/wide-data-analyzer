package com.cherkovskiy.vfs.dir;

import com.cherkovskiy.vfs.Directory;
import com.cherkovskiy.vfs.DirectoryProvider;
import com.cherkovskiy.vfs.MutableDirectory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class SimpleDirectoryProvider implements DirectoryProvider {
    @Override
    public boolean isSupportedFile(String file) {
        final String extension = FilenameUtils.getExtension(file);
        return StringUtils.isEmpty(extension) || extension.equalsIgnoreCase("dir");
    }

    @Override
    public Directory createInstance(String file) {
        return new SimpleDirectoryImpl(file, false);
    }

    @Override
    public List<String> getSupportedFormats() {
        return Collections.singletonList(".dir");
    }

    @Override
    public MutableDirectory createMutableInstance(String file, boolean createIfNotExists) {
        return new SimpleDirectoryImpl(file, createIfNotExists);
    }
}
