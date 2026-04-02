package com.numbercruncher.rainbow;

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
 *  - Schlick's approximation for Fresnel reflectance
 */
public class MaterialGlass extends Material {

    private final double cauchyB;
    private final double cauchyC; // in μm²

    /**
     * Default BK7-like glass.
     */
    public MaterialGlass() {
        this(1.5046, 0.00420);
    }

    /**
     * @param cauchyB base refractive index
     * @param cauchyC dispersion coefficient (μm²)
     */
    public MaterialGlass(double cauchyB, double cauchyC) {
        super(Color.WHITE);
        this.cauchyB = cauchyB;
        this.cauchyC = cauchyC;
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
     * Schlick's approximation for Fresnel reflectance.
     */
    private static double schlick(double cosTheta, double n1, double n2) {
        double r0 = (n1 - n2) / (n1 + n2);
        r0 = r0 * r0;
        double x = 1.0 - cosTheta;
        return r0 + (1.0 - r0) * x * x * x * x * x;
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
    public RayWithAttenuation scatter(Ray incident, HitRecord hitRecord) {
        // Fallback: use midpoint of visible spectrum (~550nm)
        return scatter(incident, hitRecord, 550.0);
    }

    @Override
    public RayWithAttenuation scatter(Ray incident, HitRecord hitRecord, double lambda) {
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
        if (refracted == null) {
            // Total internal reflection
            scatterDir = reflect(dir, outwardNormal);
        } else {
            // Probabilistic reflection vs refraction (Fresnel)
            double reflectProb = schlick(cosI, n1, n2);
            if (Math.random() < reflectProb) {
                scatterDir = reflect(dir, outwardNormal);
            } else {
                scatterDir = refracted;
            }
        }

        // Glass is transparent — attenuation is white (no color absorption)
        return new RayWithAttenuation(hitRecord.point, scatterDir, Color.WHITE);
    }

    @Override
    public RayWithAttenuation transmitted(Ray incident) {
        return null;
    }
}
