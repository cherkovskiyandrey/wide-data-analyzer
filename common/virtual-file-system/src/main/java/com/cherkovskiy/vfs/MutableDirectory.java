package com.cherkovskiy.vfs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

public interface MutableDirectory extends Directory {

    /**
     * Add new directory into archive with name and content.
     * If path contains nonexistent directory entries,
     * these entries will be created with the same attributes as this file or folder.
     * Directory must end with '/'.
     * If directory, input stream is ignored.
     *
     * @param path
     * @param inputStream if null and path is file - empty file is created
     * @return DirectoryEntry if entry created successfully or nul if entry already exists.
     * @throws IOException
     */
    @Nullable
    DirectoryEntry createIfNotExists(@Nonnull String path, @Nullable InputStream inputStream, @Nullable Attributes attributes);


    /**
     * Remove file from MutableDirectory if exists.
     * Or remove directory if it empty.
     *
     * @param path
     * @param removeEmptyFolders - remove empty parent folders
     * @return true if entry existed
     * @throws IOException if file doe
     */
    boolean removeIfExists(@Nonnull DirectoryEntry path, boolean removeEmptyFolders);
}
