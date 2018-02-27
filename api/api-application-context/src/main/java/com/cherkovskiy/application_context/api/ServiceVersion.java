package com.cherkovskiy.application_context.api;

public interface ServiceVersion {

    int getMajor();

    int getMinor();

    String getSnapshot();
}
