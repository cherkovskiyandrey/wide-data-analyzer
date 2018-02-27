package com.cherkovskiy.neuron_networks.core.activationFunctions;

import com.cherkovskiy.neuron_networks.api.ActivationFunction;
import com.cherkovskiy.neuron_networks.api.BasicActivationFunction;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum StandardActivationFunctions implements ActivationFunction {

    SIGMOID(BasicActivationFunction.SIGMOID, new Sigmoid()),
    HYPERBOLIC_TANG_ANGUITA(BasicActivationFunction.HYPERBOLIC_TANG_ANGUITA, new HyperbolicTangAnguita());

    private static final Map<String, ActivationFunction> FUNCTIONS_BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(ActivationFunction::getCanonicalName, Function.identity(), (l, r) -> l));

    private static final Map<BasicActivationFunction, ActivationFunction> FUNCTIONS_BY_API_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(StandardActivationFunctions::getBasicActivationFunction, o -> o, (l, r) -> l));

    private final BasicActivationFunction basicActivationFunction;
    private final ActivationFunction activationFunction;

    StandardActivationFunctions(BasicActivationFunction basicActivationFunction, ActivationFunction activationFunction) {
        this.basicActivationFunction = basicActivationFunction;
        this.activationFunction = activationFunction;
    }

    @Override
    public double activate(double netInput) {
        return activationFunction.activate(netInput);
    }

    @Override
    public double derivative(double netInput) {
        return activationFunction.derivative(netInput);
    }

    @Override
    public double getRange() {
        return activationFunction.getRange();
    }

    @Override
    public String getCanonicalName() {
        return activationFunction.getCanonicalName();
    }

    public BasicActivationFunction getBasicActivationFunction() {
        return basicActivationFunction;
    }

    public static boolean contains(String name) {
        return FUNCTIONS_BY_NAME.containsKey(name);
    }

    public static ActivationFunction getByName(String name) {
        return FUNCTIONS_BY_NAME.get(name);
    }

    public static ActivationFunction getByApiName(BasicActivationFunction basicActivationFunction) {
        return FUNCTIONS_BY_API_NAME.get(basicActivationFunction);
    }
}
