package com.cherkovskiy.vfs.cache;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.DirectoryUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import java.io.InputStream;

public class CacheDirectoryEntry implements DirectoryEntry {
    private final FileCache fileCache;
    private final String filePath;

    public CacheDirectoryEntry(FileCache fileCache, String filePath) {
        this.fileCache = fileCache;
        this.filePath = filePath;
    }

    @Override
    public boolean isDirectory() {
        return !DirectoryUtils.isFileEntry(filePath);
    }

    @Override
    public Attributes getAttributes() {
        return fileCache.getAttributes(filePath);
    }

    @Override
    public InputStream getInputStream() {
        return fileCache.getAsInputStream(filePath);
    }

    @Override
    @Nonnull
    public String getPath() {
        return filePath;
    }

    @Override
    public String getBaseName() {
        return FilenameUtils.getName(filePath);
    }

    @Override
    public void markToRead() {
    }
}
