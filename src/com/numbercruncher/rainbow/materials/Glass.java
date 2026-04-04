package com.numbercruncher.rainbow.materials;

import com.numbercruncher.rainbow.*;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.ray_tools.RaySpectral;

/**
 * Dielectric (glass) material with wavelength-dependent index of refraction.
 *
 * Uses Cauchy's equation for dispersion:  n(λ) = B + C / λ²
 * where λ is in micrometers.
 *
 * Default values approximate BK7 crown glass:
 *   B = 1.5046, C = 0.00420 μm²
 *   n(380nm) ≈ 1.534,  n(550nm) ≈ 1.519,  n(780nm) ≈ 1.512
 *
 * Implements:
 *  - Snell's law for refraction
 *  - Total internal reflection
 *  - Exact Fresnel reflectance (unpolarized)
 */
public class Glass extends Material {

    // Minimum probability for sampling the reflection path (importance sampling).
    // Boosts internal reflections so rainbow caustics are visible in backward path tracing.
    // The throughput is adjusted to keep the estimator unbiased.
    private static final double MIN_REFLECT_SAMPLE = 0.25;

    private final double cauchyB;
    private final double cauchyC; // in μm²
    private final double transparency; // 1.0 = perfectly clear, 0.0 = fully opaque

    /**
     * Default BK7-like glass, perfectly clear.
     */
    public Glass() {
        this(1.5046, 0.00420, 1.0);
    }

    /**
     * @param cauchyB base refractive index
     * @param cauchyC dispersion coefficient (μm²)
     */
    public Glass(double cauchyB, double cauchyC) {
        this(cauchyB, cauchyC, 1.0);
    }

    /**
     * @param cauchyB      base refractive index
     * @param cauchyC      dispersion coefficient (μm²)
     * @param transparency 1.0 = perfectly clear, 0.9 = slight tint, etc.
     */
    public Glass(double cauchyB, double cauchyC, double transparency) {
        super(Color.WHITE);
        this.cauchyB = cauchyB;
        this.cauchyC = cauchyC;
        this.transparency = transparency;
    }

    /**
     * Index of refraction at a given wavelength via Cauchy's equation.
     * @param lambdaNm wavelength in nanometers
     */
    public double indexOfRefraction(double lambdaNm) {
        double lambdaUm = lambdaNm / 1000.0;
        return cauchyB + cauchyC / (lambdaUm * lambdaUm);
    }

    /**
     * Exact Fresnel reflectance for unpolarized light.
     *
     * @param cosI  cosine of the angle of incidence (always positive)
     * @param n1    refractive index on the incident side
     * @param n2    refractive index on the transmitted side
     * @return reflectance in [0, 1]
     */
    private static double fresnel(double cosI, double n1, double n2) {
        double sinI = Math.sqrt(1.0 - cosI * cosI);
        double sinT = (n1 / n2) * sinI;
        if (sinT >= 1.0) return 1.0; // total internal reflection
        double cosT = Math.sqrt(1.0 - sinT * sinT);

        double rs = (n1 * cosI - n2 * cosT) / (n1 * cosI + n2 * cosT);
        double rp = (n2 * cosI - n1 * cosT) / (n2 * cosI + n1 * cosT);
        return 0.5 * (rs * rs + rp * rp);
    }

    /**
     * Compute refracted direction via Snell's law.
     * Returns null if total internal reflection occurs.
     *
     * @param incident   normalized incident direction (pointing into surface)
     * @param normal     outward surface normal
     * @param eta        ratio n1/n2
     */
    private static Vector refract(Vector incident, Vector normal, double eta) {
        double cosI = -incident.dot(normal);
        double sin2T = eta * eta * (1.0 - cosI * cosI);
        if (sin2T > 1.0) return null; // total internal reflection
        double cosT = Math.sqrt(1.0 - sin2T);
        return incident.scale(eta).add(normal.scale(eta * cosI - cosT));
    }

    private static Vector reflect(Vector incident, Vector normal) {
        return incident.sub(normal.scale(2.0 * incident.dot(normal)));
    }

    @Override
    public RaySpectral scatter(Ray incident, HitRecord hitRecord, double lambda, Scene scene) {
        double n = indexOfRefraction(lambda);

        Vector dir = incident.getDirection();
        Vector normal = hitRecord.normal;

        // Determine if we're entering or exiting the glass
        double cosI = -dir.dot(normal);
        double n1, n2;
        Vector outwardNormal;
        if (cosI > 0) {
            // Entering glass: air (1.0) -> glass (n)
            n1 = 1.0;
            n2 = n;
            outwardNormal = normal;
        } else {
            // Exiting glass: glass (n) -> air (1.0)
            n1 = n;
            n2 = 1.0;
            outwardNormal = normal.neg();
            cosI = -cosI;
        }

        double eta = n1 / n2;
        Vector refracted = refract(dir, outwardNormal, eta);

        Vector scatterDir;
        double throughput = transparency;
        if (refracted == null) {
            // Total internal reflection
            scatterDir = reflect(dir, outwardNormal);
        } else {
            // Importance sampling: boost reflection probability to capture
            // rainbow caustics while keeping the estimator unbiased.
            // Without this, only ~2% of rays reflect inside water drops,
            // making rainbows invisible in backward path tracing.
            double reflectProb = fresnel(cosI, n1, n2);
            double sampleProb = Math.max(reflectProb, MIN_REFLECT_SAMPLE);
            if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < sampleProb) {
                scatterDir = reflect(dir, outwardNormal);
                throughput *= reflectProb / sampleProb;
            } else {
                scatterDir = refracted;
                throughput *= (1.0 - reflectProb) / (1.0 - sampleProb);
            }
        }

        return new RaySpectral(hitRecord.point, scatterDir, lambda, throughput);
    }
}
