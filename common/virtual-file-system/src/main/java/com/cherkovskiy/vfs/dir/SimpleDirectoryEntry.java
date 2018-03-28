package com.cherkovskiy.vfs.dir;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

class SimpleDirectoryEntry implements DirectoryEntry {
    private final String path;
    private final File file;
    private final File directory;

    SimpleDirectoryEntry(File file, File parentDirectory) throws DirectoryException {
        this.file = file;
        this.directory = parentDirectory;
        this.path = parentDirectory.toPath().relativize(file.toPath()).toString();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public Attributes getAttributes() {
        return AttributeHelper.getAttributes(file);
    }

    @Override
    @Nullable
    public InputStream getInputStream() throws DirectoryException {
        if (isDirectory()) {
            return null;
        }
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new DirectoryException(e);
        }
    }

    @Override
    @Nonnull
    public String getPath() {
        return path;
    }

    @Override
    @Nullable
    public String getBaseName() {
        return !isDirectory() ? FilenameUtils.getName(path) : null;
    }

    @Override
    public void markForRead() {
    }

    File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "SimpleDirectoryEntry{" +
                "path='" + path + '\'' +
                ", baseCatalog=" + directory.getAbsoluteFile().toString() +
                '}';
    }
}
