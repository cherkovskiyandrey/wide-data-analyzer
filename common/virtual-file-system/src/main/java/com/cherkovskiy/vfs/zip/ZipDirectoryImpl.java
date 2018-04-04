package com.cherkovskiy.vfs.zip;

import com.cherkovskiy.vfs.BaseDirectory;
import com.cherkovskiy.vfs.Directory;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.cache.FileCache;
import com.cherkovskiy.vfs.cache.FileSystemCache;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.lang.String.format;

class ZipDirectoryImpl extends BaseDirectory implements Directory {
    private final ZipFile zipFile;
    private final FileCache fileCache;
    private final boolean isFileCacheExternal;

    ZipDirectoryImpl(String archiveName) {
        this(archiveName, null);
    }

    private ZipDirectoryImpl(String archiveName, FileCache fileCache) {
        super(archiveName);
        if (!getMainFile().exists()) {
            throw new IllegalArgumentException(format("ArchiveName does not exists: %s", archiveName));
        }

        if (!getMainFile().isFile()) {
            throw new IllegalArgumentException(format("ArchiveName is not a file: %s", archiveName));
        }

        try {
            this.zipFile = new ZipFile(archiveName);
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
        this.fileCache = fileCache != null ? fileCache : new FileSystemCache();
        this.isFileCacheExternal = fileCache != null;
    }

    @Override
    public void close() throws IOException {
        try {
            if (zipFile != null) {
                zipFile.close();
            }
            if (!isFileCacheExternal) {
                fileCache.close();
            }
        } finally {
            super.close();
        }
    }

    @Nonnull
    @Override
    public Iterator<DirectoryEntry> iterator() {
        if (!isOpen()) {
            throw new DirectoryException(format("File %s is closed!", getMainFile().getAbsoluteFile()));
        }
        final Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntriesInPhysicalOrder();

        return new Iterator<DirectoryEntry>() {
            private ZipArchiveEntry currentEntry = null;

            @Override
            public boolean hasNext() {
                if (currentEntry != null) {
                    return true;
                }

                while (zipEntries.hasMoreElements()) {
                    final ZipArchiveEntry cur = zipEntries.nextElement();
                    if (zipFile.canReadEntryData(cur) &&
                            StringUtils.isNotEmpty(cur.getName())) {
                        currentEntry = cur;
                        return true;
                    }
                }

                return false;
            }

            @Override
            public DirectoryEntry next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                ZipArchiveEntry result = currentEntry;
                currentEntry = null;
                return new ZipDirectoryEntry(zipFile, result, getMainFile().getAbsolutePath(), fileCache);
            }
        };
    }
}
