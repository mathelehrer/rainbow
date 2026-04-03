package com.numbercruncher.rainbow.sky;

import com.numbercruncher.rainbow.Color;
import com.numbercruncher.rainbow.ray_tools.Radiance;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.Vector;

/**
 * A mostly black sky with a small, bright sun disk.
 * Designed for scenes where you want a focused beam of light
 * (like the Pink Floyd prism setup).
 */
public class SkyDark extends Sky {

    private final Vector sunDirection;
    private final double sunAngularRadius;
    private final double sunIntensity;
    private final double sunTemperature;

    /**
     * @param sunDirection   unit vector pointing toward the sun
     * @param angularRadius  angular radius in degrees
     * @param intensity      brightness multiplier
     * @param temperature    blackbody temperature in Kelvin
     */
    public SkyDark(Vector sunDirection, double angularRadius, double intensity, double temperature) {
        this.sunDirection = sunDirection.normalize();
        this.sunAngularRadius = Math.toRadians(angularRadius);
        this.sunIntensity = intensity;
        this.sunTemperature = temperature;
    }

    /**
     * Planck blackbody, normalized so peak ≈ 1.
     */
    private static double planck(double lambdaNm, double T) {
        double lambdaM = lambdaNm * 1e-9;
        double c1 = 3.7418e-16;
        double c2 = 1.4388e-2;
        double radiance = c1 / (Math.pow(lambdaM, 5) * (Math.exp(c2 / (lambdaM * T)) - 1.0));
        return radiance / 2.634e13;
    }

    public Radiance getSpectralRadiance(Ray ray, double lambda) {
        double cosAngle = ray.getDirection().dot(sunDirection);
        double angle = Math.acos(Math.min(1.0, Math.max(-1.0, cosAngle)));

        if (angle < sunAngularRadius) {
            return new Radiance(sunIntensity * planck(lambda, sunTemperature));
        }
        // Everything else is black
        return Radiance.ZERO;
    }

    @Override
    public Color getColor(Ray ray) {
        double cosAngle = ray.getDirection().dot(sunDirection);
        double angle = Math.acos(Math.min(1.0, Math.max(-1.0, cosAngle)));
        if (angle < sunAngularRadius) {
            return new Color(1.0, 0.95, 0.8);
        }
        return Color.BLACK;
    }
}
