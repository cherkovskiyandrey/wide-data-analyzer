package com.cherkovskiy.neuron_networks.api;

import java.util.List;
import java.util.Optional;


public interface BackPropagationLearnResult {

    interface Result {
        List<Double> getErrorsPerCycle();

        double getBestError();

        long getCycleNumberBestError();

        NeuronNetwork getTopologyBestError();
    }

    List<Long> getEpochAmountPerCycle();

    Result getResultForTrainSet();

    Optional<Result> getResultForVerifyingSet();
}
