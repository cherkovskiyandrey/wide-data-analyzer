package com.cherkovskiy.vfs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;
import java.io.File;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@NotThreadSafe
public interface Directory extends Iterable<DirectoryEntry>, Closeable {

    /**
     * @return main file: archive or directory
     */
    File getMainFile();

    /**
     * Try to find entry by name.
     * Directory must be ended by '/' symbol.
     *
     * @param entryName
     * @return
     */
    @Nullable
    DirectoryEntry findByName(@Nonnull String entryName);


    default Stream<DirectoryEntry> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    boolean isOpen();
}
