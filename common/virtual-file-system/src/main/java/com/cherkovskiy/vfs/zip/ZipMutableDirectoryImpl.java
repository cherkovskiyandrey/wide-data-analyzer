package com.cherkovskiy.vfs.zip;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.BaseArchiveMutableDirectory;
import com.cherkovskiy.vfs.BaseAttributesImpl;
import com.cherkovskiy.vfs.MutableDirectory;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import org.apache.commons.compress.archivers.zip.*;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;

class ZipMutableDirectoryImpl extends BaseArchiveMutableDirectory implements MutableDirectory {
    ZipMutableDirectoryImpl(String archiveName, boolean createIfNotExists) {
        super(archiveName, createIfNotExists);
    }

    @Override
    protected void unpack() {
        try (final ZipFile zipFile = new ZipFile(getMainFile())) {
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

    @Override
    protected void pack() {
        try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(getMainFile())) {
            final ParallelScatterZipCreator scatterZipCreator = new ParallelScatterZipCreator();

            try (final ScatterZipOutputStream dirs = ScatterZipOutputStream.fileBased(File.createTempFile("scatter-dirs", "tmp"))) {
                fileCache.normalize();
                for (String filePath : fileCache) {
                    final Attributes attributes = fileCache.getAttributes(filePath);

                    if (!fileCache.isFile(filePath)) {
                        final ZipArchiveEntry zipArchiveDirEntry = new ZipArchiveEntry(filePath);
                        zipArchiveDirEntry.setMethod(ZipEntry.DEFLATED);
                        if (attributes != null && attributes.getUnixMode() != null) {
                            zipArchiveDirEntry.setUnixMode(attributes.getUnixMode());
                        }
                        dirs.addArchiveEntry(ZipArchiveEntryRequest.createZipArchiveEntryRequest(zipArchiveDirEntry, ZipMutableDirectoryImpl::createNewEmptyStream));
                    } else {
                        final ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(fileCache.getAsFile(filePath), filePath);
                        zipArchiveEntry.setMethod(ZipEntry.DEFLATED);
                        if (attributes != null && attributes.getUnixMode() != null) {
                            zipArchiveEntry.setUnixMode(attributes.getUnixMode());
                        }
                        scatterZipCreator.addArchiveEntry(zipArchiveEntry, () -> fileCache.getAsInputStream(filePath));
                    }
                }
                dirs.writeTo(zipArchiveOutputStream);
            }

            scatterZipCreator.writeTo(zipArchiveOutputStream);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new DirectoryException(e);
        }
    }

    private static InputStream createNewEmptyStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

}
