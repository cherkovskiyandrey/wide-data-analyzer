package com.cherkovskiy.neuron_networks.api;

import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;

import java.io.IOException;
import java.io.InputStream;

public interface NeuronNetworkBuilder {

    NeuronNetworkBuilder inputsNeurons(int amount);

    NeuronNetworkBuilder addHiddenLevel(int amount);

    NeuronNetworkBuilder outputNeurons(int amount);

    NeuronNetworkBuilder useBias(boolean b);

    NeuronNetworkBuilder setActivationFunction(ActivationFunction activationFunction);

    NeuronNetworkBuilder setActivationFunction(BasicActivationFunction activationFunction);

    /**
     * Build {@link NeuronNetwork} from options.
     *
     * @return
     */
    NeuronNetwork build() throws ServiceNotFoundException;

    /**
     * Build {@link NeuronNetwork} from xml file.
     * Options are ignored.
     *
     * @param from
     * @return
     */
    NeuronNetwork build(InputStream from) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ServiceNotFoundException;
}
