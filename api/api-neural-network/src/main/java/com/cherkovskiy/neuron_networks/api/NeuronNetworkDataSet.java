package com.cherkovskiy.neuron_networks.api;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public interface NeuronNetworkDataSet {

    interface TrainSet {
        NeuronNetworkInput getInput();

        NeuronNetworkOutput getOutput();
    }

    Iterator<TrainSet> iteratorOverTrainSet();

    Iterator<TrainSet> iteratorOverCheckSet();

    /**
     *  Random shuffle train set.
     *  <br>
     *  None: {@link NeuronNetworkDataSet#iteratorOverTrainSet()} always is resistance to this.
     *
     */
    void shuffleTrainSet();

    void shuffleCheckSet();

    /**
     * Return empty result if all sets have been returned or {@link NeuronNetworkDataSet#shuffleTrainSet()} has not been invoked.
     *
     * @return
     */
    Optional<TrainSet> getNextRandomTrainSet();

    Optional<TrainSet> getNextRandomCheckSet();

    List<TrainSet> getNextRandomTrainBatch(int size);

    List<TrainSet> getNextRandomCheckBatch(int size);
}
