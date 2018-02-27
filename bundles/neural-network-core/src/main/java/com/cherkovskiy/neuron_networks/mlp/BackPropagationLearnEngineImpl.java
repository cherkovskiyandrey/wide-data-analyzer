

//TODO

//package com.cherkovskiy.neuron_networks.mlp;
//
//import com.cherkovskiy.neuron_networks.api.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayDeque;
//import java.util.List;
//import java.util.Optional;
//
//public class BackPropagationLearnEngineImpl implements BackPropagationLearnEngine {
//    private static final Logger COMMON_LOGGER = LoggerFactory.getLogger("CommonLogger");
//    private static final Logger ERROR_FUNCTION_LOGGER = LoggerFactory.getLogger("ErrorFunctionLogger");
//
//    private final FeedforwardNeuronNetworkImpl nn;
//    private final double learnRate;
//    private final Optional<StatisticsProvider> statisticsProvider;
//    private final double weightDecay;
//    private DebugLevels debugLevel = DebugLevels.ERROR;
//    private int logErrorFunction = -1;
//
//    BackPropagationLearnEngineImpl(FeedforwardNeuronNetworkImpl neuronNetworkDomain, double learningRate, double weightDecay, StatisticsProvider statisticsProvider) {
//        this.nn = neuronNetworkDomain;
//        this.statisticsProvider = Optional.ofNullable(statisticsProvider);
//        this.learnRate = learningRate;
//        this.weightDecay = weightDecay;
//
//        COMMON_LOGGER.debug(this.toString());
//    }
//
//    @Override
//    public void learnBackProp(NeuronNetworkDataSet neuronNetworkTrainSet) {
//        NeuronNetworkCoreHelper.checkCompatible(neuronNetworkTrainSet, nn);
//
//        double currentMinEuclidError = Double.MAX_VALUE;
//        long epochNumber = 0;
//        boolean wasDecent = true;
//        System.out.println("decent");
//
//        //TODO: online learning may be is:
////        final double error = 0;
////        do {
////            final NeuronNetworkDataSet.TrainSet trainSet = neuronNetworkTrainSet.getNextRandomTrainSet();
////            error = doLearnEachPattern(trainSet, epochNumber); //TODO: epochNumber - теперь нет такого понятия видимо
////        } while (isEnoughToLearn(error, epochNumber)); //TODO: all factors
//
////        //TODO: or offline learning with minibatch
////        final double error = 0;
////        do {
////            final List<NeuronNetworkDataSet.TrainSet> batch = neuronNetworkTrainSet.getNextRandomTrainBatch(1024);
////            error = doLearnBatch(batch, epochNumber);
////        } while (isEnoughToLearn(error, epochNumber)); //TODO: all factors
//
//        while (true) {
//            double fullEuclidError = 0d;
//
//            statisticsProvider.ifPresent(p -> p.topologySnapshort(nn));
//            for (NeuronNetworkDataSet.TrainSet trainSet : neuronNetworkTrainSet) {
//                fullEuclidError += doLearnEachPattern(trainSet, epochNumber);
//            }
//            fullEuclidError /= 2;
//
//            if (fullEuclidError < currentMinEuclidError) {
//                currentMinEuclidError = fullEuclidError;
//                if (debugLevel.isLessThanOrEqualTo(DebugLevels.INFO)) {
//                    COMMON_LOGGER.debug("New minimum has been reached: " + currentMinEuclidError);
//                }
//                System.out.println(epochNumber + ": New minimum has been reached: " + currentMinEuclidError);
//            }
//
//            if (debugLevel.doOut(epochNumber)) {
//                COMMON_LOGGER.debug(this.toString());
//            }
//
//            if (debugLevel.isLessThanOrEqualTo(DebugLevels.INFO) && logErrorFunction != -1 && (epochNumber % logErrorFunction) == 0) {
//                //TODO: to xls http://poi.apache.org/spreadsheet/quick-guide.html#NewWorkbook
//                ERROR_FUNCTION_LOGGER.info(epochNumber + ";" + fullEuclidError);
//            }
//
//            final long finalEpochNumber = epochNumber;
//            statisticsProvider.ifPresent(p -> p.buildRatesDependencies(nn, neuronNetworkTrainSet, finalEpochNumber));
//
//            if (debugLevel.isLessThanOrEqualTo(DebugLevels.INFO)) {
//                boolean isDecent = fullEuclidError <= currentMinEuclidError;
//                String message = "| Epoch: " + epochNumber +
//                        "; current error: " + fullEuclidError +
//                        "; min error: " + currentMinEuclidError +
//                        "; TREND: " + (isDecent ? "decent" : "!!!ascent!!!");
//                COMMON_LOGGER.debug(message);
//
//                if (wasDecent != isDecent) {
//                    wasDecent = isDecent;
//                    System.out.println(isDecent ? "decent" : "!!!ascent!!!");
//                }
//            }
//
//            epochNumber++;
//        }
//
////        if (debugLevel.isLessThanOrEqualTo(DebugLevels.OFF)) {
////            for (NeuronNetworkDataSet.TrainSet trainSet : neuronNetworkTrainSet) {
////                final NeuronNetworkOutput neuronNetworkOutput = process(trainSet.getInput());
////                logAllNets(neuronNetworkOutput);
////            }
////            COMMON_LOGGER.debug(this.toString());
////        }
//    }
//
//
//    //todo: move to BackPropagationLearnEngineBuilderImpl
////    @Override
////    public void setDebugMode(DebugLevels debugLevel) {
////        this.debugLevel = debugLevel;
////    }
////
////    @Override
////    public void logErrorFunction(int everyCycles) {
////        this.logErrorFunction = everyCycles;
////    }
//
//    @Override
//    public BackPropagationLearnResult onlineLearn(NeuronNetwork neuronNetwork, NeuronNetworkDataSet neuronNetworkTrainSet) {
//        //todo
//    }
//
//    @Override
//    public BackPropagationLearnResult estimate(NeuronNetwork neuronNetworkFromFile, NeuronNetworkDataSet neuronNetworkTrainSet) {
//        //todo
//    }
//
//    //without recursion
//    private Double doLearnEachPattern(NeuronNetworkDataSet.TrainSet trainSet, long epochNumber) {
//        final NeuronNetworkOutput neuronNetworkOutput = process(trainSet.getInput());
//
//        final List<Double> currentOutput = neuronNetworkOutput.getOutput();
//        final List<Double> teachOutput = trainSet.getOutput().getOutput();
//
//        if (debugLevel.isLessThanOrEqualTo(DebugLevels.INFO)) {
//            logCurrentOutput(trainSet.getInput(), neuronNetworkOutput);
//            if (debugLevel.isLessThanOrEqualTo(DebugLevels.DEBUG) || debugLevel.doOut(epochNumber)) {
//                logAllNets(neuronNetworkOutput);
//            }
//        }
//
//        final ArrayDeque<Double> deltaRates = NeuronNetworkCoreHelper.quickBpLearnPattern(teachOutput, neuronNetworkOutput, learnRate, weightDecay, nn);
//        NeuronNetworkCoreHelper.applyDeltaRates(deltaRates, nn);
//
//        if (debugLevel.isLessThanOrEqualTo(DebugLevels.TRACE)) {
//            COMMON_LOGGER.debug(this.toString());
//        }
//
//        return Double.isNaN(weightDecay) ? NeuronNetworkCoreHelper.calcEuclideError(teachOutput, currentOutput) :
//                NeuronNetworkCoreHelper.calcEuclideError(teachOutput, currentOutput, nn.getTopology(), weightDecay);
//    }
//
//
//    private void logCurrentOutput(NeuronNetworkInput input, NeuronNetworkOutput output) {
//        final StringBuilder inputStr = new StringBuilder("Input: ");
//        for (double inputVal : input.getInput()) {
//            inputStr.append(String.format("|%14.10f|", inputVal));
//        }
//        inputStr.append(" => ");
//        for (double outputVal : output.getOutput()) {
//            inputStr.append(String.format("|%14.10f|", outputVal));
//        }
//        COMMON_LOGGER.debug(inputStr.toString());
//    }
//
//    private void logAllNets(NeuronNetworkOutput neuronNetworkOutput) {
//        final StringBuilder outStr = new StringBuilder("Nets: ");
//        for (double outputVal : neuronNetworkOutput.getInputsAllNeurons()) {
//            outStr.append(String.format("|%10.5f|", outputVal));
//        }
//
//        COMMON_LOGGER.debug(outStr.toString());
//    }
//
//
//}
