package com.cherkovskiy.neuron_networks.mlp;

class Pair<T, U> {
    private final T first;
    private final U second;

    Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    static <T1, U1> Pair<T1, U1> of(T1 first, U1 second) {
        return new Pair<>(first, second);
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }
}
