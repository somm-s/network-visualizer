package ch.cydcampus.hickup.model;

import com.tambapps.fft4j.FastFouriers;
import com.tambapps.fft4j.Signal;
import com.tambapps.fft4j.algorithm.BasicFastFourier;

/*
 * Combination rule that estimates the threshold per flow connection using Fourier Transformation.
 * Based on ideas from: https://www.utwente.nl/en/eemcs/dacs/assignments/completed/bachelor/reports/B-assignment_Grondman.pdf
 */
public class FourierCombinationRule {

    BasicFastFourier fastFourier = new BasicFastFourier();
    Signal signal = new Signal(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

    public FourierCombinationRule() {
        Signal res = FastFouriers.BASIC.transform(signal);
        System.out.println(res);
    }

    public static void main(String[] args) {
        new FourierCombinationRule();
    }

    /*
    * Estimates the parameters for the Combination Rules that are used to combine the tokens.
    * Uses the same data stream that flows through the AbstractionModule.
    * Works asynchronously in the background.
    */
    private class ParameterEstimator {
        
    }
}


