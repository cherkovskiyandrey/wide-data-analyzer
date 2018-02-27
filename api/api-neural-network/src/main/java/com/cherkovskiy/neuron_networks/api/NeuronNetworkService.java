package com.cherkovskiy.neuron_networks.api;


import javax.annotation.Nonnull;


public interface NeuronNetworkService {

    @Nonnull
    NeuronNetworkBuilder createFeedforwardBuilder();

    @Nonnull
    BackPropagationLearnEngineBuilder createBackPropagationLearnEngineBuilder();

    @Nonnull
    ResilientBackPropagationLearnEngineBuilder createResilientBackPropagationLearnEngineBuilder();

    @Nonnull
    NeuronNetworkInputBuilder createInputBuilder();

    @Nonnull
    NeuronNetworkDataSetBuilder createTrainSetBuilder();

    @Nonnull
    NeuronNetworkOutputBuilder createOutputBuilder();
}
