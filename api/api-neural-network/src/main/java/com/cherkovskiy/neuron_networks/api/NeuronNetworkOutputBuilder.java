package com.cherkovskiy.neuron_networks.api;

import java.util.List;

public interface NeuronNetworkOutputBuilder {

    NeuronNetworkOutputBuilder setOutputValues(List<Double> doubles);

    NeuronNetworkOutput build();
}
