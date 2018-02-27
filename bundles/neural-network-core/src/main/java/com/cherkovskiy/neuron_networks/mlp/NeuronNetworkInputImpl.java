package com.cherkovskiy.neuron_networks.mlp;

import com.cherkovskiy.neuron_networks.api.NeuronNetworkInput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NeuronNetworkInputImpl implements NeuronNetworkInput {
    private final List<Double> inputValues;

    NeuronNetworkInputImpl(List<Double> inputValues) {
        this.inputValues = new ArrayList<>(inputValues);
    }

    @Override
    public int size() {
        return inputValues.size();
    }

    @Override
    public Collection<? extends Double> getInput() {
        return Collections.unmodifiableList(inputValues);
    }
}
