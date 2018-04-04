package com.cherkovskiy.vfs.cache;

import com.cherkovskiy.vfs.DirectoryEntry;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class CacheDirectoryEntryIterator implements Iterator<DirectoryEntry> {
    private final FileCache fileCache;
    private final Iterator<String> iterator;
    private final Predicate<String> filter;
    private String currentEntry;

    public CacheDirectoryEntryIterator(FileCache fileCache, Predicate<String> filter) {
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

        return new CacheDirectoryEntry(fileCache, filePath);
    }
}
