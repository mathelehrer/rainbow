package com.numbercruncher.rainbow;

public class Utils {

    public static final double EPS = 1e-8;
    public static final double PI = Math.PI;
    public static final double TAU = 2 * PI;

    /**
     * Planck's law: spectral radiance of a blackbody at temperature T.
     * Returns relative intensity normalized so the peak at ~500nm for 5800K is approximately 1.
     *
     * @param lambdaNm wavelength in nanometers
     * @param T        temperature in Kelvin
     */
    public static double planck(double lambdaNm, double T) {
        double lambdaM = lambdaNm * 1e-9;
        double c1 = 3.7418e-16;  // 2 * pi * h * c^2
        double c2 = 1.4388e-2;   // h * c / k_B
        double radiance = c1 / (Math.pow(lambdaM, 5) * (Math.exp(c2 / (lambdaM * T)) - 1.0));
        return radiance / 2.634e13;
    }
}
