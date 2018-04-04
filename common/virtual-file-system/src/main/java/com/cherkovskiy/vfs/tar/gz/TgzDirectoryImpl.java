package com.cherkovskiy.vfs.tar.gz;

import com.cherkovskiy.vfs.cache.FileCache;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import com.cherkovskiy.vfs.tar.TarDirectoryImpl;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class TgzDirectoryImpl extends TarDirectoryImpl {
    TgzDirectoryImpl(String file) {
        super(file);
    }

    TgzDirectoryImpl(String archiveName, FileCache fileCache) {
        super(archiveName, fileCache);
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
