package com.cherkovskiy.vfs.zip;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.BaseAttributesImpl;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.cache.FileCache;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

class ZipDirectoryEntry implements DirectoryEntry {
    private final String pathToArchive;
    private final ZipFile zipFile;
    private final ZipArchiveEntry zipEntry;
    private final FileCache fileCache;

    ZipDirectoryEntry(ZipFile zipFile, ZipArchiveEntry zipEntry, String pathToArchive, FileCache fileCache) {
        this.zipFile = zipFile;
        this.zipEntry = zipEntry;
        this.pathToArchive = pathToArchive;
        this.fileCache = fileCache;
    }

    @Override
    public boolean isDirectory() {
        return zipEntry.isDirectory();
    }

    @Override
    public Attributes getAttributes() {
        return new BaseAttributesImpl(zipEntry.getUnixMode(), null, null);
    }

    @Override
    @Nullable
    public InputStream getInputStream() throws DirectoryException {
        if (isDirectory()) {
            return null;
        }
        try {
            return zipFile.getInputStream(zipEntry);
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    @Override
    @Nonnull
    public String getPath() {
        return zipEntry.getName();
    }

    @Override
    @Nullable
    public String getBaseName() {
        return !isDirectory() ? Paths.get(getPath()).getFileName().toString() : null;
    }

    @Override
    public void markForRead() {
        if (!isDirectory()) {
            try (final InputStream inputStream = getInputStream()) {
                fileCache.put(getPath(), inputStream, getAttributes());
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public String toString() {
        return "ZipDirectoryEntry{" +
                "path='" + getPath() + '\'' +
                ", archive=" + pathToArchive +
                '}';
    }
}
