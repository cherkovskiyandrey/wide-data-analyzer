package com.cherkovskiy.application_context.configuration.utils;

import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;

public final class CollectionBuilder {

    @FunctionalInterface
    interface Builder<T extends Collection> {
        T build();

        static <T extends Collection> Builder<T> wrap(Supplier<T> builder) {
            return builder::get;
        }
    }

    private final static ImmutableMap<Class<? extends Collection>, Builder<?>> TOKE_TO_CREATOR =
            new ImmutableMap.Builder<Class<? extends Collection>, Builder<?>>()

                    .put(Collection.class,      Builder.<Collection>wrap(ArrayList::new))
                    .put(List.class,            Builder.<List>wrap(ArrayList::new))
                    .put(ArrayList.class,       Builder.<ArrayList>wrap(ArrayList::new))
                    .put(LinkedList.class,      Builder.<LinkedList>wrap(LinkedList::new))
                    .put(Set.class,             Builder.<Set>wrap(HashSet::new))
                    .put(SortedSet.class,       Builder.<SortedSet>wrap(TreeSet::new))
                    .put(HashSet.class,         Builder.<HashSet>wrap(HashSet::new))
                    .put(TreeSet.class,         Builder.<TreeSet>wrap(TreeSet::new))
                    .put(LinkedHashSet.class,   Builder.<LinkedHashSet>wrap(LinkedHashSet::new))

                    .build();

    @SuppressWarnings("unchecked")
    public static <T extends Collection> T createBy(Class<T> typeToken) {
        final Builder<T> creator = (Builder<T>) TOKE_TO_CREATOR.get(typeToken);
        if (creator == null) {
            throw new IllegalStateException(format("Unsupported collection type: %s. Supported types: %s",
                    typeToken.getName(),
                    TOKE_TO_CREATOR.keySet().stream().map(Class::getName).collect(Collectors.joining(", "))
            ));
        }
        return creator.build();
    }

    private CollectionBuilder() {
    }

}
