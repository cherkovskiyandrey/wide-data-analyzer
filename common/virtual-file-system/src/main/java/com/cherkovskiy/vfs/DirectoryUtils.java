package com.cherkovskiy.vfs;

import com.cherkovskiy.vfs.exceptions.DirectoryException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class DirectoryUtils {

    public static void copyRecursive(@Nonnull Directory from, @Nullable String fromDir, @Nonnull MutableDirectory to, @Nullable String toDir) {
        final String normalizedFromDir = normalizeAsDir(fromDir);
        final String normalizedToDir = normalizeAsDir(toDir);

        final Predicate<DirectoryEntry> fromFilter;
        if (StringUtils.isNotBlank(normalizedFromDir)) {
            DirectoryEntry fromDirEntry = from.findByName(normalizedFromDir);
            if (Objects.isNull(fromDirEntry)) {
                throw new DirectoryException(format("From sub-directory: %s does not exists in from directory: %s", fromDir, from.getMainFile().getAbsolutePath()));
            }
            fromFilter = directoryEntry -> directoryEntry.getPath().startsWith(normalizedFromDir);
        } else {
            fromFilter = directoryEntry -> true;
        }

        final Set<DirectoryEntry> copyEntries = from.stream().filter(fromFilter).collect(Collectors.toSet());
        for (DirectoryEntry entry : copyEntries) {
            String targetPath = normalizedToDir.concat(entry.getPath().substring(normalizedFromDir.length()));
            DirectoryEntry toDirEntry = to.findByName(targetPath);
            if (Objects.nonNull(toDirEntry)) {
                to.removeIfExists(toDirEntry, false);
            }
            try (InputStream inputStream = entry.getInputStream()) {
                to.createIfNotExists(targetPath, inputStream, null);
            } catch (IOException e) {
                throw new DirectoryException(e);
            }
        }
    }

    @Nonnull
    public static String normalizeAsDir(String dir) {
        dir = normalizePath(dir, false);
        return StringUtils.isNotBlank(dir) && dir.endsWith("/") ? dir.concat("/") : dir;
    }


    @Nonnull
    public static String normalizePath(String path, boolean throwException) {
        final String result = FilenameUtils.normalize(path, true);
        if (StringUtils.isBlank(result)) {
            if (throwException) {
                throw new DirectoryException("Path is empty: " + path);
            }
            return "";
        }

        if (result.startsWith("/")) {
            return result.substring(1, result.length());
        }
        return result;
    }

    public static boolean isFile(@Nonnull String filePath) {
        final String normalizedName = normalizePath(filePath, false);
        return StringUtils.isNotBlank(normalizedName) && !normalizedName.endsWith("/");
    }
}
