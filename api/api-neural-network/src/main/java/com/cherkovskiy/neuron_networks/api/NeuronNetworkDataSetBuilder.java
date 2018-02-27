package com.cherkovskiy.neuron_networks.api;

import java.io.InputStream;

public interface NeuronNetworkDataSetBuilder {

    NeuronNetworkDataSetBuilder setTrainInputAndOutput(NeuronNetworkInput input, NeuronNetworkOutput output);

    NeuronNetworkDataSetBuilder setVerifyingInputAndOutput(NeuronNetworkInput input, NeuronNetworkOutput output);

    /**
     * Part of all data to use to verify.
     *
     * @param part
     * @return
     */
    NeuronNetworkDataSetBuilder useToVerify(float part);

    NeuronNetworkDataSet build();

    NeuronNetworkDataSet build(InputStream inputStream);
}
