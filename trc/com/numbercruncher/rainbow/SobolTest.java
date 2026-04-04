package com.numbercruncher.rainbow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SobolTest {

    @Test
    void basicSequence() {
        Sobol sobol = new Sobol();
        double[] x = new double[3];

        for (int i = 0; i < 10; i++) {
            sobol.next(3, x);
            System.out.printf("  [%d] %.6f  %.6f  %.6f%n", i, x[0], x[1], x[2]);
            for (int d = 0; d < 3; d++) {
                assertTrue(x[d] >= 0 && x[d] < 1, "Values should be in [0, 1)");
            }
        }
    }

    @Test
    void twoInstancesProduceSameSequence() {
        Sobol a = new Sobol();
        Sobol b = new Sobol();
        double[] xa = new double[4];
        double[] xb = new double[4];

        for (int i = 0; i < 1000; i++) {
            a.next(4, xa);
            b.next(4, xb);
            assertArrayEquals(xa, xb, 1e-15,
                    "Independent instances should produce identical sequences");
        }
    }

    @Test
    void skipThenGenerate() {
        Sobol full = new Sobol();
        Sobol skipped = new Sobol();

        double[] x = new double[3];

        // Generate 500 points from 'full', discard them
        for (int i = 0; i < 500; i++) full.next(3, x);

        // Skip 500 on 'skipped'
        skipped.skip(500);

        // Now both should produce the same next points
        double[] xf = new double[3];
        double[] xs = new double[3];
        for (int i = 0; i < 100; i++) {
            full.next(3, xf);
            skipped.next(3, xs);
            assertArrayEquals(xf, xs, 1e-15,
                    "After skip, sequences should match");
        }
    }
}
