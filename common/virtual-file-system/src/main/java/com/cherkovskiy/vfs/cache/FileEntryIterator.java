package com.cherkovskiy.vfs.cache;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.DirectoryEntry;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FileEntryIterator implements Iterator<DirectoryEntry> {
    private final FileCache fileCache;
    private final Iterator<String> iterator;
    private final Predicate<String> filter;
    private String currentEntry;

    public FileEntryIterator(FileCache fileCache, Predicate<String> filter) {
        this.fileCache = fileCache;
        this.iterator = fileCache.iterator();
        this.filter = filter;
    }

    @Override
    public boolean hasNext() {
        if (currentEntry != null) {
            return true;
        }
        while (iterator.hasNext()) {
            currentEntry = iterator.next();

            if (filter != null) {
                if (filter.test(currentEntry)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        currentEntry = null;
        return false;
    }

    @Override
    public DirectoryEntry next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        final String filePath = currentEntry;
        currentEntry = null;

        return new DirectoryEntry() {

            @Override
            public boolean isDirectory() {
                return !fileCache.isFile(filePath);
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
            public void markForRead() {
            }
        };
    }
}
