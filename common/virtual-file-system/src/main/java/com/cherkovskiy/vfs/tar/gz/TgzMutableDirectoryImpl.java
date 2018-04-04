package com.cherkovskiy.vfs.tar.gz;

import com.cherkovskiy.vfs.exceptions.DirectoryException;
import com.cherkovskiy.vfs.tar.TarMutableDirectoryImpl;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.*;

class TgzMutableDirectoryImpl extends TarMutableDirectoryImpl {
    TgzMutableDirectoryImpl(String archiveName, boolean createIfNotExists) {
        super(archiveName, createIfNotExists);
    }

    @Override
    protected TarArchiveOutputStream openOutputStream(File file) {
        try {
            return new TarArchiveOutputStream(
                    new GzipCompressorOutputStream(
                            new BufferedOutputStream(
                                    new FileOutputStream(file),
                                    100 * 1024 * 1024
                            )
                    )
            );
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    @Override
    protected TarArchiveInputStream openInputStream(File file) {
        try {
            return new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }
}
