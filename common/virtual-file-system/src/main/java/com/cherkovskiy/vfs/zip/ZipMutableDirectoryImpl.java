package com.cherkovskiy.vfs.zip;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.BaseAttributesImpl;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.MutableDirectory;
import com.cherkovskiy.vfs.cache.FileCache;
import com.cherkovskiy.vfs.cache.FileEntryIterator;
import com.cherkovskiy.vfs.cache.FileSystemCache;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import org.apache.commons.compress.archivers.zip.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;

import static java.lang.String.format;

class ZipMutableDirectoryImpl implements MutableDirectory {
    private final String archiveName;
    private final FileCache fileCache = new FileSystemCache();

    ZipMutableDirectoryImpl(String archiveName, boolean createIfNotExists) {
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

    private void unpack(String archiveName) {
        try (final ZipFile zipFile = new ZipFile(archiveName)) {
            final Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntriesInPhysicalOrder();

            while (zipEntries.hasMoreElements()) {
                final ZipArchiveEntry cur = zipEntries.nextElement();
                if (zipFile.canReadEntryData(cur) && StringUtils.isNotEmpty(cur.getName())) {
                    if (cur.isDirectory()) {
                        fileCache.put(cur.getName(), null, new BaseAttributesImpl(cur.getUnixMode(), null, null));
                    } else {
                        try (InputStream entryStream = zipFile.getInputStream(cur)) {
                            fileCache.put(cur.getName(), entryStream, new BaseAttributesImpl(cur.getUnixMode(), null, null));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    private void pack() throws IOException {
        try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(new File(archiveName))) {
            final ParallelScatterZipCreator scatterZipCreator = new ParallelScatterZipCreator();

            try (final ScatterZipOutputStream dirs = ScatterZipOutputStream.fileBased(File.createTempFile("scatter-dirs", "tmp"))) {
                fileCache.normalize();
                for (String filePath : fileCache) {
                    final Attributes attributes = fileCache.getAttributes(filePath);

                    if (!fileCache.isFile(filePath)) {
                        final ZipArchiveEntry zipArchiveDirEntry = new ZipArchiveEntry(filePath);
                        zipArchiveDirEntry.setMethod(ZipEntry.DEFLATED);
                        if (attributes.getUnixMode() != null) {
                            zipArchiveDirEntry.setUnixMode(attributes.getUnixMode());
                        }
                        dirs.addArchiveEntry(ZipArchiveEntryRequest.createZipArchiveEntryRequest(zipArchiveDirEntry, ZipMutableDirectoryImpl::createNewEmptyStream));
                    } else {
                        final ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(fileCache.getAsFile(filePath), filePath);
                        zipArchiveEntry.setMethod(ZipEntry.DEFLATED);
                        if (attributes.getUnixMode() != null) {
                            zipArchiveEntry.setUnixMode(attributes.getUnixMode());
                        }
                        scatterZipCreator.addArchiveEntry(zipArchiveEntry, () -> fileCache.getAsInputStream(filePath));
                    }
                }
                dirs.writeTo(zipArchiveOutputStream);
            }

            scatterZipCreator.writeTo(zipArchiveOutputStream);
        } catch (InterruptedException | ExecutionException e) {
            throw new DirectoryException(e);
        }
    }

    private static InputStream createNewEmptyStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    @Nonnull
    public Iterator<DirectoryEntry> iterator() {
        return new FileEntryIterator(fileCache, null);
    }
}
