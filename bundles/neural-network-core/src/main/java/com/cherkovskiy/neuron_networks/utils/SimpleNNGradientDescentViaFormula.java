package com.cherkovskiy.neuron_networks.utils;

public class SimpleNNGradientDescentViaFormula {

    /**
     * -- in1 -->()\
     *              k1
     *                \
     *                  ---()--- out -->
     *                /
     *               k2
     * -- in2 -->()/
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        double[][] trainSet = {
                {0, 0, 0},
                {0, 1, 1},
                {1, 0, 1},
                {1, 1, 1},
        };
        double k1Cur = 1;
        double k2Cur = 1;
        double gamma = 100;
        double precision = 0.0000001;
        double stepForK1 = 5;
        double stepForK2 = 5;
        long cycleNumber = 0;

        for (double[] train : trainSet) {
            double nnResult = calcSigmoid(k1Cur, k2Cur, train[0], train[1]);
            System.out.println("Result: " + train[0] + " || " + train[1] + " => " + nnResult);
        }

        while (stepForK1 > precision || stepForK2 > precision) {
            double deltaK1 = -gamma * dfForK1(trainSet, k1Cur, k2Cur);
            double deltaK2 = -gamma * dfForK2(trainSet, k1Cur, k2Cur);
            k1Cur += deltaK1;
            k2Cur += deltaK2;
            stepForK1 = Math.abs(deltaK1);
            stepForK2 = Math.abs(deltaK2);
            System.out.println("--------------------------------");
            System.out.println("delta k1: " + deltaK1 + " delta k2: " + deltaK2);
            System.out.println("k1: " + k1Cur + " k2: " + k2Cur);
            System.out.println("--------------------------------");

            for (double[] train : trainSet) {
                double nnResult = calcSigmoid(k1Cur, k2Cur, train[0], train[1]);
                System.out.println("Result: " + train[0] + " || " + train[1] + " => " + nnResult);
            }
            System.out.println("Error: " + calcError(trainSet, k1Cur, k2Cur));
            System.out.println("============================= END of " + cycleNumber++ + " cycle =======================================");
        }

    }

    private static double calcError(double[][] trainSet, double k1Cur, double k2Cur) {
        double result = 0;
        for (double[] train : trainSet) {
            double sigmoid = calcSigmoid(k1Cur, k2Cur, train[0], train[1]);
            result += Math.pow((train[2] - sigmoid), 2);
        }
        return result/trainSet.length;
    }

    private static double dfForK1(double[][] trainSet, double k1Cur, double k2Cur) {
        double result = 0;
        for (double[] train : trainSet) {
            double sigmoid = calcSigmoid(k1Cur, k2Cur, train[0], train[1]);
            result += (train[2] - sigmoid) * sigmoid * (1 - sigmoid) * train[0];
        }
        return -result * (2.d /trainSet.length);
    }

    private static double dfForK2(double[][] trainSet, double k1Cur, double k2Cur) {
        double result = 0;
        for (double[] train : trainSet) {
            double sigmoid = calcSigmoid(k1Cur, k2Cur, train[0], train[1]);
            result += (train[2] - sigmoid) * sigmoid * (1 - sigmoid) * train[1];
        }
        return -result * (2.d /trainSet.length);
    }

    private static double calcSigmoid(double k1Cur, double k2Cur, double input1, double input2) {
        return 1 / (1 + Math.exp(-(k1Cur * input1 + k2Cur * input2)));
    }
}
