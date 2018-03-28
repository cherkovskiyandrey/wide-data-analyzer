package com.cherkovskiy.vfs.tar;

import com.cherkovskiy.vfs.Directory;
import com.cherkovskiy.vfs.DirectoryProvider;
import com.cherkovskiy.vfs.MutableDirectory;
import org.apache.commons.io.FilenameUtils;

import java.util.Collections;
import java.util.List;

public class TarDirectoryProvider implements DirectoryProvider {

    @Override
    public boolean isSupportedFile(String file) {
        return "tar".equalsIgnoreCase(FilenameUtils.getExtension(file));
    }

    @Override
    public Directory createInstance(String file) {
        return new TarDirectoryImpl(file);
    }

    @Override
    public List<String> getSupportedFormats() {
        return Collections.singletonList("tar");
    }

    @Override
    public MutableDirectory createMutableInstance(String file, boolean createIfNotExists) {
        return new TarMutableDirectoryImpl(file, createIfNotExists);
    }
}
