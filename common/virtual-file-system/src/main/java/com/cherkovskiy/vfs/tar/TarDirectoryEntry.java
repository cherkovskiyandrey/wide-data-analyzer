package com.cherkovskiy.vfs.tar;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.BaseAttributesImpl;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.cache.FileCache;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.file.Paths;

public class TarDirectoryEntry implements DirectoryEntry {
    private final String pathToArchive;
    private final TarArchiveInputStream tarInputStream;
    private final TarArchiveEntry currentEntry;
    private final FileCache fileCache;

    TarDirectoryEntry(TarArchiveInputStream tarInputStream, TarArchiveEntry currentEntry, String pathToArchive, FileCache fileCache) {
        this.tarInputStream = tarInputStream;
        this.currentEntry = currentEntry;
        this.pathToArchive = pathToArchive;
        this.fileCache = fileCache;
    }

    @Override
    public boolean isDirectory() {
        return currentEntry.isDirectory();
    }

    @Override
    public Attributes getAttributes() {
        return new BaseAttributesImpl(currentEntry.getMode(), currentEntry.getUserName(), currentEntry.getGroupName());
    }

    @Override
    @Nullable
    public InputStream getInputStream() {
        if (!fileCache.contain(getPath())) {
            throw new DirectoryException("Unsupported read operation without preliminary marking for read.");
        }

        return fileCache.getAsInputStream(getPath());
    }

    @Override
    @Nonnull
    public String getPath() {
        return currentEntry.getName();
    }

    @Override
    @Nullable
    public String getBaseName() {
        return !isDirectory() ? Paths.get(getPath()).getFileName().toString() : null;
    }

    @Override
    public void markToRead() {
        if (!isDirectory()) {
            if (!tarInputStream.getCurrentEntry().equals(currentEntry)) {
                throw new DirectoryException("Can`t cache file, because file iterator is not on this file position.");
            }
            fileCache.put(getPath(), tarInputStream, getAttributes());
        }
    }

    @Override
    public String toString() {
        return "TarDirectoryEntry{" +
                "path='" + getPath() + '\'' +
                ", archive=" + pathToArchive +
                '}';
    }
}
