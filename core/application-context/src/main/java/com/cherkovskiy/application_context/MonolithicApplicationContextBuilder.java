package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.ApplicationContext;
import com.cherkovskiy.application_context.api.ContextBuilder;

public class MonolithicApplicationContextBuilder implements ContextBuilder {
    @Override
    public ContextBuilder setArguments(String[] args) {
        throw new UnsupportedOperationException("It is not supported yet.");
    }

    @Override
    public ApplicationContext build() {
        //TODO: реализация спеки пункт 7 (вся логика загрузки и инициализаиции тут. Потом растащу по классам и интерфейсам)
        //TODO: реализация всех рантайм проверок описанных в спеке


        return new MonolithicApplicationContext();
    }
}
