package com.cherkovskiy.neuron_networks.core.activationFunctions;

import com.cherkovskiy.neuron_networks.api.ActivationFunction;

public class HyperbolicTangAnguita implements ActivationFunction {
    public static final String NAME = "HYPERBOLIC_TANGENS_ANGUITA";

    @Override
    public double activate(double netInput) {
        if (netInput > 0) {
            if (netInput <= 1.92033) {
                return 0.96016 - 0.26037 * (netInput - 1.92033) * (netInput - 1.92033);
                // c-a*(x-b)^2
            } else {
                return 0.96016;
            }
        } else {
            if (netInput >= -1.92033) {
                return 0.26037 * (netInput + 1.92033) * (netInput + 1.92033) - 0.96016;
                // a*(x+b)^2-c
            } else {
                return -0.96016;
            }
        }
    }

    @Override
    public double derivative(double netInput) {
        if (netInput > 0) {
            if (netInput <= 1.92033) {
                return -2 * 0.26037 * netInput + 2 * 0.26037 * 1.92033;
                // -a2x+a2b
            } else {
                return 0.0781;
            }
        } else {
            if (netInput >= -1.92033) {
                return 2 * 0.26037 * netInput + 2 * 0.26037 * 1.92033;
                // 2ax+2ab
            } else {
                return 0.0781;
            }
        }
    }

    @Override
    public double getRange() {
        return 2*0.96016;
    }

    @Override
    public String getCanonicalName() {
        return NAME;
    }

}
