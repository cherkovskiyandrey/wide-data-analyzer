package com.cherkovskiy.vfs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;

public interface DirectoryEntry {

    /**
     * @return true for directory entry and false for regular file
     */
    boolean isDirectory();

    /**
     * Return attributes if supported.
     *
     * @return
     */
    @Nullable
    Attributes getAttributes();

    /**
     * Open and return input stream for this file object.
     *
     * @return
     */
    @Nullable
    InputStream getInputStream();

    /**
     * @return path to this file relatively archive
     */
    @Nonnull
    String getPath();

    /**
     * @return name of file
     */
    @Nullable
    String getBaseName();

    /**
     * Mark this file to read.
     * Can use to improve performance.
     * Can make a sense to invoke this method when first scan directory is doing,
     * because some archives does not support random access to file.
     * As result some implementations could read and cache (or load to temporary folder) content of this file.
     * <p>
     * Usually this method is invoked when stream is walking.
     */
    void markToRead();

    /**
     * Represent file entry as string.
     *
     * @return
     */
    String toString();
}
