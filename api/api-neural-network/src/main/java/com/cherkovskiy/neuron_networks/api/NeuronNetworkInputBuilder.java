package com.cherkovskiy.neuron_networks.api;

import java.util.Collection;

public interface NeuronNetworkInputBuilder {

    NeuronNetworkInputBuilder setInputValues(Collection<Double> inputs);

    NeuronNetworkInput build();
}
