package com.cherkovskiy.neuron_networks.mlp;

import com.cherkovskiy.neuron_networks.api.BackPropagationLearnEngine;
import com.cherkovskiy.neuron_networks.api.BackPropagationLearnEngineBuilder;
import com.cherkovskiy.neuron_networks.api.DebugLevels;

public class BackPropagationLearnEngineBuilderImpl implements BackPropagationLearnEngineBuilder {
    private double learningRate = 0.01;       //according to 92 page: 0.01 ≤ η ≤ 0.9
    private double weightDecay = Double.NaN;  //turn off by default

    @Override
    public BackPropagationLearnEngineBuilder learningRate(double rate) {
        this.learningRate = learningRate;
        return this;
    }

    @Override
    public BackPropagationLearnEngineBuilder weightDecay(double decayFactor) {
        this.weightDecay = decayFactor;
        return this;
    }

    @Override
    public BackPropagationLearnEngineBuilder setStopCondition(int epochAmount, double maxErrorFluctuation) {
        return null;
    }

    @Override
    public BackPropagationLearnEngineBuilder setMaxEpochPerCycle(int epochAmount) {
        return null;
    }

    @Override
    public BackPropagationLearnEngineBuilder setSuccessErrorValue(double errorVal) {
        return null;
    }

    @Override
    public BackPropagationLearnEngineBuilder expand(boolean isPermitted) {
        return null;
    }

    @Override
    public BackPropagationLearnEngineBuilder setMinNeuronsPerLevel(int neurons) {
        return null;
    }

    @Override
    public BackPropagationLearnEngineBuilder setMaxNeuronsPerLevel(int neurons) {
        return null;
    }

    @Override
    public BackPropagationLearnEngineBuilder setMinLevels(int levels) {
        return null;
    }

    @Override
    public BackPropagationLearnEngineBuilder setMaxLevels(int levels) {
        return null;
    }

    @Override
    public BackPropagationLearnEngineBuilder setDebugMode(DebugLevels debugLevel) {
        return null;
    }

    @Override
    public BackPropagationLearnEngineBuilder logErrorFunction(int everyCycles) {
        return null;
    }

    @Override
    public BackPropagationLearnEngineBuilder useStatModule(boolean on, double range, double step) {
        return null;
    }


    @Override
    public BackPropagationLearnEngine build() {
        return null;
    }
}
