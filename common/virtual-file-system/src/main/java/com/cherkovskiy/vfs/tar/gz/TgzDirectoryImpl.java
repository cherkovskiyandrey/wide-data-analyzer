package com.cherkovskiy.vfs.tar.gz;

import com.cherkovskiy.vfs.cache.FileCache;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import com.cherkovskiy.vfs.tar.TarDirectoryImpl;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.FileInputStream;
import java.io.IOException;

public class TgzDirectoryImpl extends TarDirectoryImpl {
    public TgzDirectoryImpl(String file) {
        super(file);
    }

    TgzDirectoryImpl(String archiveName, FileCache fileCache) {
        super(archiveName, fileCache);
    }

    protected TarArchiveInputStream openInputStream(String file) {
        try {
            return new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }
}
