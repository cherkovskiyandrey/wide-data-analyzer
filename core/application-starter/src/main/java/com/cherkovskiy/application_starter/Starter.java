package com.cherkovskiy.application_starter;

import com.cherkovskiy.application_context.MonolithicApplicationContextBuilder;
import com.cherkovskiy.application_context.api.ContextBuilder;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class Starter implements Consumer<String[]> {

    @Override
    public void accept(String[] args) {
        startApplication(args);
    }

    private void startApplication(@Nonnull String[] args) {
        final ContextBuilder contextBuilder = new MonolithicApplicationContextBuilder();

        contextBuilder
                //TODO: some other stuff
                .setArguments(args)
                .build();
    }
}
