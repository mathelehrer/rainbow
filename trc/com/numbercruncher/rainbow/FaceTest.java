package com.numbercruncher.rainbow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FaceTest {

    @Test
    void inside() {
        double sideLength = 1;
        Vector corner000 =new Vector();
        Vector corner100 =new Vector(sideLength,0,0);
        Vector corner110 =new Vector(sideLength,sideLength,0);
        Vector corner010 =new Vector(0,sideLength,0);
        Vector corner001 =new Vector(0,0,sideLength);
        Vector corner101 =new Vector(sideLength,0,sideLength);
        Vector corner011 =new Vector(0,sideLength,sideLength);
        Vector corner111 =new Vector(sideLength,sideLength,sideLength);
        Vector[] corners ={corner000,corner100,corner110,corner010,corner001,corner101,corner011,corner111};



        Face[] faces = new Face[6];
        faces[0] = new Face(corners,new int[]{0,3,2,1});
        faces[1] = new Face(corners,new int[]{0,1,5,4});
        faces[2] = new Face(corners,new int[]{0,4,6,3});
        faces[3] = new Face(corners,new int[]{7,2,3,6});
        faces[4] = new Face(corners,new int[]{7,5,1,2});
        faces[5] = new Face(corners,new int[]{7,6,4,5});

        for (int j = 0; j < 10000; j++) {
            // construct random point
            double x = Math.random();
            double y = Math.random();
            double side = Math.random();

            double z = 0;
            if (side < 0.5)
                z = 0;
            else
                z = sideLength;
            Vector p = new Vector(x, y, z);
            Vector q = new Vector(x, z, y);
            Vector r = new Vector(z, y, x);

            boolean qContained = false;
            boolean rContained = false;
            boolean pContained = false;

            for (int i = 0; i < faces.length; i++) {
                Face face = faces[i];
                if (face.contains(p)) pContained = true;
                if (face.contains(q)) qContained = true;
                if (face.contains(r)) rContained = true;
            }

            assertTrue(qContained && rContained && pContained);
        }
    }

    @Test
    void contains() {
        double sideLength = 1;
        Vector corner000 =new Vector();
        Vector corner100 =new Vector(sideLength,0,0);
        Vector corner110 =new Vector(sideLength,sideLength,0);
        Vector corner010 =new Vector(0,sideLength,0);
        Vector corner001 =new Vector(0,0,sideLength);
        Vector corner101 =new Vector(sideLength,0,sideLength);
        Vector corner011 =new Vector(0,sideLength,sideLength);
        Vector corner111 =new Vector(sideLength,sideLength,sideLength);
        Vector[] corners ={corner000,corner100,corner110,corner010,corner001,corner101,corner011,corner111};



        Face[] faces = new Face[6];
        faces[0] = new Face(corners,new int[]{0,3,2,1});
        faces[1] = new Face(corners,new int[]{0,1,5,4});
        faces[2] = new Face(corners,new int[]{0,4,6,3});
        faces[3] = new Face(corners,new int[]{7,2,3,6});
        faces[4] = new Face(corners,new int[]{7,5,1,2});
        faces[5] = new Face(corners,new int[]{7,6,4,5});


    }
}