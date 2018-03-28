package com.cherkovskiy.vfs.cache;

import com.cherkovskiy.vfs.Attributes;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;

public interface FileCache extends Iterable<String> {

    void close();

    boolean contain(String path);

    /**
     * Return null if filePath is directory.
     *
     * @param path
     * @return
     */
    @Nullable
    InputStream getAsInputStream(String path);

    /**
     * Put entry (file or directory).
     * Directory must end with '/'.
     * If directory, input stream is ignored.
     *
     * @param path
     * @param inputStream
     * @param attributes
     */
    void put(String path, InputStream inputStream, Attributes attributes);

    /**
     * Remove entry if it is a file.
     * Remove directory if it is empty.
     *
     * @param path
     * @param removeEmptyFolders - remove empty parent folders
     */
    boolean remove(String path, boolean removeEmptyFolders);

    /**
     * Return null if filePath is directory.
     *
     * @param filePath
     * @return
     */
    @Nullable
    File getAsFile(String filePath);

    @Nullable
    Attributes getAttributes(String filePath);

    boolean isFile(String filePath);

    /**
     * <ol>
     * <li>Allotment unregistered directories</li>
     * <li>Reorder items: directories -> files, short name -> long name</li>
     * </ol>
     * <p>
     * For nonexistent parent directory entries in directory path - directory path attributes will be used.
     * <p>
     * For nonexistent parent directory entries in file path,
     * these entries will be created with the attributes of parent directory,
     * if a parent directory is present, otherwise first sibling folder attributes will be used,
     * otherwise directory will be without {@link Attributes#getUnixMode()} and with other attributes from file.
     */
    void normalize();
}
