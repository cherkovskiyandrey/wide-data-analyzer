package com.cherkovskiy.vfs.dir;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.MutableDirectory;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Objects;

import static java.lang.String.format;

@NotThreadSafe
class SimpleDirectoryImpl implements MutableDirectory {
    private final File directory;
    private long changesCounter = 0;

    SimpleDirectoryImpl(String baseFile, boolean createIfNotExists) {
        if (StringUtils.isEmpty(baseFile)) {
            throw new IllegalArgumentException("BaseFile is null or empty.");
        }
        this.directory = new File(baseFile);

        if (!directory.exists()) {
            if (createIfNotExists) {
                try {
                    Files.createDirectories(directory.toPath());
                } catch (IOException e) {
                    throw new DirectoryException(e);
                }
            } else {
                throw new IllegalArgumentException(format("%s does not exists.", baseFile));
            }
        }

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(format("%s is not a directory.", baseFile));
        }
    }

    @Override
    public void close() throws IOException {
    }

    @Nonnull
    @Override
    public Iterator<DirectoryEntry> iterator() {
        final Iterator<Path> pathStream;
        try {
            pathStream = Files.walk(directory.toPath(), Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS).iterator();
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
        final long currentChanges = changesCounter;

        return new Iterator<DirectoryEntry>() {

            @Override
            public boolean hasNext() {
                if (changesCounter != currentChanges) {
                    throw new ConcurrentModificationException();
                }
                return pathStream.hasNext();
            }

            @Override
            public DirectoryEntry next() {
                if (changesCounter != currentChanges) {
                    throw new ConcurrentModificationException();
                }
                final File file = pathStream.next().toFile();
                return new SimpleDirectoryEntry(file, directory);
            }
        };
    }

    @Override
    public boolean createIfNotExists(@Nonnull String path, @Nullable InputStream inputStream, @Nullable Attributes attributes) {
        try {
            final File newFile = Paths.get(directory.getCanonicalPath(), path).toFile();
            if (!newFile.exists()) {
                if (isDirectory(newFile)) {
                    FileUtils.forceMkdir(newFile);
                } else {
                    FileUtils.copyInputStreamToFile(Objects.requireNonNull(inputStream), newFile);
                }
                if (attributes != null) {
                    AttributeHelper.setAttributes(newFile, attributes);
                }
                changesCounter++;
                return true;
            }
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
        return false;
    }

    private boolean isDirectory(@Nonnull File file) {
        return file.exists() ? file.isDirectory() : FilenameUtils.normalize(file.getAbsolutePath(), true).endsWith("/");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean removeIfExists(@Nonnull DirectoryEntry directoryEntry, boolean removeEmptyFolders) {
        if (!(directoryEntry instanceof SimpleDirectoryEntry)) {
            throw new IllegalArgumentException(String.format("DirectoryEntry is not subtype of SimpleDirectoryEntry: %s", directoryEntry.toString()));
        }

        boolean result = false;
        final File removedFile = ((SimpleDirectoryEntry) directoryEntry).getFile();

        if (removedFile.exists()) {
            try {
                if (directoryEntry.isDirectory()) {
                    if (!Files.list(removedFile.toPath()).findAny().isPresent()) {
                        Files.delete(removedFile.toPath());
                        result = true;
                        changesCounter++;
                    }
                } else if (Files.isRegularFile(removedFile.toPath())) {
                    Files.delete(removedFile.toPath());
                    result = true;
                    changesCounter++;
                }

                if (removeEmptyFolders) {
                    final Path parent = removedFile.toPath().normalize().getParent();
                    if (parent != null) {
                        result |= removeIfExists(new SimpleDirectoryEntry(parent.toFile(), directory), true);
                    }
                }
            } catch (IOException e) {
                throw new DirectoryException(e);
            }
        }
        return result;
    }
}
