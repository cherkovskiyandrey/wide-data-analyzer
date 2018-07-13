package com.cherkovskiy.application_starter;

import com.cherkovskiy.application_context.api.ContextBuilder;
import com.cherkovskiy.application_context.MonolithicApplicationContextBuilder;

public class Starter {

    public static void main(String[] args) {
        final ContextBuilder contextBuilder = new MonolithicApplicationContextBuilder();

        contextBuilder
                //TODO: some other stuff
                .setArguments(args)
                .build();
    }
}
