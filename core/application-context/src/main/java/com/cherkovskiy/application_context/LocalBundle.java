package com.cherkovskiy.application_context;

import com.cherkovskiy.application_context.api.Bundle;
import com.cherkovskiy.application_context.api.ResolvedBundleArtifact;
import com.cherkovskiy.application_context.configuration.environments.ConfigurationImpl;

import javax.annotation.Nonnull;

class LocalBundle implements Bundle {
    @Nonnull
    private final ResolvedBundleArtifact resolvedBundleArtifact;
    @Nonnull
    private final ConfigurationImpl globalConfiguration;


    LocalBundle(@Nonnull ResolvedBundleArtifact resolvedBundleArtifact, @Nonnull ConfigurationImpl globalConfiguration) {
        this.resolvedBundleArtifact = resolvedBundleArtifact;
        this.globalConfiguration = globalConfiguration;
    }

    @Override
    public void load() {
        //todo: нужно сюда передать бустраповский класс лоадер - подумать как
        // + как-то нужно написать класс лоадер кастомный с возможностью выгрузки jar и связанных с ним классов
        // но если мы загружаем бандл с расширенным интерфейсом по идее это же возможно, значит нужно
        // чтобы класс лоадер начал выдывать новые классы для новых вызовов
        // а что если кто-то ещё расширит интерфейс и он будет конфликтовать с недавно обновлённым,
        // т.е. есть удаление методов? - нахрен такое - кинуть исключение в рантайме можно, (такое исключено при сборке)
        // а добавляемые методы делать default
        /**
         * Если запретить менять апи на уровне приложения?
         * Тогда можно не перезапуская приложение релоадить бандлы только с одним и тем же апи, что не гипко по идее
         * К тому же для микросервисной архитектуры по идее нет смысла запрещать расширять интерфейс.
         */
        //resolvedBundleArtifact.
    }

    @Override
    public void unload() {
        //todo
    }
}
