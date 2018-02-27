package com.cherkovskiy.neuron_networks.api;

import java.io.Serializable;

public interface ActivationFunction extends Serializable {

    double activate(double netInput);

    double derivative(double netInput);

    double getRange();

    String getCanonicalName();
}
