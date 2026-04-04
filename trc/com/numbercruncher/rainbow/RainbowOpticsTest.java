package com.numbercruncher.rainbow;

import com.numbercruncher.rainbow.materials.Glass;
import com.numbercruncher.rainbow.objects.Sphere;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.ray_tools.RaySpectral;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verify that refraction through a water sphere produces the correct
 * rainbow deviation angle, including wavelength-dependent dispersion.
 *
 * Theory (primary rainbow, one internal reflection):
 *   D(theta_i) = 2*theta_i - 4*theta_r + pi
 *   where theta_r = arcsin(sin(theta_i) / n)
 *
 *   Minimum deviation at cos(theta_i) = sqrt((n^2 - 1) / 3)
 *
 *   For water (n ~ 1.333 at 589nm):  D_min ~ 137.8°  (scattering ~ 42.2°)
 *   For violet (n ~ 1.345 at 380nm): D_min ~ 139.7°  (scattering ~ 40.3°)
 *   For red    (n ~ 1.329 at 780nm): D_min ~ 137.1°  (scattering ~ 42.9°)
 */
class RainbowOpticsTest {

    // Water Cauchy coefficients (same as in Scene.createRainbowScene)
    private static final double CAUCHY_B = 1.324;
    private static final double CAUCHY_C = 0.00310; // um^2

    /** Cauchy equation: n(lambda) = B + C / lambda_um^2 */
    private static double waterIOR(double lambdaNm) {
        double um = lambdaNm / 1000.0;
        return CAUCHY_B + CAUCHY_C / (um * um);
    }

    /** Theoretical minimum deviation for primary rainbow. */
    private static double theoryMinDeviation(double n) {
        double cosI = Math.sqrt((n * n - 1.0) / 3.0);
        double thetaI = Math.acos(cosI);
        double thetaR = Math.asin(Math.sin(thetaI) / n);
        return 2.0 * thetaI - 4.0 * thetaR + Math.PI;
    }

    /** Impact parameter that gives minimum deviation. */
    private static double rainbowImpactParam(double n, double R) {
        double cosI = Math.sqrt((n * n - 1.0) / 3.0);
        double thetaI = Math.acos(cosI);
        return R * Math.sin(thetaI);
    }

    // ---- Snell's law (same formulas as Glass, reimplemented here for independent verification) ----

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

    // ---- Sphere intersection (independent of Sphere class) ----

    /** Find the far intersection of a ray with a unit sphere at the origin.
     *  Returns null if no intersection. */
    private static double[] sphereIntersect(Vector origin, Vector dir, double R) {
        double b = origin.dot(dir);
        double c = origin.dot(origin) - R * R;
        double disc = b * b - c;
        if (disc < 0) return null;
        double sqrtDisc = Math.sqrt(disc);
        return new double[]{-b - sqrtDisc, -b + sqrtDisc};
    }

    // =====================================================================
    //  Test 1: Trace a ray through a sphere step by step, verify deviation
    // =====================================================================

    @Test
    void primaryRainbowDeviation_manualTrace() {
        double lambda = 589.0; // sodium D line
        double n = waterIOR(lambda);
        double R = 1.0;
        double D_theory = theoryMinDeviation(n);
        double b = rainbowImpactParam(n, R);

        System.out.printf("=== Manual trace at %.0f nm ===%n", lambda);
        System.out.printf("n = %.6f%n", n);
        System.out.printf("Rainbow impact parameter b = %.6f%n", b);
        System.out.printf("Theoretical deviation D = %.4f deg%n", Math.toDegrees(D_theory));

        // Ray parallel to +x, offset by b in y
        Vector rayDir = new Vector(1, 0, 0);
        Vector rayOrigin = new Vector(-10, b, 0);

        // --- Step 1: Entry intersection ---
        double[] ts = sphereIntersect(rayOrigin, rayDir, R);
        assertNotNull(ts);
        Vector P1 = rayOrigin.add(rayDir.scale(ts[0]));
        Vector n1 = P1.normalize(); // outward normal
        double cosIncidence = -rayDir.dot(n1);
        System.out.printf("Entry angle = %.2f deg%n", Math.toDegrees(Math.acos(cosIncidence)));

        // --- Step 2: Refract into sphere (air -> water, eta = 1/n) ---
        Vector d1 = refract(rayDir, n1, 1.0 / n);
        assertNotNull(d1, "Total internal reflection at entry — should not happen");

        // --- Step 3: Find back-surface intersection (from inside) ---
        double[] ts2 = sphereIntersect(P1, d1, R);
        assertNotNull(ts2);
        double t2 = ts2[1]; // far intersection
        assertTrue(t2 > 1e-6, "Back intersection too close");
        Vector P2 = P1.add(d1.scale(t2));
        Vector n2outward = P2.normalize();

        // --- Step 4: Reflect off back surface (internal reflection) ---
        // Ray is inside, normal points outward, so inward normal = -n2outward
        Vector d2 = reflect(d1, n2outward.neg());

        // --- Step 5: Find front-surface intersection again ---
        double[] ts3 = sphereIntersect(P2, d2, R);
        assertNotNull(ts3);
        double t3 = ts3[1];
        assertTrue(t3 > 1e-6, "Exit intersection too close");
        Vector P3 = P2.add(d2.scale(t3));
        Vector n3outward = P3.normalize();

        // --- Step 6: Refract out (water -> air, eta = n/1) ---
        Vector d3 = refract(d2, n3outward.neg(), n);
        assertNotNull(d3, "Total internal reflection at exit — should not happen at rainbow angle");

        // --- Deviation angle ---
        double cosD = rayDir.dot(d3);
        double D_numerical = Math.acos(Math.max(-1, Math.min(1, cosD)));

        System.out.printf("Numerical deviation  D = %.4f deg%n", Math.toDegrees(D_numerical));
        System.out.printf("Scattering angle     = %.4f deg (180 - D)%n",
                180.0 - Math.toDegrees(D_numerical));

        assertEquals(Math.toDegrees(D_theory), Math.toDegrees(D_numerical), 0.05,
                "Deviation should match theory within 0.05 degrees");
    }

    // =====================================================================
    //  Test 2: Dispersion — red and violet produce different rainbow angles
    // =====================================================================

    @Test
    void rainbowDispersion() {
        System.out.println("=== Dispersion test ===");
        double[] wavelengths = {380, 450, 520, 589, 650, 780};

        double prevScattering = 0;
        for (double lambda : wavelengths) {
            double n = waterIOR(lambda);
            double D = theoryMinDeviation(n);
            double scattering = 180.0 - Math.toDegrees(D);

            // Trace numerically to verify
            double b = rainbowImpactParam(n, 1.0);
            double D_num = traceRainbowRay(b, n);

            System.out.printf("  lambda=%3.0f nm  n=%.6f  scattering=%.2f deg  (numerical=%.2f deg)%n",
                    lambda, n, scattering, 180 - Math.toDegrees(D_num));

            assertEquals(Math.toDegrees(D), Math.toDegrees(D_num), 0.05,
                    "Deviation mismatch at " + lambda + " nm");

            // Iterating short→long wavelength: n decreases → less deviation → LARGER scattering angle
            assertTrue(scattering > prevScattering,
                    "Scattering angle should increase with longer wavelength (less refraction)");
            prevScattering = scattering;
        }

        // Check the overall rainbow width from dispersion
        double nRed = waterIOR(780);
        double nViolet = waterIOR(380);
        double scatteringRed = 180 - Math.toDegrees(theoryMinDeviation(nRed));
        double scatteringViolet = 180 - Math.toDegrees(theoryMinDeviation(nViolet));
        double width = scatteringRed - scatteringViolet;
        System.out.printf("  Rainbow width (red-violet) = %.2f deg%n", width);
        assertTrue(width > 1.5 && width < 3.5,
                "Rainbow angular width should be ~2-3 degrees for water");
    }

    /** Helper: trace a ray at the given impact parameter through a unit sphere and return deviation. */
    private double traceRainbowRay(double b, double n) {
        Vector rayDir = new Vector(1, 0, 0);
        Vector origin = new Vector(-10, b, 0);

        double[] ts1 = sphereIntersect(origin, rayDir, 1.0);
        Vector P1 = origin.add(rayDir.scale(ts1[0]));
        Vector d1 = refract(rayDir, P1.normalize(), 1.0 / n);

        double[] ts2 = sphereIntersect(P1, d1, 1.0);
        Vector P2 = P1.add(d1.scale(ts2[1]));
        Vector d2 = reflect(d1, P2.normalize().neg());

        double[] ts3 = sphereIntersect(P2, d2, 1.0);
        Vector P3 = P2.add(d2.scale(ts3[1]));
        Vector d3 = refract(d2, P3.normalize().neg(), n);

        return Math.acos(Math.max(-1, Math.min(1, rayDir.dot(d3))));
    }

    // =====================================================================
    //  Test 3: Glass.indexOfRefraction matches Cauchy equation
    // =====================================================================

    @Test
    void glassIndexOfRefraction() {
        Glass water = new Glass(CAUCHY_B, CAUCHY_C);

        double[] lambdas = {380, 450, 550, 589, 650, 780};
        for (double lam : lambdas) {
            double expected = waterIOR(lam);
            double actual = water.indexOfRefraction(lam);
            assertEquals(expected, actual, 1e-10,
                    "IOR mismatch at " + lam + " nm");
        }
    }

    // =====================================================================
    //  Test 4: Full path through Glass.scatter + Sphere.intersect
    //          Collect exit-ray statistics and verify the deviation
    //          distribution peaks at the rainbow angle.
    // =====================================================================

    @Test
    void rainbowPeakFromScatter() {
        double lambda = 550.0;
        Glass water = new Glass(CAUCHY_B, CAUCHY_C);
        double n = water.indexOfRefraction(lambda);
        double D_theory = theoryMinDeviation(n);
        double scatterTheory = 180.0 - Math.toDegrees(D_theory);

        // Unit sphere at origin
        Sphere drop = new Sphere(1.0, new Vector(0, 0, 0), water);
        // Dummy scene with no sky, just the drop
        Scene scene = new Scene().addObject(drop);

        // Send many rays at random impact parameters, trace through
        // the drop using Glass.scatter, and histogram the exit deviation.
        int N = 100_000;
        int bins = 90; // 2-degree bins over [0, 180]
        int[] histogram = new int[bins];
        int rainbowPaths = 0;

        for (int i = 0; i < N; i++) {
            // Random impact parameter in [0, 1) (distance from center, as fraction of radius)
            double b = Math.random();
            Vector origin = new Vector(-10, b, 0);
            Vector dir = new Vector(1, 0, 0);
            Ray ray = new Ray(origin, dir);

            // Step 1: hit the sphere
            HitRecord h1 = drop.intersect(ray);
            if (h1 == null) continue;

            // Step 2: scatter at entry (mostly refracts in)
            RaySpectral s1 = water.scatter(ray, h1, lambda, scene);
            Ray ray2 = new Ray(h1.point, s1.getDirection());

            // Step 3: hit back surface
            HitRecord h2 = drop.intersect(ray2);
            if (h2 == null) continue;

            // Step 4: scatter at back (importance-sampled reflect/refract)
            RaySpectral s2 = water.scatter(ray2, h2, lambda, scene);
            Ray ray3 = new Ray(h2.point, s2.getDirection());

            // Did it reflect (stay inside) or refract out?
            // If the ray still intersects the sphere, it reflected → rainbow path
            HitRecord h3 = drop.intersect(ray3);
            if (h3 == null) continue; // refracted out the back — not a rainbow path

            // Step 5: scatter at front surface (mostly refracts out)
            RaySpectral s3 = water.scatter(ray3, h3, lambda, scene);
            Vector exitDir = s3.getDirection();

            // Check if this exit ray left the sphere (dot with outward normal > 0)
            // by checking if another intersection exists
            Ray ray4 = new Ray(h3.point, exitDir);
            HitRecord h4 = drop.intersect(ray4);
            if (h4 != null) continue; // still inside (another internal reflection) — skip

            // Compute deviation
            double cosD = dir.dot(exitDir);
            double D_deg = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, cosD))));
            int bin = Math.min(bins - 1, (int) (D_deg / 2.0));
            histogram[bin]++;
            rainbowPaths++;
        }

        System.out.println("=== Scatter-based deviation histogram ===");
        System.out.printf("Rainbow paths: %d / %d rays%n", rainbowPaths, N);
        System.out.printf("Theory: D_min = %.2f deg (scattering = %.2f deg)%n",
                Math.toDegrees(D_theory), scatterTheory);

        // Find the peak bin
        int peakBin = 0, peakCount = 0;
        for (int i = 0; i < bins; i++) {
            if (histogram[i] > peakCount) {
                peakCount = histogram[i];
                peakBin = i;
            }
        }
        double peakAngle = peakBin * 2.0 + 1.0; // center of bin
        System.out.printf("Peak bin: [%.0f, %.0f) deg — count = %d%n",
                peakBin * 2.0, (peakBin + 1) * 2.0, peakCount);

        // Print the histogram around the peak
        System.out.println("Histogram (deviation angle → count):");
        for (int i = Math.max(0, peakBin - 10); i < Math.min(bins, peakBin + 10); i++) {
            String bar = "*".repeat(Math.min(60, histogram[i] * 60 / Math.max(1, peakCount)));
            System.out.printf("  [%3d-%3d) %5d  %s%n", i * 2, (i + 1) * 2, histogram[i], bar);
        }

        // The peak should be near D_theory (within one bin width = 2 degrees)
        assertEquals(Math.toDegrees(D_theory), peakAngle, 3.0,
                "Histogram peak should be near theoretical rainbow deviation");
    }

    // =====================================================================
    //  Exact Fresnel equations (unpolarized) — used as reference
    // =====================================================================

    /**
     * Exact Fresnel reflectance for unpolarized light.
     *
     * @param cosI  cos(angle of incidence), positive
     * @param n1    refractive index on incident side
     * @param n2    refractive index on transmitted side
     * @return reflectance in [0, 1]
     */
    private static double fresnelExact(double cosI, double n1, double n2) {
        double sinI = Math.sqrt(1.0 - cosI * cosI);
        double sinT = n1 / n2 * sinI;
        if (sinT >= 1.0) return 1.0; // total internal reflection
        double cosT = Math.sqrt(1.0 - sinT * sinT);

        double rs = (n1 * cosI - n2 * cosT) / (n1 * cosI + n2 * cosT);
        double rp = (n2 * cosI - n1 * cosT) / (n2 * cosI + n1 * cosT);
        return 0.5 * (rs * rs + rp * rp);
    }

    // =====================================================================
    //  Test 5: Schlick approximation vs exact Fresnel
    // =====================================================================

    @Test
    void fresnelMatchesExact() {
        double n = 1.333; // water
        Glass water = new Glass(n, 0); // no dispersion — fixed IOR
        double lambda = 550.0;

        System.out.println("=== Glass.fresnel vs exact Fresnel (air → water, n=1.333) ===");
        System.out.printf("  %8s  %10s  %10s  %10s%n", "angle", "exact", "measured", "error");

        // Test air → water (entering)
        for (int deg = 0; deg <= 85; deg += 5) {
            double cosI = Math.cos(Math.toRadians(deg));
            double exact = fresnelExact(cosI, 1.0, n);
            double measured = fresnelFromScatter(water, lambda, cosI, true);

            System.out.printf("  %6d°  %10.6f  %10.6f  %+10.6f%n", deg, exact, measured, measured - exact);

            // Exact Fresnel — only Monte Carlo noise, tight tolerance
            assertEquals(exact, measured, 0.02,
                    "Fresnel mismatch at " + deg + "° (entering)");
        }

        // Test water → air (exiting)
        System.out.println("\n=== Glass.fresnel vs exact Fresnel (water → air, n=1.333) ===");
        System.out.printf("  %8s  %10s  %10s  %10s%n", "angle", "exact", "measured", "error");

        double criticalAngle = Math.toDegrees(Math.asin(1.0 / n)); // ~48.6°
        for (int deg = 10; deg < (int) criticalAngle; deg += 5) {
            double cosI = Math.cos(Math.toRadians(deg));
            double exact = fresnelExact(cosI, n, 1.0);
            double measured = fresnelFromScatter(water, lambda, cosI, false);

            System.out.printf("  %6d°  %10.6f  %10.6f  %+10.6f%n", deg, exact, measured, measured - exact);

            assertEquals(exact, measured, 0.02,
                    "Fresnel mismatch at " + deg + "° (exiting)");
        }

        // Just above critical angle — reflectance must be 1.0 (total internal reflection)
        double cosICrit = Math.cos(Math.asin(1.0 / n) + Math.toRadians(0.1));
        double exactCrit = fresnelExact(cosICrit, n, 1.0);
        assertEquals(1.0, exactCrit, 1e-10,
                "Fresnel reflectance above critical angle should be exactly 1.0 (TIR)");
    }

    /**
     * Extract the Schlick reflectance from Glass.scatter by running many trials
     * and measuring the reflection fraction, corrected for importance sampling weights.
     *
     * Since scatter uses importance sampling (MIN_REFLECT_SAMPLE = 0.25),
     * we measure the WEIGHTED reflectance:
     *   R_measured = mean(throughput for reflected samples) * P(reflect sampled)
     *             + mean(throughput for refracted samples) * P(refract sampled) ... (not quite)
     *
     * Actually, it's simpler: run many trials, check if the scattered direction
     * is a reflection or refraction, and compute the weighted average.
     */
    private double fresnelFromScatter(Glass glass, double lambda, double cosI, boolean entering) {
        // Build a flat interface by using a huge sphere (locally flat)
        // Normal along +y, ray coming from -y direction at angle theta
        double theta = Math.acos(cosI);
        Vector normal = new Vector(0, 1, 0);
        Vector incident;
        if (entering) {
            // Ray comes from outside, hitting surface with outward normal +y
            incident = new Vector(Math.sin(theta), -Math.cos(theta), 0);
        } else {
            // Ray comes from inside, hitting surface with outward normal +y
            // (outward means away from glass, so ray comes from glass side = -y direction)
            incident = new Vector(Math.sin(theta), Math.cos(theta), 0);
        }
        incident = incident.normalize();

        // HitRecord with the surface point and normal
        com.numbercruncher.rainbow.ray_tools.HitRecord hit =
                new com.numbercruncher.rainbow.ray_tools.HitRecord(
                        new Vector(0, 0, 0), normal, 1.0);

        // Dummy scene
        Scene scene = new Scene();

        // Run many trials and compute average throughput for reflection path
        int N = 100_000;
        double reflectWeightSum = 0;
        double refractWeightSum = 0;
        int reflectCount = 0;

        for (int i = 0; i < N; i++) {
            Ray ray = new Ray(incident.scale(-1), incident); // origin doesn't matter for scatter
            RaySpectral result = glass.scatter(ray, hit, lambda, scene);
            Vector dir = result.getDirection();

            // Check if reflected: reflected direction has same y-sign as incident
            boolean reflected;
            if (entering) {
                // Entering: incident goes -y, reflected bounces back to +y
                reflected = dir.y > 0;
            } else {
                // Exiting: incident goes +y, reflected bounces back to -y (into the glass)
                reflected = dir.y < 0;
            }

            if (reflected) {
                reflectWeightSum += result.getThroughput();
                reflectCount++;
            } else {
                refractWeightSum += result.getThroughput();
            }
        }

        // The importance-sampled estimator for reflectance:
        // E[throughput | reflected] * P(reflected sampled) = true reflectance * transparency
        // Since transparency = 1 for our test glass:
        // Schlick reflectance = reflectWeightSum / N
        return reflectWeightSum / N;
    }

    // =====================================================================
    //  Test 6: Energy conservation — total throughput should equal transparency
    // =====================================================================

    @Test
    void energyConservation() {
        Glass water = new Glass(1.333, 0);
        double lambda = 550.0;
        Scene scene = new Scene();

        System.out.println("=== Energy conservation test ===");
        System.out.printf("  %8s  %12s  %12s  %12s%n",
                "angle", "E[throughput]", "expected", "error");

        // For a single interface, the expected total throughput = transparency = 1.0
        // because: P(reflect)*weight_reflect + P(refract)*weight_refract
        //        = sampleP * (R/sampleP) + (1-sampleP) * ((1-R)/(1-sampleP))
        //        = R + (1-R) = 1.0
        //
        // Test at various angles of incidence (entering glass)
        for (int deg = 0; deg <= 80; deg += 10) {
            double cosI = Math.cos(Math.toRadians(deg));
            double theta = Math.toRadians(deg);
            Vector incident = new Vector(Math.sin(theta), -Math.cos(theta), 0).normalize();
            Vector normal = new Vector(0, 1, 0);

            com.numbercruncher.rainbow.ray_tools.HitRecord hit =
                    new com.numbercruncher.rainbow.ray_tools.HitRecord(
                            new Vector(0, 0, 0), normal, 1.0);

            int N = 50_000;
            double totalThroughput = 0;
            for (int i = 0; i < N; i++) {
                Ray ray = new Ray(incident.scale(-1), incident);
                RaySpectral result = water.scatter(ray, hit, lambda, scene);
                totalThroughput += result.getThroughput();
            }
            double avgThroughput = totalThroughput / N;

            System.out.printf("  %6d°  %12.6f  %12.6f  %+12.6f%n",
                    deg, avgThroughput, 1.0, avgThroughput - 1.0);

            // Should be very close to 1.0 (unbiased estimator)
            assertEquals(1.0, avgThroughput, 0.02,
                    "Average throughput should be 1.0 (energy conservation) at " + deg + "°");
        }

        // Also test exiting glass (water → air) at angles below critical angle
        System.out.println("\n  Exiting (water → air):");
        double critDeg = Math.toDegrees(Math.asin(1.0 / 1.333));
        for (int deg = 0; deg < (int) critDeg - 5; deg += 10) {
            double theta = Math.toRadians(deg);
            // Ray from inside glass (+y direction), hitting surface with outward normal +y
            Vector incident = new Vector(Math.sin(theta), Math.cos(theta), 0).normalize();
            Vector normal = new Vector(0, 1, 0);

            com.numbercruncher.rainbow.ray_tools.HitRecord hit =
                    new com.numbercruncher.rainbow.ray_tools.HitRecord(
                            new Vector(0, 0, 0), normal, 1.0);

            int N = 50_000;
            double totalThroughput = 0;
            for (int i = 0; i < N; i++) {
                Ray ray = new Ray(incident.scale(-1), incident);
                RaySpectral result = water.scatter(ray, hit, lambda, scene);
                totalThroughput += result.getThroughput();
            }
            double avgThroughput = totalThroughput / N;

            System.out.printf("  %6d°  %12.6f  %12.6f  %+12.6f%n",
                    deg, avgThroughput, 1.0, avgThroughput - 1.0);

            assertEquals(1.0, avgThroughput, 0.02,
                    "Average throughput should be 1.0 (energy conservation) at exit " + deg + "°");
        }
    }

    // =====================================================================
    //  Test 7: Total internal reflection at critical angle
    // =====================================================================

    @Test
    void totalInternalReflection() {
        double n = 1.333;
        Glass water = new Glass(n, 0);
        double lambda = 550.0;
        Scene scene = new Scene();

        double criticalAngle = Math.asin(1.0 / n); // ~48.6°
        System.out.printf("=== TIR test: critical angle = %.2f° ===%n",
                Math.toDegrees(criticalAngle));

        // Above critical angle — must always reflect
        for (int degAbove = 1; degAbove <= 20; degAbove += 5) {
            double theta = criticalAngle + Math.toRadians(degAbove);
            // Ray from inside glass, going upward (+y) toward surface
            Vector incident = new Vector(Math.sin(theta), Math.cos(theta), 0).normalize();
            Vector normal = new Vector(0, 1, 0);

            com.numbercruncher.rainbow.ray_tools.HitRecord hit =
                    new com.numbercruncher.rainbow.ray_tools.HitRecord(
                            new Vector(0, 0, 0), normal, 1.0);

            int N = 1000;
            int reflections = 0;
            for (int i = 0; i < N; i++) {
                Ray ray = new Ray(incident.scale(-1), incident);
                RaySpectral result = water.scatter(ray, hit, lambda, scene);
                // Incident goes in +y, TIR bounces it back to -y (into the glass)
                if (result.getDirection().y < 0) reflections++;
            }

            System.out.printf("  %d° above critical: %d/%d reflected%n",
                    degAbove, reflections, N);
            assertEquals(N, reflections,
                    "All rays should reflect above critical angle (" + degAbove + "° above)");
        }
    }
}
