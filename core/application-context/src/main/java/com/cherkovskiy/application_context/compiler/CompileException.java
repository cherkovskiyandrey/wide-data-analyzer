package com.cherkovskiy.application_context.compiler;

public class CompileException extends Exception {
    public CompileException(String s) {
        super(s);
    }

    public CompileException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
