package com.numbercruncher.rainbow.materials;

import com.numbercruncher.rainbow.*;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Radiance;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.ray_tools.RaySpectral;
import com.numbercruncher.rainbow.sky.SkySunny;

import java.util.concurrent.ThreadLocalRandom;

import static com.numbercruncher.rainbow.Utils.EPS;
import static com.numbercruncher.rainbow.sky.SkySunny.SUN_ANGLE;

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
    public RaySpectral scatter(Ray ray, HitRecord hitRecord, double lambda, Scene scene){
        Vector scatterDirection = Vector.lambertianReflection(hitRecord.normal);

        // 1. Reflectance contribution (indirect light via recursive bounce)
        //    cos(θ) factor darkens grazing angles → more pronounced shadows
        double cosTheta = Math.max(0, scatterDirection.dot(hitRecord.normal));
        double reflectance = color.spectralReflectance(lambda) * cosTheta * albedo;

        // 2. Emission contribution (self-emitted radiance)
        double emittedRadiance = getEmission();

        // 3. Direct sun sampling (next event estimation)
        //    If the sky has a sun, sample a shadow ray toward the sun disk.
        //    If unoccluded, add the sun's direct spectral contribution weighted by BRDF.
        double directRadiance = 0;
        if (scene.getSky() instanceof SkySunny) {
            SkySunny sun = (SkySunny) scene.getSky();
            // Sample a random direction within the sun's cone
            Vector sunDir = sampleSunCone(sun.getSunDirection(), Math.toRadians(SUN_ANGLE));
            double cosSun = sunDir.dot(hitRecord.normal);
            if (cosSun > 0) {
                // Shadow ray: check if any object occludes the path to the sun
                Ray shadowRay = new Ray(hitRecord.point, sunDir);
                HitRecord shadowHit = scene.intersect(shadowRay, new Interval(0.0001, Double.MAX_VALUE));
                if (shadowHit == null || shadowHit.objectIndex == -1) {
                    // Unoccluded — add sun's spectral radiance weighted by Lambertian BRDF
                    // BRDF for Lambertian = albedo * spectralReflectance / π
                    // The solid angle of the sun cone is 2π(1 - cos(θ_sun))
                    double sunAngularRadius = Math.toRadians(SUN_ANGLE);
                    double sunSolidAngle = 2.0 * Math.PI * (1.0 - Math.cos(sunAngularRadius));
                    Radiance sunRadiance = sun.getSpectralRadiance(shadowRay, lambda);
                    double brdf = color.spectralReflectance(lambda) * albedo / Math.PI;
                    directRadiance = brdf * cosSun * sunRadiance.value;
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
