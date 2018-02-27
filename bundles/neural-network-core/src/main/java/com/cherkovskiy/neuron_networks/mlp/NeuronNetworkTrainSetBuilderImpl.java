package com.cherkovskiy.neuron_networks.mlp;

import com.cherkovskiy.neuron_networks.api.NeuronNetworkInput;
import com.cherkovskiy.neuron_networks.api.NeuronNetworkOutput;
import com.cherkovskiy.neuron_networks.api.NeuronNetworkDataSet;
import com.cherkovskiy.neuron_networks.api.NeuronNetworkDataSetBuilder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NeuronNetworkTrainSetBuilderImpl implements NeuronNetworkDataSetBuilder {
    private List<Pair<NeuronNetworkInput, NeuronNetworkOutput>> trainInput = new ArrayList<>();
    private List<Pair<NeuronNetworkInput, NeuronNetworkOutput>> verifyingInput = new ArrayList<>();

    @Override
    public NeuronNetworkDataSetBuilder setTrainInputAndOutput(NeuronNetworkInput input, NeuronNetworkOutput output) {
        trainInput.add(Pair.of(input, output));
        return this;
    }

    @Override
    public NeuronNetworkDataSetBuilder setVerifyingInputAndOutput(NeuronNetworkInput input, NeuronNetworkOutput output) {
        verifyingInput.add(Pair.of(input, output));
        return this;
    }

    @Override
    public NeuronNetworkDataSetBuilder useToVerify(float part) {
        //todo
        throw new UnsupportedOperationException("Is not implemented yet.");
    }

    @Override
    public NeuronNetworkDataSet build() {
        return new NeuronNetworkTrainSetInMemoryImpl(Collections.unmodifiableList(trainInput));
    }

    @Override
    public NeuronNetworkDataSet build(InputStream inputStream) {
        throw new UnsupportedOperationException("Method has not been supported yet.");
    }
}
