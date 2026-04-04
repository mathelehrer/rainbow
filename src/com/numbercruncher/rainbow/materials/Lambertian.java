package com.numbercruncher.rainbow.materials;

import com.numbercruncher.rainbow.*;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.ray_tools.RaySpectral;
import com.numbercruncher.rainbow.sky.SkySunny;

import java.util.concurrent.ThreadLocalRandom;

import static com.numbercruncher.rainbow.Utils.EPS;

public class Lambertian extends Material {
    private final double albedo; //a measure for the reflectiveness of the material

    public Lambertian(Color color, double albedo, double emission){
        super(color, emission);
        this.albedo = albedo;
    }

    public Lambertian(Color color, double albedo){
        this(color, albedo, 0);
    }

    public Lambertian(Color color){
        this(color, 0.5, 0);
    }

    @Override
    public boolean usesDirectLightSampling() {
        return true;
    }


    @Override
    public RaySpectral scatter(Ray ray, HitRecord hitRecord, double lambda, Scene scene){
        // Flip normal to face the incoming ray (handles inside-of-sphere hits)
        Vector normal = hitRecord.normal;
        if (ray.getDirection().dot(normal) > 0) normal = normal.neg();

        Vector scatterDirection = Vector.lambertianReflection(normal);

        // Reflectance contribution (indirect light via recursive bounce)
        // cos(θ) is already accounted for by the cosine-weighted sampling in lambertianReflection()
        double reflectance = color.spectralReflectance(lambda) * albedo;

        // 2. Emission contribution (self-emitted radiance)
        double emittedRadiance = getEmission();

        // 3. Direct sun sampling (next event estimation)
        //    If the sky has a sun, sample a shadow ray toward the sun disk.
        //    If unoccluded, add the sun's direct spectral contribution weighted by BRDF.
        double directRadiance = 0;
        if (scene.getSky() instanceof SkySunny) {
            SkySunny sun = (SkySunny) scene.getSky();
            // Sample a random direction within the sun's cone
            Vector sunDir = sampleSunCone(sun.getSunDirection(), sun.getSunAngularRadius());
            double cosSun = sunDir.dot(hitRecord.normal);
            if (cosSun > 0) {
                // Shadow ray: check if any object occludes the path to the sun
                Ray shadowRay = new Ray(hitRecord.point, sunDir);
                HitRecord shadowHit = scene.intersect(shadowRay, new Interval(0.0001, Double.MAX_VALUE));
                if (shadowHit == null || shadowHit.objectIndex == -1) {
                    // Unoccluded — add sun's spectral radiance weighted by Lambertian BRDF
                    // BRDF for Lambertian = albedo * spectralReflectance / π
                    // MC estimator: BRDF * L_sun * cos(θ) * Ω_sun
                    // (uniform sampling over sun cone, PDF = 1/Ω_sun)
                    double sunRadiance = sun.getSpectralRadiance(shadowRay, lambda);
                    double brdf = color.spectralReflectance(lambda) * albedo / Math.PI;
                    directRadiance = brdf * cosSun * sunRadiance * sun.getSunSolidAngle();
                }
            }
        }

        RaySpectral result = new RaySpectral(hitRecord.point, scatterDirection, lambda, reflectance);
        result.setRadiance(emittedRadiance + directRadiance);
        return result;
    }

    /**
     * Sample a uniformly random direction within a cone of half-angle θ around the given axis.
     */
    private static Vector sampleSunCone(Vector axis, double halfAngle) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double cosMax = Math.cos(halfAngle);
        double cosTheta = 1.0 - rng.nextDouble() * (1.0 - cosMax);
        double sinTheta = Math.sqrt(1.0 - cosTheta * cosTheta);
        double phi = 2.0 * Math.PI * rng.nextDouble();

        // Build local coordinate frame around axis
        Vector w = axis;
        Vector a = (Math.abs(w.x) > 0.9) ? new Vector(0, 1, 0) : new Vector(1, 0, 0);
        Vector u = a.cross(w).normalize();
        Vector v = w.cross(u);

        return u.scale(sinTheta * Math.cos(phi))
                .add(v.scale(sinTheta * Math.sin(phi)))
                .add(w.scale(cosTheta))
                .normalize();
    }
}
