package com.cherkovskiy.vfs.cache;

import com.cherkovskiy.vfs.Attributes;
import com.cherkovskiy.vfs.BaseAttributesImpl;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.UUID;

@NotThreadSafe
public class FileSystemCache implements FileCache {

    private final static class DirectoryComparator implements Comparator<String> {

        @Override
        public int compare(String left, String right) {
            if (!isFileHelper(left) && isFileHelper(right)) {
                return -1;
            } else if (isFileHelper(left) && !isFileHelper(right)) {
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
        return entryToAttr.containsKey(normalizePath(path));
    }

    @Override
    public InputStream getAsInputStream(String path) {
        if (!isFile(path)) {
            return null;
        }
        try {
            return new FileInputStream(mapToUniqueFileInTmpDir(normalizePath(path)).toFile());
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    @Override
    public void put(String path, InputStream inputStream, Attributes attributes) {
        path = normalizePath(path);
        if (!contain(path)) {
            if (isFile(path)) {
                try {
                    final Path file = mapToUniqueFileInTmpDir(path);
                    FileUtils.deleteQuietly(file.toFile());
                    try (final OutputStream out = new FileOutputStream(file.toFile())) {
                        IOUtils.copy(inputStream, out);
                    }
                } catch (IOException e) {
                    throw new DirectoryException(e);
                }
            }
            entryToAttr.put(path, attributes);
        }
    }

    private static String normalizePath(String path) {
        final String result = FilenameUtils.normalize(path, true);
        if (StringUtils.isBlank(result)) {
            throw new DirectoryException("Path is empty: " + path);
        }

        if (result.charAt(0) == '/') {
            return result.substring(1, result.length());
        }
        return result;
    }

    @Override
    public boolean remove(String path, boolean removeEmptyFolders) {
        path = normalizePath(path);
        boolean result = false;

        if (isFile(path)) {
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
        if (!isFile(path)) {
            return null;
        }
        try {
            return mapToUniqueFileInTmpDir(normalizePath(path)).toFile();
        } catch (IOException e) {
            throw new DirectoryException(e);
        }
    }

    @Override
    public Attributes getAttributes(String filePath) {
        return entryToAttr.get(filePath);
    }

    @Override
    public boolean isFile(String filePath) {
        return isFileHelper(filePath);
    }

    private static boolean isFileHelper(String filePath) {
        final String normalizedName = normalizePath(filePath);
        return !normalizedName.endsWith("/");
    }

    @Override
    public void normalize() {
        for (String path : Lists.newArrayList(entryToAttr.keySet())) {
            if (isFile(path)) {
                final String parent = findParent(path);
                if (StringUtils.isNotBlank(parent)) {
                    final Attributes attributes = entryToAttr.get(parent);
                    applyAttributesToParents(path, attributes);
                } else {
                    final String firstSibling = entryToAttr.firstKey();
                    if (isFile(firstSibling)) {
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
