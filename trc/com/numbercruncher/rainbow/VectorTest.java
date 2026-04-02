package com.numbercruncher.rainbow;

import org.junit.jupiter.api.Test;

import static com.numbercruncher.rainbow.Utils.EPS;
import static org.junit.jupiter.api.Assertions.*;

class VectorTest {

    /**
     * Check that one hundred random unit vectors lie inside a hemisphere
     * around a random normal vector
     */
    @Test
    void randomUnitOnHemisphere() {
        Vector normal = Vector.randomUnitVector();
        assertTrue(Math.abs(normal.dot(normal)-1)<EPS);
        for (int i = 0; i < 100; i++) {
            Vector v = Vector.randomUnitOnHemisphere(normal);
            assertTrue(v.dot(normal) >= 0);
            assertTrue(Math.abs(v.dot(v)-1)<EPS);
        }
    }

    @Test
    void getRandomOrthonormal() {
        for (int i = 0; i < 10000; i++) {
            Vector n = Vector.randomUnitVector();
            Vector a = n.getRandomOrthonormal();
            assertTrue(Math.abs(a.dot(n))<EPS);

        }
    }

    @Test
    void cross() {
        for (int i = 0; i < 10000; i++) {
            Vector n  = Vector.randomUnitVector();
            Vector a = n.getRandomOrthonormal();
            Vector b = n.cross(a).normalize();
            assertTrue(Math.abs(n.dot(a))<EPS);
            assertTrue(Math.abs(n.dot(b))<EPS);
            assertTrue(Math.abs(a.dot(b))<EPS);
        }
    }

    @Test
    void reflection(){
        for (int i = 0; i < 10000; i++) {
            Vector n = new Vector(0, 1, 0);
            Vector in = Vector.randomUnitOnHemisphere(n);
            Vector o = Vector.metalReflection(n, in, 0);
            //Check law of reflection
            assertTrue(Math.abs(n.dot(o) - n.dot(in.neg())) < EPS);
        }

    }
}