package com.cherkovskiy.vfs.tar.bz2;

import com.cherkovskiy.vfs.cache.FileCache;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import com.cherkovskiy.vfs.tar.TarDirectoryImpl;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

class Bz2DirectoryImpl extends TarDirectoryImpl {
    Bz2DirectoryImpl(String file) {
        super(file);
    }

    Bz2DirectoryImpl(String archiveName, FileCache fileCache) {
        super(archiveName, fileCache);
    }

    protected TarArchiveInputStream openInputStream(String file) {
        try {
            return new TarArchiveInputStream(
                    new BZip2CompressorInputStream(
                            new BufferedInputStream(
                                    new FileInputStream(file),
                                    100*1024*1024
                            )
                    )
            );
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }
}
