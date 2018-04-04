package com.cherkovskiy.vfs.zip;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.DirectoryEntry;
import com.cherkovskiy.vfs.MutableDirectory;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Simple adapter over jar archives.
 * Like {@link java.util.jar.JarFile}
 */
@NotThreadSafe
public class JarDirectoryAdapter implements MutableDirectory {
    private final MutableDirectory jarDirectory;
    private DirectoryEntry manifestEntry;

    public JarDirectoryAdapter(MutableDirectory jarDirectory) {
        if (!(jarDirectory instanceof ZipMutableDirectoryImpl) ||
                !"jar".equalsIgnoreCase(FilenameUtils.getExtension(jarDirectory.getMainFile().getAbsolutePath()))) {
            throw new DirectoryException("Archive is not a jar type.");
        }
        if (!jarDirectory.isOpen()) {
            throw new DirectoryException("Archive is already closed.");
        }

        this.jarDirectory = jarDirectory;
        this.manifestEntry = jarDirectory.findByName(JarFile.MANIFEST_NAME);
    }


    /**
     * Return manifest from archive.
     * If manifest does not exists - return null.
     *
     * @return
     */
    @Nullable
    public Manifest getManifest() {
        if (manifestEntry != null) {
            try {
                try (InputStream inputStream = manifestEntry.getInputStream()) {
                    return new Manifest(inputStream);
                }
            } catch (IOException e) {
                throw new DirectoryException(e);
            }
        }
        return null;
    }

    /**
     * Write or rewrite manifest file.
     *
     * @param manifest
     */
    public void setManifest(@Nonnull Manifest manifest) {
        try (final ByteArrayOutputStream manifestOutput = new ByteArrayOutputStream(1024)) {

            final java.util.jar.Attributes attributes = manifest.getMainAttributes();
            if (!attributes.containsKey(java.util.jar.Attributes.Name.MANIFEST_VERSION)) {
                attributes.put(java.util.jar.Attributes.Name.MANIFEST_VERSION, "1.0");
            }
            manifest.write(manifestOutput);

            final Attributes entryAttributes = manifestEntry != null ? manifestEntry.getAttributes() : null;
            if (manifestEntry != null) {
                if (!jarDirectory.removeIfExists(manifestEntry, false)) {
                    throw new DirectoryException("Could not rewrite manifest in " + jarDirectory.getMainFile().getAbsolutePath());
                }
            }

            try (InputStream manifestInput = new ByteArrayInputStream(manifestOutput.toByteArray())) {
                if ((manifestEntry = jarDirectory.createIfNotExists(JarFile.MANIFEST_NAME, manifestInput, entryAttributes)) == null) {
                    throw new DirectoryException("Could not rewrite manifest in " + jarDirectory.getMainFile().getAbsolutePath());
                }
            }
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }


    @Override
    public DirectoryEntry createIfNotExists(@Nonnull String path, @Nullable InputStream inputStream, @Nullable Attributes attributes) {
        return jarDirectory.createIfNotExists(path, inputStream, attributes);
    }

    @Override
    public boolean removeIfExists(@Nonnull DirectoryEntry path, boolean removeEmptyFolders) {
        return jarDirectory.removeIfExists(path, removeEmptyFolders);
    }

    @Override
    public void close() throws IOException {
        jarDirectory.close();
    }

    @Override
    @Nonnull
    public Iterator<DirectoryEntry> iterator() {
        return jarDirectory.iterator();
    }

    @Override
    public File getMainFile() {
        return jarDirectory.getMainFile();
    }

    @Nullable
    @Override
    public DirectoryEntry findByName(@Nonnull String entryName) {
        return jarDirectory.findByName(entryName);
    }

    @Override
    public boolean isOpen() {
        return jarDirectory.isOpen();
    }
}
