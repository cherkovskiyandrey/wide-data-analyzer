package com.cherkovskiy.application_context.api;

import java.io.IOException;

public interface ContextBuilder {
    
    ContextBuilder setArguments(String[] args);

    ApplicationContext build() throws IOException;
}
