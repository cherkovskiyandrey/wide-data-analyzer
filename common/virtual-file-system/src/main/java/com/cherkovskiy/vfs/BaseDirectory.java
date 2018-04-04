package com.cherkovskiy.vfs;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public abstract class BaseDirectory implements Directory {
    private final File file;
    private volatile boolean isOpen = true;

    public BaseDirectory(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("File name is null or empty");
        }
        this.file = new File(fileName);
    }

    @Override
    public File getMainFile() {
        return file;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Nullable
    @Override
    public DirectoryEntry findByName(@Nonnull String entryName) {
        return stream().filter(directoryEntry -> directoryEntry.getPath().equalsIgnoreCase(entryName)).findFirst().orElse(null);
    }

    @Override
    public void close() throws IOException {
        isOpen = false;
    }
}
