package com.numbercruncher.rainbow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SphereTest {

    @Test
    void intersects() {
        Sphere s = new Sphere(0.5,new Vector(1,1,0),
                new MaterialDefault(Color.RED));

        for (int i = 0; i < 100; i++) {
            double z = 1./100*i;
            Ray r = new Ray(new Vector(0,0,z),new Vector(1,1,0));
            if (i<50)
                assertNotEquals(-1,s.intersect(r));
            else
                assertEquals(-1,s.intersect(r));
        }


    }

    @Test
    void intersect() {

    }
}