package com.numbercruncher.rainbow;

import com.numbercruncher.rainbow.materials.Lambertian;
import com.numbercruncher.rainbow.materials.Material;
import com.numbercruncher.rainbow.materials.Metal;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.ray_tools.RaySpectral;
import com.numbercruncher.rainbow.sky.SkySunny;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Forward photon trace from the sun, depositing flux on Lambertian surfaces
 * after at least one specular bounce. The deposits accumulate in a
 * {@link CausticMap}; Lambertian scatter then reads the map as an extra
 * incoming-irradiance term, giving a clean caustic without the high
 * variance of pure backward tracing.
 *
 * Photon flux for N photons emitted uniformly within the sun cone, over a
 * disk of area A perpendicular to the sun, sampled uniformly across the
 * full visible spectrum:
 *   Φ_p = L_sun(λ_p) · Ω_sun · A_disk · Δλ / N
 * where L_sun is the per-steradian spectral radiance returned by
 * {@link SkySunny#getSpectralRadiance}.
 */
public class CausticTracer {

    /**
     * Shoot N photons from the sun and deposit caustic flux into the map.
     *
     * @param scene        scene containing a {@link SkySunny}
     * @param map          caustic map to deposit into
     * @param N            number of photons
     * @param diskRadius   radius of the photon-emission disk (perpendicular
     *                     to the sun direction); must cover the cross-section
     *                     of the scene as seen from the sun
     * @param diskDistance distance of the emission disk along the sun direction
     *                     (must be far enough that the disk lies outside any
     *                     scene geometry)
     */
    public static void shoot(Scene scene, CausticMap map, int N,
                             double diskRadius, double diskDistance) {
        if (!(scene.getSky() instanceof SkySunny)) {
            throw new IllegalStateException(
                    "CausticTracer requires SkySunny; got " + scene.getSky().getClass().getSimpleName());
        }
        SkySunny sun = (SkySunny) scene.getSky();
        Vector sunDir = sun.getSunDirection();
        double sunAngularRadius = sun.getSunAngularRadius();
        double sunSolidAngle = sun.getSunSolidAngle();

        // Build orthonormal basis (u, v) perpendicular to sun direction.
        Vector seed = (Math.abs(sunDir.x) > 0.9) ? new Vector(0, 1, 0) : new Vector(1, 0, 0);
        Vector u = seed.sub(sunDir.scale(seed.dot(sunDir))).normalize();
        Vector v = sunDir.cross(u).normalize();

        // Emission disk: place along +sunDir at diskDistance from the origin.
        Vector diskCenter = sunDir.scale(diskDistance);

        double diskArea = Math.PI * diskRadius * diskRadius;
        double lambdaRange = CIE1931.LAMBDA_MAX - CIE1931.LAMBDA_MIN;

        // Pre-compute the per-photon flux factor that's independent of λ:
        // Φ_p(λ) = L_sun(λ) · Ω_sun · A_disk · Δλ / N
        final double fluxScale = sunSolidAngle * diskArea * lambdaRange / N;

        // Reference ray pointing exactly at the sun centre — used to read
        // L_sun at each wavelength.
        Ray sunRay = new Ray(new Vector(0, 0, 0), sunDir);

        AtomicInteger progress = new AtomicInteger();
        long start = System.currentTimeMillis();
        IntStream.range(0, N).parallel().forEach(i -> {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            double lambda = CIE1931.LAMBDA_MIN + rng.nextDouble() * lambdaRange;

            // Sample direction within sun cone: photons travel away from sun,
            // so the cone axis for travel direction is -sunDir.
            Vector dir = sampleCone(sunDir.neg(), sunAngularRadius, rng);

            // Sample disk position
            double r = diskRadius * Math.sqrt(rng.nextDouble());
            double phi = 2.0 * Math.PI * rng.nextDouble();
            Vector startPos = diskCenter
                    .add(u.scale(r * Math.cos(phi)))
                    .add(v.scale(r * Math.sin(phi)));

            double Lsun = sun.getSpectralRadiance(sunRay, lambda);
            double throughput = Lsun * fluxScale;

            Ray ray = new Ray(startPos, dir);
            int specularBounces = 0;
            for (int depth = 0; depth < Camera.maxDepth; depth++) {
                HitRecord rec = scene.intersect(ray, new Interval(1e-4, Double.MAX_VALUE));
                if (rec == null) break;
                Material mat = scene.getObjects().get(rec.objectIndex).getMaterial();

                if (mat instanceof Lambertian) {
                    if (specularBounces > 0) {
                        map.deposit(rec.point.x, rec.point.y, lambda, throughput);
                    }
                    break;
                } else if (mat instanceof Metal) {
                    RaySpectral scatter = mat.scatter(ray, rec, lambda, scene);
                    throughput *= scatter.getThroughput();
                    if (throughput <= 0) break;
                    ray = new Ray(rec.point, scatter.getDirection());
                    specularBounces++;
                } else {
                    // Glass / Default not modelled by this tracer yet.
                    break;
                }
            }

            int done = progress.incrementAndGet();
            if (done % 200_000 == 0) {
                System.out.println("Caustic photons: " + done + "/" + N);
            }
        });
        System.out.println("Caustic shoot time: " + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * Sample a uniformly-random direction within a cone of half-angle
     * {@code halfAngle} around {@code axis}.
     */
    private static Vector sampleCone(Vector axis, double halfAngle, ThreadLocalRandom rng) {
        double cosMax = Math.cos(halfAngle);
        double cosTheta = 1.0 - rng.nextDouble() * (1.0 - cosMax);
        double sinTheta = Math.sqrt(Math.max(0.0, 1.0 - cosTheta * cosTheta));
        double phi = 2.0 * Math.PI * rng.nextDouble();
        Vector w = axis.normalize();
        Vector seed = (Math.abs(w.x) > 0.9) ? new Vector(0, 1, 0) : new Vector(1, 0, 0);
        Vector a = seed.sub(w.scale(seed.dot(w))).normalize();
        Vector b = w.cross(a).normalize();
        return a.scale(sinTheta * Math.cos(phi))
                .add(b.scale(sinTheta * Math.sin(phi)))
                .add(w.scale(cosTheta))
                .normalize();
    }
}
