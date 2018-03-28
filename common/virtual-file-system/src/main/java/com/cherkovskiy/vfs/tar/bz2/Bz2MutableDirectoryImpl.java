package com.cherkovskiy.vfs.tar.bz2;

import com.cherkovskiy.vfs.exceptions.DirectoryException;
import com.cherkovskiy.vfs.tar.TarMutableDirectoryImpl;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.*;

public class Bz2MutableDirectoryImpl extends TarMutableDirectoryImpl {
    Bz2MutableDirectoryImpl(String archiveName, boolean createIfNotExists) {
        super(archiveName, createIfNotExists);
    }

    @Override
    protected TarArchiveOutputStream openOutputStream(String file) {
        try {
            return new TarArchiveOutputStream(
                    new BZip2CompressorOutputStream(
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
    protected TarArchiveInputStream openInputStream(String file) {
        try {
            return new TarArchiveInputStream(
                    new BZip2CompressorInputStream(
                            new BufferedInputStream(
                                    new FileInputStream(file),
                                    100 * 1024 * 1024
                            )
                    )
            );
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }
}
