package com.cherkovskiy.vfs.dir;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.BaseDirectory;
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
class SimpleDirectoryImpl extends BaseDirectory implements MutableDirectory {
    private long changesCounter = 0;

    SimpleDirectoryImpl(String baseFile, boolean createIfNotExists) {
        super(baseFile);

        if (!getMainFile().exists()) {
            if (createIfNotExists) {
                try {
                    Files.createDirectories(getMainFile().toPath());
                } catch (IOException e) {
                    throw new DirectoryException(e);
                }
            } else {
                throw new IllegalArgumentException(format("%s does not exists.", baseFile));
            }
        }

        if (!getMainFile().isDirectory()) {
            throw new IllegalArgumentException(format("%s is not a directory.", baseFile));
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Nonnull
    @Override
    public Iterator<DirectoryEntry> iterator() {
        final Iterator<Path> pathStream;
        try {
            pathStream = Files.walk(getMainFile().toPath(), Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS).iterator();
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
                return new SimpleDirectoryEntry(file, getMainFile());
            }
        };
    }

    @Override
    public DirectoryEntry createIfNotExists(@Nonnull String path, @Nullable InputStream inputStream, @Nullable Attributes attributes) {
        try {
            String normalizedPath = FilenameUtils.normalize(String.join("/", getMainFile().getCanonicalPath(), path), true);
            final File newFile = FileUtils.getFile(normalizedPath);

            if (!newFile.exists()) {
                if (isDirectory(normalizedPath)) {
                    FileUtils.forceMkdir(newFile);
                } else if (!Objects.isNull(inputStream)) {
                    FileUtils.copyInputStreamToFile(inputStream, newFile);
                } else {
                    FileUtils.touch(newFile);
                }
                if (attributes != null) {
                    AttributeHelper.setAttributes(newFile, attributes);
                }
                changesCounter++;
                return new SimpleDirectoryEntry(newFile, getMainFile());
            }
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
        return null;
    }

    private boolean isDirectory(@Nonnull String file) {
        return StringUtils.isNotBlank(file) && file.endsWith("/");
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
                        result |= removeIfExists(new SimpleDirectoryEntry(parent.toFile(), getMainFile()), true);
                    }
                }
            } catch (IOException e) {
                throw new DirectoryException(e);
            }
        }
        return result;
    }
}
