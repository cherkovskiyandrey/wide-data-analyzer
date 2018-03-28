package com.cherkovskiy.vfs;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;

@NotThreadSafe
public interface Directory extends Iterable<DirectoryEntry>, Closeable {
}
