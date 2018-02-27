package com.cherkovskiy.neuron_networks.api;

import java.io.IOException;
import java.io.OutputStream;

public interface NeuronNetwork {

    NeuronNetworkOutput process(NeuronNetworkInput input);

    void writeTo(OutputStream to) throws IOException, ClassNotFoundException;
}
