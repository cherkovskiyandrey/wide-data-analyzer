package com.cherkovskiy.neuron_networks.utils;

public class GradientDescentExample {


    //TODO: improve to find global min in all cases (https://en.wikipedia.org/wiki/Line_search https://en.wikipedia.org/wiki/Stochastic_gradient_descent)
    public static void main(String[] args) throws Exception {
        double cur_x = -3; // The algorithm starts at x=6
        double gamma = 0.01; // step size multiplier
        double precision = 0.00001;
        double previous_step_size = Math.abs(cur_x);

        System.out.println("Prev x   | Cur x   | Abs Delta  | F(X)");
        while (previous_step_size > precision) {
            double prev_x = cur_x;
            cur_x += -gamma * df(prev_x);
            previous_step_size = Math.abs(cur_x - prev_x);
            System.out.println(prev_x + " | " + cur_x + " | " + previous_step_size + " | " + f(cur_x));
        }

        System.out.println("The local min X = " + cur_x + " and Y = " + f(cur_x));
    }

    private static double f(double x) {
        return Math.pow(x, 4) - 3 * Math.pow(x, 3) + 2;
    }

    //Derivative for f(x)=x4−3x3+2
    private static double df(double x) {
        return 4 * Math.pow(x, 3) - 9 * Math.pow(x, 2);
    }


}
