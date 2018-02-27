package com.cherkovskiy.neuron_networks.mlp;

import com.cherkovskiy.neuron_networks.api.NeuronNetworkOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NeuronNetworkOutputImpl implements NeuronNetworkOutput {
    private final List<Double> inputVector;
    private final int inputAmount;
    private final List<Double> outputVector;
    private final int outputAmount;

    NeuronNetworkOutputImpl(List<Double> inputVector, int inputAmount, List<Double> outputVector, int outputAmount) {
        this.inputVector = new ArrayList<>(inputVector);
        this.inputAmount = inputAmount;
        this.outputVector = new ArrayList<>(outputVector);
        this.outputAmount = outputAmount;
    }

    NeuronNetworkOutputImpl(List<Double> outputVector, int outputAmount) {
        this.inputVector = Collections.emptyList();
        this.inputAmount = 0;
        this.outputVector = new ArrayList<>(outputVector);
        this.outputAmount = outputAmount;
    }

    @Override
    public List<Double> getOutput() {
        return outputVector.stream().skip(outputVector.size() - outputAmount).collect(Collectors.toList());
    }

    @Override
    public List<Double> getOutputAllNeurons() {
        return outputVector;
    }

    @Override
    public List<Double> getInputs() {
        return inputVector.stream().limit(inputAmount).collect(Collectors.toList());
    }

    @Override
    public List<Double> getInputsAllNeurons() {
        return inputVector;
    }
}
