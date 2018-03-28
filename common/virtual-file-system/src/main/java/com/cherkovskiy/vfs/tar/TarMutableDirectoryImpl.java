package com.cherkovskiy.vfs.tar;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.BaseAttributesImpl;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.MutableDirectory;
import com.cherkovskiy.vfs.cache.FileCache;
import com.cherkovskiy.vfs.cache.FileEntryIterator;
import com.cherkovskiy.vfs.cache.FileSystemCache;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Iterator;

import static java.lang.String.format;

public class TarMutableDirectoryImpl implements MutableDirectory {
    private final String archiveName;
    private final FileCache fileCache = new FileSystemCache();

    public TarMutableDirectoryImpl(String archiveName, boolean createIfNotExists) {
        if (StringUtils.isEmpty(archiveName)) {
            throw new IllegalArgumentException("ArchiveName is null or empty");
        }
        this.archiveName = archiveName;

        final File archive = new File(archiveName);

        if (!createIfNotExists) {
            if (!archive.exists()) {
                throw new IllegalArgumentException(format("%s does not exists", archiveName));
            }

            if (!archive.isFile()) {
                throw new IllegalArgumentException(format("%s is not a file", archiveName));
            }

            unpack(archiveName);

        } else {
            if (archive.exists()) {

                if (!archive.isFile()) {
                    throw new IllegalArgumentException(format("%s is not a file", archiveName));
                }

                unpack(archiveName);
            }
        }
    }

    @Override
    public boolean createIfNotExists(@Nonnull String path, @Nullable InputStream inputStream, @Nullable Attributes attributes) {
        if (!fileCache.contain(path)) {
            fileCache.put(path, inputStream, attributes);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeIfExists(@Nonnull DirectoryEntry path, boolean removeEmptyFolders) {
        return fileCache.remove(path.getPath(), removeEmptyFolders);
    }

    @Override
    public void close() throws IOException {
        try {
            pack();
        } finally {
            fileCache.close();
        }
    }

    protected TarArchiveOutputStream openOutputStream(String file) {
        try {
            return new TarArchiveOutputStream(new FileOutputStream(file));
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    protected TarArchiveInputStream openInputStream(String file) {
        try {
            return new TarArchiveInputStream(new FileInputStream(file));
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    private void unpack(String archiveName) {
        try (TarArchiveInputStream stream = openInputStream(archiveName)) {
            TarArchiveEntry entry;

            while ((entry = stream.getNextTarEntry()) != null) {
                fileCache.put(
                        entry.getName(),
                        entry.isFile() ? stream : null,
                        new BaseAttributesImpl(entry.getMode(), entry.getUserName(), entry.getGroupName())
                );
            }
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    private void pack() {
        try (TarArchiveOutputStream tarArchiveOutputStream = openOutputStream(archiveName)) {
            fileCache.normalize();
            for (String filePath : fileCache) {
                final Attributes attributes = fileCache.getAttributes(filePath);
                final File file = fileCache.getAsFile(filePath);

                final TarArchiveEntry tarArchiveEntry = file != null ?
                        new TarArchiveEntry(fileCache.getAsFile(filePath), filePath) :
                        new TarArchiveEntry(filePath);

                if (attributes.getUnixMode() != null) tarArchiveEntry.setMode(attributes.getUnixMode());
                if (StringUtils.isNotBlank(attributes.getOwner())) tarArchiveEntry.setUserName(attributes.getOwner());
                if (StringUtils.isNotBlank(attributes.getGroup())) tarArchiveEntry.setGroupName(attributes.getGroup());

                tarArchiveOutputStream.putArchiveEntry(tarArchiveEntry);

                if (file != null) {
                    try (InputStream inputStream = fileCache.getAsInputStream(filePath)) {
                        IOUtils.copy(inputStream, tarArchiveOutputStream);
                    } finally {
                        tarArchiveOutputStream.closeArchiveEntry();
                    }
                }
            }
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    @Override
    @Nonnull
    public Iterator<DirectoryEntry> iterator() {
        return new FileEntryIterator(fileCache, null);
    }
}
