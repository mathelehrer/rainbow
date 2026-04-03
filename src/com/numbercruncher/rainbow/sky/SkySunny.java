package com.numbercruncher.rainbow.sky;

import com.numbercruncher.rainbow.Color;
import com.numbercruncher.rainbow.ray_tools.Radiance;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.Vector;

/**
 * A physically-motivated sunny sky for spectral rendering.
 *
 * - Sun disk: emits a Planck blackbody spectrum at ~5800K
 * - Sky dome: Rayleigh-like scattering (intensity ~ 1/lambda^4),
 *   brighter near the horizon, blended with the sun's spectrum
 *
 * The sun direction is specified via altitude and azimuth angles.
 */
public class SkySunny extends Sky {
    public static final double SUN_ANGLE = 1.0; //0.27

    private final Vector sunDirection; // unit vector toward the sun
    private final double sunAngularRadius; // radians (sun is ~0.27° = 0.0047 rad)
    private final double sunTemperature; // Kelvin
    private final double sunIntensity; // multiplier for sun disk
    private final double skyIntensity; // multiplier for sky dome

    /**
     * Default: sun at 45° altitude, azimuth 0 (in front of camera along +y),
     * temperature 5800K, realistic angular size.
     */
    public SkySunny() {
        this(Math.toRadians(45), 0.0, 5800.0, 5.0, 0.6);
    }

    /**
     * @param altitude    sun altitude in radians above horizon (0 = horizon, pi/2 = zenith)
     * @param azimuth     sun azimuth in radians (0 = +y direction, positive = toward +x)
     * @param temperature sun color temperature in Kelvin
     * @param sunIntensity  brightness multiplier for the sun disk
     * @param skyIntensity  brightness multiplier for the sky dome
     */
    public SkySunny(double altitude, double azimuth, double temperature,
                    double sunIntensity, double skyIntensity) {
        // Convert altitude/azimuth to a direction vector
        // camera looks along +y, up is +z
        double cosAlt = Math.cos(altitude);
        this.sunDirection = new Vector(
                cosAlt * Math.sin(azimuth),
                cosAlt * Math.cos(azimuth),
                Math.sin(altitude)
        ).normalize();
        this.sunAngularRadius = Math.toRadians(SUN_ANGLE);
        this.sunTemperature = temperature;
        this.sunIntensity = sunIntensity;
        this.skyIntensity = skyIntensity;
    }

    /**
     * Planck's law: spectral radiance of a blackbody at temperature T.
     * Returns relative intensity (not absolute SI units) normalized so the
     * peak at ~500nm for 5800K is approximately 1.
     *
     * @param lambda wavelength in nm
     * @param T temperature in Kelvin
     */
    private static double planck(double lambda, double T) {
        double lambdaM = lambda * 1e-9; // nm to meters
        double c1 = 3.7418e-16; // 2 * pi * h * c^2
        double c2 = 1.4388e-2;  // h * c / k_B
        double radiance = c1 / (Math.pow(lambdaM, 5) * (Math.exp(c2 / (lambdaM * T)) - 1.0));
        // Normalize: peak of 5800K blackbody is at ~500nm
        // radiance at 500nm, 5800K ≈ 2.634e13
        return radiance / 2.634e13;
    }

    /**
     * Spectral radiance for a given ray direction and wavelength.
     */
    public Radiance getSpectralRadiance(Ray ray, double lambda) {
        Vector dir = ray.getDirection();
        double cosAngle = dir.dot(sunDirection);
        double angle = Math.acos(Math.min(1.0, Math.max(-1.0, cosAngle)));

        double sun = 0.0;
        if (angle < sunAngularRadius) {
            // Inside sun disk: Planck spectrum
            sun = sunIntensity * planck(lambda, sunTemperature);
        }

        // Sky dome: Rayleigh scattering gives blue tint (1/lambda^4)
        // modulated by elevation (brighter near horizon for atmosphere thickness)
        double lambda0 = 550.0; // reference wavelength
        double rayleigh = Math.pow(lambda0 / lambda, 4.0);

        // Elevation factor: more scattering near horizon
        double elevation = dir.z; // z = up
        double horizonFactor = 1.0 - 0.5 * Math.max(0, elevation);

        // Also add a soft forward-scattering glow around the sun (Mie-like)
        double mieGlow = 0.0;
        if (cosAngle > 0) {
            mieGlow = 0.2 * Math.pow(cosAngle, 32) * planck(lambda, sunTemperature);
        }

        double sky = skyIntensity * rayleigh * horizonFactor * planck(lambda, sunTemperature);

        return new Radiance(sun + sky + mieGlow);
    }

    /**
     * RGB fallback for non-spectral rendering.
     */
    @Override
    public Color getColor(Ray ray) {
        Vector dir = ray.getDirection();
        double cosAngle = dir.dot(sunDirection);
        double angle = Math.acos(Math.min(1.0, Math.max(-1.0, cosAngle)));

        if (angle < sunAngularRadius) {
            return new Color(1.0, 0.95, 0.8); // warm white sun
        }

        // Simple blue sky gradient
        double t = 0.5 * (dir.z + 1.0);
        double r = 0.4 * (1.0 - t) + 0.2 * t;
        double g = 0.6 * (1.0 - t) + 0.4 * t;
        double b = 0.8 * (1.0 - t) + 1.0 * t;
        return new Color(r, g, b);
    }

    public Vector getSunDirection() {
        return sunDirection;
    }
}
