package com.cherkovskiy.neuron_networks.core.activationFunctions;

import com.cherkovskiy.neuron_networks.api.ActivationFunction;

public class Sigmoid implements ActivationFunction {

    public static final String NAME = "SIGMOID";

    @Override
    public double activate(double netInput) {
        return 1. / (1. + Math.exp(-netInput));
    }

    @Override
    public double derivative(double netInput) {
        final double activated = activate(netInput);
        return activated * (1 - activated);
    }

    @Override
    public double getRange() {
        return 1.;
    }

    @Override
    public String getCanonicalName() {
        return NAME;
    }

}
