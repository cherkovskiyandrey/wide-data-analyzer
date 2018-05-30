package com.cherkovskiy.vfs.cache;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.BaseAttributesImpl;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.cherkovskiy.vfs.DirectoryUtils.isFileEntry;
import static com.cherkovskiy.vfs.DirectoryUtils.normalizeRelativePath;

@NotThreadSafe
public class FileSystemCache implements FileCache {

    private final static class DirectoryComparator implements Comparator<String> {

        @Override
        public int compare(String left, String right) {
            if (!isFileEntry(left) && isFileEntry(right)) {
                return -1;
            } else if (isFileEntry(left) && !isFileEntry(right)) {
                return 1;
            }
            int depth = StringUtils.countMatches(left, "/") - StringUtils.countMatches(right, "/");
            if (depth != 0) {
                return depth;
            }
            return left.compareTo(right);
        }
    }

    private final File baseTmpDirectory;
    private final SortedMap<String, Attributes> entryToAttr = Maps.newTreeMap(new DirectoryComparator());


    public FileSystemCache() {
        try {
            this.baseTmpDirectory = Files.createTempDirectory(null).toFile();
            FileUtils.forceDeleteOnExit(this.baseTmpDirectory);
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    @Override
    public void close() {
        FileUtils.deleteQuietly(baseTmpDirectory);
    }

    @Override
    public boolean contain(String path) {
        return entryToAttr.containsKey(normalizeRelativePath(path, false));
    }

    @Override
    public InputStream getAsInputStream(String path) {
        if (!isFileEntry(path)) {
            return null;
        }
        try {
            return new FileInputStream(mapToUniqueFileInTmpDir(normalizeRelativePath(path, true)).toFile());
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    @Override
    public void put(String path, InputStream inputStream, Attributes attributes) {
        path = normalizeRelativePath(path, true);
        if (!contain(path)) {
            if (isFileEntry(path)) {
                try {
                    final Path file = mapToUniqueFileInTmpDir(path);
                    FileUtils.deleteQuietly(file.toFile());

                    if (!Objects.isNull(inputStream)) {
                        try (final OutputStream out = new FileOutputStream(file.toFile())) {
                            IOUtils.copy(inputStream, out);
                        }
                    } else {
                        FileUtils.touch(file.toFile());
                    }
                } catch (IOException e) {
                    throw new DirectoryException(e);
                }
            }
            entryToAttr.put(path, attributes);
        }
    }

    @Override
    public boolean remove(String path, boolean removeEmptyFolders) {
        path = normalizeRelativePath(path, true);
        boolean result = false;

        if (isFileEntry(path)) {
            try {
                final Path file = mapToUniqueFileInTmpDir(path);
                FileUtils.deleteQuietly(file.toFile());
                entryToAttr.remove(path);
                result = true;
            } catch (IOException e) {
                throw new DirectoryException(e);
            }
        } else {
            final String prefix = path;

            if (entryToAttr.keySet().stream()
                    .filter(name -> !name.equals(prefix))
                    .noneMatch(name -> name.contains(prefix))) {

                entryToAttr.remove(path);
                result = true;
            }
        }
        if (removeEmptyFolders) {
            String parent = getParentPath(path);
            if (StringUtils.isNotBlank(parent)) {
                result |= remove(parent, true);
            }
        }
        return result;
    }

    @Override
    public File getAsFile(String path) {
        if (!isFileEntry(path)) {
            return null;
        }
        try {
            return mapToUniqueFileInTmpDir(normalizeRelativePath(path, true)).toFile();
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    @Override
    public Attributes getAttributes(String filePath) {
        return entryToAttr.get(filePath);
    }

    @Override
    public void normalize() {
        for (String path : Lists.newArrayList(entryToAttr.keySet())) {
            if (isFileEntry(path)) {
                final String parent = findParent(path);
                if (StringUtils.isNotBlank(parent)) {
                    final Attributes attributes = entryToAttr.get(parent);
                    applyAttributesToParents(path, attributes);
                } else {
                    final String firstSibling = entryToAttr.firstKey();
                    if (isFileEntry(firstSibling)) {
                        final Attributes attributes = entryToAttr.get(path);
                        applyAttributesToParents(path, new BaseAttributesImpl(null, attributes.getOwner(), attributes.getGroup()));
                    } else {
                        final Attributes attributes = entryToAttr.get(firstSibling);
                        applyAttributesToParents(path, attributes);
                    }
                }
            } else {
                final Attributes attributes = entryToAttr.get(path);
                applyAttributesToParents(path, attributes);
            }
        }
    }

    private String findParent(String path) {
        while (StringUtils.isNotBlank(path = getParentPath(path))) {
            if (entryToAttr.containsKey(path)) {
                return path;
            }
        }
        return null;
    }

    private void applyAttributesToParents(String path, Attributes attributes) {
        while (StringUtils.isNotBlank(path = getParentPath(path))) {
            if (!entryToAttr.containsKey(path)) {
                entryToAttr.put(path, attributes);
            }
        }
    }

    private Path mapToUniqueFileInTmpDir(String path) throws IOException {
        String uniqueName = UUID.nameUUIDFromBytes(path.getBytes(StandardCharsets.UTF_8)).toString();
        return Paths.get(baseTmpDirectory.getCanonicalPath(), uniqueName);
    }


    // a/b/c ->  a/b/ (file)
    // a/b/  ->  a/   (dir)
    // a/    ->  ""   (dir)
    // a     ->  ""   (file)
    //       ->  ""
    // ""    ->  ""
    private static String getParentPath(String path) {
        return StringUtils.isNotBlank(path) ?
                path.substring(0, path.substring(0, path.length() - 1).lastIndexOf("/") + 1)
                : "";
    }

    @Override
    @Nonnull
    public Iterator<String> iterator() {
        return entryToAttr.keySet().iterator();
    }
}
