package com.cherkovskiy.neuron_networks.utils;


/**
 * When the network eventually begins to
 memorize the samples, the shape of the
 learning curve can provide an indication:
 If the learning curve of the verification
 samples is suddenly and rapidly rising
 while the learning curve of the verification
 data is continuously falling, this could indicate
 memorizing and a generalization getting
 poorer and poorer. At this point it
 could be decided whether the network has
 already learned well enough at the next
 point of the two curves, and maybe the
 final point of learning is to be applied
 here (this procedure is called early stopping).

 */


///TODO: some problems with local minimum and quasi-standstill with small gradient
public class SimpleNNGradientDescentViaFormulaAndBias {
    /**
     * -- in1 -->()\
     *              k1
     *                \
     *                 ------------()--- out -->
     *                /  /
     *               k2 /
     * -- in2 -->()/   /
     *                k3
     *         (bias)/
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
//                {1, 1, 0}, - impossible because NN architecture isn`t appropriate
        };
        double k1Cur = -1;
        double k2Cur = -1;
        double k3Cur = -1;
        double gamma = 100;
        double precision = 0.0000001;
        double stepForK1 = 5;
        double stepForK2 = 5;
        double stepForK3 = 5;
        double prevError = 1001;
        double curError = 1000;
        long cycleNumber = 0;

        for (double[] train : trainSet) {
            double nnResult = calcSigmoid(k1Cur, k2Cur, k3Cur, train[0], train[1]);
            System.out.println("Result: " + train[0] + " || " + train[1] + " => " + nnResult);
        }

        //while ( (stepForK1 > precision || stepForK2 > precision || stepForK3 > precision) && prevError > curError) {
        while ( (stepForK1 > precision || stepForK2 > precision || stepForK3 > precision)) {
            double deltaK1 = -gamma * dfForK1(trainSet, k1Cur, k2Cur, k3Cur);
            double deltaK2 = -gamma * dfForK2(trainSet, k1Cur, k2Cur, k3Cur);
            double deltaK3 = -gamma * dfForK3(trainSet, k1Cur, k2Cur, k3Cur);
            k1Cur += deltaK1;
            k2Cur += deltaK2;
            k3Cur += deltaK3;
            stepForK1 = Math.abs(deltaK1);
            stepForK2 = Math.abs(deltaK2);
            stepForK3 = Math.abs(deltaK3);
            System.out.println("--------------------------------");
            System.out.println("delta k1: " + deltaK1 + " delta k2: " + deltaK2 + " delta k3: " + deltaK3);
            System.out.println("k1: " + k1Cur + " k2: " + k2Cur + " k3: " + k3Cur);
            System.out.println("--------------------------------");

            for (double[] train : trainSet) {
                double nnResult = calcSigmoid(k1Cur, k2Cur, k3Cur, train[0], train[1]);
                System.out.println("Result: " + train[0] + " || " + train[1] + " => " + nnResult);
            }

            prevError = curError;
            curError = calcError(trainSet, k1Cur, k2Cur, k3Cur);
            System.out.println("Error: " + curError);
            System.out.println("============================= END of " + cycleNumber++ + " cycle =======================================");
        }

    }

    private static double calcError(double[][] trainSet, double k1Cur, double k2Cur, double k3Cur) {
        double result = 0;
        for (double[] train : trainSet) {
            double sigmoid = calcSigmoid(k1Cur, k2Cur, k3Cur, train[0], train[1]);
            result += Math.pow((train[2] - sigmoid), 2);
        }
        return result/trainSet.length;
    }

    private static double dfForK1(double[][] trainSet, double k1Cur, double k2Cur, double k3Cur) {
        double result = 0;
        for (double[] train : trainSet) {
            double sigmoid = calcSigmoid(k1Cur, k2Cur, k3Cur, train[0], train[1]);
            result += (train[2] - sigmoid) * sigmoid * (1 - sigmoid) * train[0];
        }
        return -result * (2.d /trainSet.length);
    }

    private static double dfForK2(double[][] trainSet, double k1Cur, double k2Cur, double k3Cur ) {
        double result = 0;
        for (double[] train : trainSet) {
            double sigmoid = calcSigmoid(k1Cur, k2Cur, k3Cur, train[0], train[1]);
            result += (train[2] - sigmoid) * sigmoid * (1 - sigmoid) * train[1];
        }
        return -result * (2.d /trainSet.length);
    }

    private static double dfForK3(double[][] trainSet, double k1Cur, double k2Cur, double k3Cur) {
        double result = 0;
        for (double[] train : trainSet) {
            double sigmoid = calcSigmoid(k1Cur, k2Cur, k3Cur, train[0], train[1]);
            result += (train[2] - sigmoid) * sigmoid * (1 - sigmoid);
        }
        return -result * (2.d /trainSet.length);
    }

    private static double calcSigmoid(double k1Cur, double k2Cur, double k3Cur, double input1, double input2) {
        return 1 / (1 + Math.exp(-(k1Cur * input1 + k2Cur * input2 + k3Cur)));
    }
}
