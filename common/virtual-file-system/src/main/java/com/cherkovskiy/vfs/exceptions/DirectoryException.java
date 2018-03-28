package com.cherkovskiy.vfs.exceptions;

public class DirectoryException extends RuntimeException {
    public DirectoryException(String s) {
        super(s);
    }

    public DirectoryException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DirectoryException(Throwable throwable) {
        super(throwable);
    }
}
