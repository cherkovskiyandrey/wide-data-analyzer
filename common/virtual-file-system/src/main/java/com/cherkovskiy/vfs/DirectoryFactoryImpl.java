package com.cherkovskiy.vfs;

import com.cherkovskiy.vfs.dir.SimpleDirectoryProvider;
import com.cherkovskiy.vfs.exceptions.DirectoryException;
import com.cherkovskiy.vfs.tar.TarDirectoryProvider;
import com.cherkovskiy.vfs.tar.bz2.Bz2DirectoryProvider;
import com.cherkovskiy.vfs.tar.gz.TgzDirectoryProvider;
import com.cherkovskiy.vfs.zip.ZipDirectoryProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static java.lang.String.format;

class DirectoryFactoryImpl implements DirectoryFactory {

    private static final ImmutableList<DirectoryProvider> SUPPORTED_DIRECTORIES = new ImmutableList.Builder<DirectoryProvider>()

            .add(new SimpleDirectoryProvider())
            .add(new ZipDirectoryProvider())
            .add(new TarDirectoryProvider())
            .add(new TgzDirectoryProvider())
            .add(new Bz2DirectoryProvider())

            .build();


    private final static ImmutableSet<String> SUPPORTED_FORMATS = new ImmutableSet.Builder<String>()
            .addAll(SUPPORTED_DIRECTORIES.stream().flatMap(provider -> provider.getSupportedFormats().stream()).collect(Collectors.toList()))
            .build();

    @Override
    public Directory tryDetectAndOpenReadOnly(String file) {
        basicChecks(file);

        return SUPPORTED_DIRECTORIES.stream()
                .filter(provider -> provider.isSupportedFile(file))
                .findFirst()
                .map(provider -> provider.createInstance(file))
                .orElseThrow(() -> new DirectoryException(format("Unsupported type of file: %s. Supported types: %s", file, SUPPORTED_FORMATS)));
    }

    @Override
    public MutableDirectory tryDetectAndOpen(String file, boolean createIfNotExists) {
        basicChecks(file);

        return SUPPORTED_DIRECTORIES.stream()
                .filter(provider -> provider.isSupportedFile(file))
                .findFirst()
                .map(provider -> provider.createMutableInstance(file, createIfNotExists))
                .orElseThrow(() -> new DirectoryException(format("Unsupported type of file: %s. Supported types: %s", file, SUPPORTED_FORMATS)));
    }

    private void basicChecks(String file) {
        if (StringUtils.isEmpty(file)) {
            throw new IllegalArgumentException(format("file name is null or empty: %s", file));
        }
    }
}
