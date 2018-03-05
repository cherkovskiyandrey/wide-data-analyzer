package com.cherkovskiy.neuron_networks.mlp;

import com.cherkovskiy.application_context.ApplicationContextHolder;
import com.cherkovskiy.application_context.api.exceptions.ServiceNotFoundException;
import com.cherkovskiy.comprehensive_serializer.api.SerializerService;
import com.cherkovskiy.neuron_networks.api.ActivationFunction;
import com.cherkovskiy.neuron_networks.api.BasicActivationFunction;
import com.cherkovskiy.neuron_networks.api.NeuronNetwork;
import com.cherkovskiy.neuron_networks.api.NeuronNetworkBuilder;
import com.cherkovskiy.neuron_networks.core.activationFunctions.StandardActivationFunctions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FeedforwardNeuronNetworkBuilderImpl implements NeuronNetworkBuilder {
    private final SerializerService serializerService;
    private ActivationFunction activationFunction;
    private int input;
    private LinkedList<Integer> hiddenLevels = new LinkedList<>();
    private int output;

    public FeedforwardNeuronNetworkBuilderImpl() {
        try {
            this.serializerService = ApplicationContextHolder.currentContext().getService(SerializerService.class);
        } catch (ServiceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO: move to LearnBuilder
//    private boolean useStatModule;
//    private double range;
//    private double step;
//    private double weightDecay = Double.NaN;
//    private double learningRate = 0.01;  //according to 92 page: 0.01 ≤ η ≤ 0.9;

    @Override
    public NeuronNetworkBuilder inputsNeurons(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException(String.format("Amount of inputs neurons must be at least one: %d", amount));
        }
        this.input = amount;
        return this;
    }

    @Override
    public NeuronNetworkBuilder addHiddenLevel(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Level can`t be empty");
        }
        this.hiddenLevels.add(amount);
        return this;
    }

    @Override
    public NeuronNetworkBuilder outputNeurons(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException(String.format("Amount of outputs neurons must be at least one: %d", amount));
        }
        this.output = amount;
        return this;
    }

    @Override
    public NeuronNetworkBuilder useBias(boolean b) {
        return this;
    }

    @Override
    public NeuronNetworkBuilder setActivationFunction(BasicActivationFunction activationFunction) {
        this.activationFunction = StandardActivationFunctions.getByApiName(activationFunction);
        return this;
    }

    @Override
    public NeuronNetworkBuilder setActivationFunction(ActivationFunction activationFunction) {
        this.activationFunction = activationFunction;
        return this;
    }

    //TODO: move to LearnBuilder
//    @Override
//    public NeuronNetworkBuilder useStatModule(boolean b, double range, double step) {
//        this.useStatModule = b;
//        this.range = range;
//        this.step = step;
//        return this;
//    }
//
//    @Override
//    public NeuronNetworkBuilder learningRate(double learningRate) {
//        this.learningRate = learningRate;
//        return this;
//    }
//
//    @Override
//    public NeuronNetworkBuilder weightDecay(double b) {
//        this.weightDecay = b;
//        return this;
//    }

    public NeuronNetwork build() throws ServiceNotFoundException {
        int allNeurons = input + hiddenLevels.stream().mapToInt(Integer::intValue).sum() + output;
        final double[][] topology = NeuronNetworkCoreHelper.nanArray(allNeurons, allNeurons);
        int levelBegin = 0;
        int levelEnd = input;

        for (int levelAmount : Stream.concat(hiddenLevels.stream(), Stream.of(output)).collect(Collectors.toList())) {
            for (int levelNeuron = levelEnd; levelNeuron < levelEnd + levelAmount; levelNeuron++) {
                for (int prevLevelNeuron = levelBegin; prevLevelNeuron < levelEnd; prevLevelNeuron++) {
                    topology[levelNeuron][prevLevelNeuron] = getNextRandom();
                }
            }
            levelBegin = levelEnd;
            levelEnd = levelEnd + levelAmount;
        }

        return new FeedforwardNeuronNetworkImpl(input, topology, output, activationFunction);
    }

    @Override
    public NeuronNetwork build(InputStream from) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ServiceNotFoundException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(from, StandardCharsets.UTF_8));

        final String type = reader.readLine();
        if (!FeedforwardNeuronNetworkImpl.TYPE.equalsIgnoreCase(type)) {
            throw new ClassNotFoundException("Incompatible type of NN: " + type + ". Only " + FeedforwardNeuronNetworkImpl.TYPE + " can be deserialized.");
        }

        final String uuid = reader.readLine();
        if (!FeedforwardNeuronNetworkImpl.UUID.equalsIgnoreCase(uuid)) {
            throw new ClassNotFoundException("Incompatible of UUID. Supported: " + FeedforwardNeuronNetworkImpl.UUID + ". Read: " + uuid);
        }

        final String nameOfActFunc = reader.readLine();
        if (StandardActivationFunctions.contains(nameOfActFunc)) {
            this.activationFunction = StandardActivationFunctions.getByName(nameOfActFunc);
        } else {
            final String actFuncAsStr = reader.readLine();
            try (InputStream inputStream = Base64.getDecoder().wrap(new ByteArrayInputStream(actFuncAsStr.getBytes(StandardCharsets.UTF_8)))) {
                this.activationFunction = serializerService.deserializeFrom(inputStream, ActivationFunction.class);
            }
        }

        this.input = Integer.parseInt(reader.readLine());
        this.output = Integer.parseInt(reader.readLine());

        int topologySize = Integer.parseInt(reader.readLine());
        final double[][] topology = NeuronNetworkCoreHelper.nanArray(topologySize, topologySize);
        for (int i = 0; i < topologySize; i++) {
            final int levelIdx = i;
            final String level = reader.readLine();
            Arrays.stream(level.split(";"))
                    .filter(((Predicate<String>) String::isEmpty).negate())
                    .forEach(str -> {
                        final String[] pair = str.split(":");
                        topology[levelIdx][Integer.parseInt(pair[0])] = Double.parseDouble(pair[1]);
                    });
        }

        return new FeedforwardNeuronNetworkImpl(input, topology, output, activationFunction);
    }


    private static Double getNextRandom() {
        while (true) {
            double val = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
            if (Math.abs(val) > 1.E-5) {
                return val;
            }
        }
    }
}
