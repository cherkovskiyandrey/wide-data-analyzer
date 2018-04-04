package com.cherkovskiy.vfs;

import com.cherkovskiy.vfs.cache.CacheDirectoryEntry;
import com.cherkovskiy.vfs.cache.CacheDirectoryEntryIterator;
import com.cherkovskiy.vfs.cache.FileCache;
import com.cherkovskiy.vfs.cache.FileSystemCache;
import com.cherkovskiy.vfs.exceptions.DirectoryException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static java.lang.String.format;

public abstract class BaseArchiveMutableDirectory extends BaseDirectory implements MutableDirectory {
    protected final FileCache fileCache = new FileSystemCache();

    public BaseArchiveMutableDirectory(String fileName, boolean createIfNotExists) {
        super(fileName);

        if (!createIfNotExists) {
            if (!getMainFile().exists()) {
                throw new IllegalArgumentException(format("%s does not exists", fileName));
            }
            if (!getMainFile().isFile()) {
                throw new IllegalArgumentException(format("%s is not a file", fileName));
            }
            unpack();
        } else {
            if (getMainFile().exists()) {
                if (!getMainFile().isFile()) {
                    throw new IllegalArgumentException(format("%s is not a file", fileName));
                }
                unpack();
            }
        }
    }

    @Override
    public DirectoryEntry createIfNotExists(@Nonnull String path, @Nullable InputStream inputStream, @Nullable Attributes attributes) {
        if (!isOpen()) {
            throw new DirectoryException(format("File %s is closed!", getMainFile().getAbsoluteFile()));
        }
        if (!fileCache.contain(path)) {
            fileCache.put(path, inputStream, attributes);
            return new CacheDirectoryEntry(fileCache, path);
        }
        return null;
    }

    @Override
    public boolean removeIfExists(@Nonnull DirectoryEntry path, boolean removeEmptyFolders) {
        if (!isOpen()) {
            throw new DirectoryException(format("File %s is closed!", getMainFile().getAbsoluteFile()));
        }
        return fileCache.remove(path.getPath(), removeEmptyFolders);
    }

    @Override
    public void close() throws IOException {
        try {
            pack();
        } finally {
            fileCache.close();
            super.close();
        }
    }

    @Override
    @Nonnull
    public Iterator<DirectoryEntry> iterator() {
        if (!isOpen()) {
            throw new DirectoryException(format("File %s is closed!", getMainFile().getAbsoluteFile()));
        }
        return new CacheDirectoryEntryIterator(fileCache, null);
    }

    @Nullable
    @Override
    public DirectoryEntry findByName(@Nonnull String entryName) {
        return fileCache.contain(entryName) ? new CacheDirectoryEntry(fileCache, entryName) : null;
    }

    /**
     * Pack content.
     */
    protected abstract void pack();

    /**
     * Unpack content.
     */
    protected abstract void unpack();
}
