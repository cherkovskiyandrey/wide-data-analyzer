package com.cherkovskiy.vfs.tar;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.BaseArchiveMutableDirectory;
import com.cherkovskiy.vfs.BaseAttributesImpl;
import com.cherkovskiy.vfs.MutableDirectory;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

public class TarMutableDirectoryImpl extends BaseArchiveMutableDirectory implements MutableDirectory {

    protected TarMutableDirectoryImpl(String archiveName, boolean createIfNotExists) {
        super(archiveName, createIfNotExists);
    }

    protected TarArchiveOutputStream openOutputStream(File file) {
        try {
            return new TarArchiveOutputStream(new FileOutputStream(file));
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    protected TarArchiveInputStream openInputStream(File file) {
        try {
            return new TarArchiveInputStream(new FileInputStream(file));
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    @Override
    protected void unpack() {
        try (TarArchiveInputStream stream = openInputStream(getMainFile())) {
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

    @Override
    protected void pack() {
        try (TarArchiveOutputStream tarArchiveOutputStream = openOutputStream(getMainFile())) {
            fileCache.normalize();
            for (String filePath : fileCache) {
                final Attributes attributes = fileCache.getAttributes(filePath);
                final File file = fileCache.getAsFile(filePath);

                final TarArchiveEntry tarArchiveEntry = file != null ?
                        new TarArchiveEntry(fileCache.getAsFile(filePath), filePath) :
                        new TarArchiveEntry(filePath);

                if (attributes != null) {
                    if (attributes.getUnixMode() != null) tarArchiveEntry.setMode(attributes.getUnixMode());
                    if (StringUtils.isNotBlank(attributes.getOwner()))
                        tarArchiveEntry.setUserName(attributes.getOwner());
                    if (StringUtils.isNotBlank(attributes.getGroup()))
                        tarArchiveEntry.setGroupName(attributes.getGroup());
                }

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
}
