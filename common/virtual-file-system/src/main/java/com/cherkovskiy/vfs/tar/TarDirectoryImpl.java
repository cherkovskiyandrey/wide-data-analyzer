package com.cherkovskiy.vfs.tar;

import com.cherkovskiy.vfs.Directory;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.cache.FileCache;
import com.cherkovskiy.vfs.cache.FileSystemCache;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import com.google.common.collect.Maps;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.lang.String.format;

public class TarDirectoryImpl implements Directory {
    private final String archiveName;
    private final FileCache fileCache;
    private final boolean isFileCacheExternal;
    private final Map<IteratorImpl, TarArchiveInputStream> iteratorToStream = Maps.newHashMap();

    public TarDirectoryImpl(String baseFile) {
        this(baseFile, null);
    }

    public TarDirectoryImpl(String archiveName, FileCache fileCache) {
        if (StringUtils.isEmpty(archiveName)) {
            throw new IllegalArgumentException(format("archiveName is null or empty: %s", archiveName));
        }
        this.archiveName = archiveName;
        final File archive = new File(archiveName);

        if (!archive.exists()) {
            throw new IllegalArgumentException(format("archiveName does not exists: %s", archiveName));
        }

        if (!archive.isFile()) {
            throw new IllegalArgumentException(format("archiveName is not a file: %s", archiveName));
        }

        this.fileCache = fileCache != null ? fileCache : new FileSystemCache();
        this.isFileCacheExternal = fileCache != null;
    }

    protected TarArchiveInputStream openInputStream(String file) {
        try {
            return new TarArchiveInputStream(new FileInputStream(file));
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (!isFileCacheExternal) {
            fileCache.close();
        }
        for (TarArchiveInputStream archive : iteratorToStream.values()) {
            archive.close();
        }
    }

    @Nonnull
    @Override
    public Iterator<DirectoryEntry> iterator() {
        return new IteratorImpl();
    }

    private class IteratorImpl implements Iterator<DirectoryEntry> {
        private final TarArchiveInputStream tarInputStream;
        private TarArchiveEntry currentEntry;


        IteratorImpl() {
            this.tarInputStream = openInputStream(archiveName);
            iteratorToStream.put(this, this.tarInputStream);
        }

        private TarArchiveEntry getNextEntry() {
            TarArchiveEntry nextEntry;
            while (true) {
                try {
                    nextEntry = tarInputStream.getNextTarEntry();
                } catch (IOException e) {
                    throw new DirectoryException(e);
                }
                if (nextEntry == null) {
                    try {
                        tarInputStream.close();
                    } catch (IOException e) {
                        throw new DirectoryException(e);
                    }
                    iteratorToStream.remove(this);
                    return null;
                }
                if (tarInputStream.canReadEntryData(nextEntry)) {
                    return nextEntry;
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (currentEntry != null) {
                return true;
            }
            currentEntry = getNextEntry();
            return currentEntry != null;
        }

        @Override
        public DirectoryEntry next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            final TarArchiveEntry result = currentEntry;
            currentEntry = null;
            return new TarDirectoryEntry(tarInputStream, result, archiveName, fileCache);
        }
    }
}
