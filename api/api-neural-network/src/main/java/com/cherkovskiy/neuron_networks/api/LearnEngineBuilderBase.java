package com.cherkovskiy.neuron_networks.api;

public interface LearnEngineBuilderBase<T extends LearnEngineBuilderBase> {

    /**
     * If error function does not change greater than maxErrorFluctuation in last epochAmount epoch, onlineLearn will stop.
     * It is a successful end of cycle.
     *
     * @param epochAmount
     * @param maxErrorFluctuation
     * @return
     */
    T setStopCondition(int epochAmount, double maxErrorFluctuation);

    /**
     * Usually it is protection from long cycles.
     *
     * @param epochAmount
     * @return
     */
    T setMaxEpochPerCycle(int epochAmount);

    /**
     * Criteria to successful stop learning current cycle.
     *
     * @param errorVal
     * @return
     */
    T setSuccessErrorValue(double errorVal);


    /**
     * Is it permitted to expand NN topology.
     *
     * @param isPermitted
     * @return
     */
    T expand(boolean isPermitted);

    /**
     * Use in extension topology.
     *
     * @param neurons
     * @return
     */
    T setMinNeuronsPerLevel(int neurons);

    /**
     * Use in extension topology.
     *
     * @param neurons
     * @return
     */
    T setMaxNeuronsPerLevel(int neurons);

    /**
     * Use in extension topology.
     *
     * @param levels
     * @return
     */
    T setMinLevels(int levels);

    /**
     * Use in extension topology.
     *
     * @param levels
     * @return
     */
    T setMaxLevels(int levels);

    T setDebugMode(DebugLevels debugLevel);

    T logErrorFunction(int everyCycles);

    /**
     * Just to investigation goals.
     * To process-intensive! Don`t use in release.
     *
     * @param on
     * @param range
     * @param step
     * @return
     */
    T useStatModule(boolean on, double range, double step);

}
