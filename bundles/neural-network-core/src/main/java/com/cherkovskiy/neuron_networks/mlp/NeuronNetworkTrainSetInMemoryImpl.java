package com.cherkovskiy.neuron_networks.mlp;

import com.cherkovskiy.neuron_networks.api.NeuronNetworkInput;
import com.cherkovskiy.neuron_networks.api.NeuronNetworkOutput;
import com.cherkovskiy.neuron_networks.api.NeuronNetworkDataSet;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

class NeuronNetworkTrainSetInMemoryImpl implements NeuronNetworkDataSet {
    private final List<Pair<NeuronNetworkInput, NeuronNetworkOutput>> trainInput; //TODO: get rid of Pair and use TrainSet implementation

    NeuronNetworkTrainSetInMemoryImpl(List<Pair<NeuronNetworkInput, NeuronNetworkOutput>> trainInput) {
        this.trainInput = trainInput;
    }

    //todo
//    @Nonnull
//    @Override
//    public Iterator<TrainSet> iterator() {
//        final Iterator<Pair<NeuronNetworkInput, NeuronNetworkOutput>> originalItr = trainInput.iterator();
//
//        return new Iterator<TrainSet>() {
//            @Override
//            public boolean hasNext() {
//                return originalItr.hasNext();
//            }
//
//            @Override
//            public TrainSet next() {
//                final Pair<NeuronNetworkInput, NeuronNetworkOutput> origVal = originalItr.next();
//
//                return new TrainSet() {
//                    @Override
//                    public NeuronNetworkInput getInput() {
//                        return origVal.getFirst();
//                    }
//
//                    @Override
//                    public NeuronNetworkOutput getOutput() {
//                        return origVal.getSecond();
//                    }
//                };
//            }
//        };
//    }

    @Override
    public Iterator<TrainSet> iteratorOverTrainSet() {
        //todo
        throw new UnsupportedOperationException("Is not implemented yet.");
    }

    @Override
    public Iterator<TrainSet> iteratorOverCheckSet() {
        //todo
        throw new UnsupportedOperationException("Is not implemented yet.");
    }

    @Override
    public void shuffleTrainSet() {
        //todo
        throw new UnsupportedOperationException("Is not implemented yet.");
    }

    @Override
    public void shuffleCheckSet() {
        //todo
        throw new UnsupportedOperationException("Is not implemented yet.");
    }

    @Override
    public Optional<TrainSet> getNextRandomTrainSet() {
        //todo
        throw new UnsupportedOperationException("Is not implemented yet.");
    }

    @Override
    public Optional<TrainSet> getNextRandomCheckSet() {
        //todo
        throw new UnsupportedOperationException("Is not implemented yet.");
    }

    @Override
    public List<TrainSet> getNextRandomTrainBatch(int size) {
        //todo
        throw new UnsupportedOperationException("Is not implemented yet.");
    }

    @Override
    public List<TrainSet> getNextRandomCheckBatch(int size) {
        //todo
        throw new UnsupportedOperationException("Is not implemented yet.");
    }
}
