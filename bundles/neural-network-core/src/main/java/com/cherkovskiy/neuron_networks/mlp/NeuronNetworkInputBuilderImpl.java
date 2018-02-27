package com.cherkovskiy.neuron_networks.mlp;

import com.cherkovskiy.neuron_networks.api.NeuronNetworkInput;
import com.cherkovskiy.neuron_networks.api.NeuronNetworkInputBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NeuronNetworkInputBuilderImpl implements NeuronNetworkInputBuilder {
    private List<Double> inputs = new ArrayList<>();

    @Override
    public NeuronNetworkInputBuilder setInputValues(Collection<Double> inputs) {
        this.inputs.clear();
        this.inputs.addAll(inputs);
        return this;
    }

    @Override
    public NeuronNetworkInput build() {
        return new NeuronNetworkInputImpl(Collections.unmodifiableList(inputs));
    }
}
