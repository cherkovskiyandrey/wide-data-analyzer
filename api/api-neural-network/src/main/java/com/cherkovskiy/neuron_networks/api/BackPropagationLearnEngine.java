package com.cherkovskiy.neuron_networks.api;

public interface BackPropagationLearnEngine {

    BackPropagationLearnResult onlineLearn(NeuronNetwork neuronNetwork, NeuronNetworkDataSet neuronNetworkTrainSet);


    BackPropagationLearnResult batchLearn(NeuronNetwork neuronNetwork, NeuronNetworkDataSet neuronNetworkTrainSet);

    /**
     * Estimate NN is it appropriate to current dataset.
     *
     * @param neuronNetworkFromFile
     * @param neuronNetworkTrainSet
     * @return
     */
    BackPropagationLearnResult estimate(NeuronNetwork neuronNetworkFromFile, NeuronNetworkDataSet neuronNetworkTrainSet);
}
