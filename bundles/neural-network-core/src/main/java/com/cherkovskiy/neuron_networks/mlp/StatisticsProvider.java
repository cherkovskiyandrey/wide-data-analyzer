package com.cherkovskiy.neuron_networks.mlp;


import com.cherkovskiy.neuron_networks.api.NeuronNetworkDataSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

@NotThreadSafe
class StatisticsProvider {
    private static final Logger STAT_LOGGER = LoggerFactory.getLogger("StatLogger");

    private final double delta;
    private final double range;
    private FeedforwardNeuronNetworkImpl begin;

    //TODO: to detect change direction!

    StatisticsProvider(double range, double delta) {
        this.delta = delta;
        this.range = range;
    }

    public void topologySnapshort(FeedforwardNeuronNetworkImpl nn) {
        begin = nn.newClone();
    }

    public void buildRatesDependencies(FeedforwardNeuronNetworkImpl topology, NeuronNetworkDataSet neuronNetworkTrainSet, long epochNumber) {
        NeuronNetworkCoreHelper.checkCompatible(begin, topology);

        STAT_LOGGER.info(String.format("============= STAT for %d =============", epochNumber));

        for (int i = 0; i < begin.getTopology().length; i++) {
            for (int j = 0; j < begin.getTopology()[i].length; j++) {
                final Double ratePrev = begin.getTopology()[i][j];
                final Double rateCur = topology.getTopology()[i][j];
                if (!ratePrev.isNaN()) {
                    double curDelta = rateCur - ratePrev;
                    double beginRange = ratePrev - (range / 2);
                    double endRange = ratePrev + (range / 2);

                    STAT_LOGGER.debug(String.format("%d,%d => rate prev: %24.20f; rate cur: %24.20f; delta: %24.20f", i, j, ratePrev, rateCur, curDelta));

                    if (!Range.between(beginRange, endRange).contains(rateCur)) {
                        STAT_LOGGER.warn(String.format("New name %24.20f is out of range: %24.20f <--> %24.20f", rateCur, beginRange, endRange));
                    }

                    //by scan range
                    final List<Pair<Double, Double>> errorDep = new ArrayList<>();
                    final FeedforwardNeuronNetworkImpl tmpTopology = begin.newClone();
                    final SortedSet<Double> points = Sets.newTreeSet();
                    for (double k = beginRange; k < endRange; k += delta) {
                        points.add(k);
                    }
                    points.add(rateCur);
                    points.add(ratePrev);

                    //todo
//                    for (Double k : points) {
//                        tmpTopology.getTopology()[i][j] = k;
//
//                        // calc full ERROR
//                        double error = 0;
//                        for (NeuronNetworkDataSet.TrainSet trainSet : neuronNetworkTrainSet) {
//                            final NeuronNetworkOutput neuronNetworkOutput = NeuronNetworkCoreHelper.process(trainSet.getInput(), tmpTopology);
//                            final List<Double> currentOutput = neuronNetworkOutput.getOutput();
//                            final List<Double> teachOutput = trainSet.getOutput().getOutput();
//                            error += NeuronNetworkCoreHelper.calcEuclideError(teachOutput, currentOutput);
//                        }
//                        error /= 2;
//                        errorDep.add(Pair.of(k, error));
//                    }

                    //TODO: обработчик неверного направления!

                    //TODO: сделать функцию Err = f(wi, wj) и посмотреть если зависимость смены знака wi в области значений wj и если есть - дать сигнал

                    toLog(i, j, errorDep);
                }
            }
        }
        STAT_LOGGER.info(String.format("============= END STAT for %d =============", epochNumber));
    }

    private void toLog(int i, int j, List<Pair<Double, Double>> errorDep) {
        StringBuilder str = new StringBuilder();
        errorDep.forEach(r -> {
            str.append(String.format("          %24.20f;%24.20f%s", r.getFirst(), r.getSecond(), System.lineSeparator()));
        });

        STAT_LOGGER.trace(String.format("%d,%d => %s%s", i, j, System.lineSeparator(), str.toString()));
    }
}

