package com.cherkovskiy.neuron_networks.api;

import java.util.Collection;

public interface NeuronNetworkInput {

    int size();

    Collection<? extends Double> getInput();
}
