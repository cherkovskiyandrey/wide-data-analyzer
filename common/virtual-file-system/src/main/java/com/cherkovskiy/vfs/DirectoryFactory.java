package com.cherkovskiy.vfs;

import com.cherkovskiy.vfs.exceptions.DirectoryException;

public interface DirectoryFactory {

    /**
     * Try to detect archive format and open it for read only access.
     *
     * @param file
     * @return
     */
    Directory tryDetectAndOpenReadOnly(String file);

    /**
     * Try to detect archive format and open it for read/write access.
     * <br>
     * <b>IMPORTANT:</b> for some archive types it may be heavy operation, because
     * some archive can`t support read and write access from single stream.
     * In this case usually first of all archive will be unpacked and will be packed on close operation.
     *
     * @param file
     * @param createIfNotExists
     * @throws DirectoryException if format could not be recognized
     * @return
     */
    MutableDirectory tryDetectAndOpen(String file, boolean createIfNotExists);

    static DirectoryFactory defaultInstance() {
        return new DirectoryFactoryImpl();
    }
}
