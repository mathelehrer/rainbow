package com.numbercruncher.rainbow.objects;

import com.numbercruncher.rainbow.*;
import com.numbercruncher.rainbow.materials.Material;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;

/**
 * Triangular prism defined as the intersection of 5 half-spaces:
 * 3 side planes (forming an equilateral triangle cross-section)
 * and 2 cap planes (bounding the prism along its axis).
 *
 * The prism is oriented with:
 *  - equilateral triangle cross-section in the xz-plane
 *  - extended along the y-axis
 *  - one vertex pointing up (+z), flat side at the bottom
 *
 * The center, sideLength, and height (extent along y) are configurable.
 */
public class Cube implements SceneObject {

    private final Vector center;      // center of the prism
    private final double sideLength;

    private final Material material;

    // Precomputed plane data: normals (outward) and offsets
    // Each plane is defined by: normal.dot(point) <= offset  (inside)

    private final Face[] faces;

    /**
     * @param center     center of the prism in world space
     * @param sideLength side length of the equilateral triangle
     * @param material   glass material (should be MaterialGlass for dispersion)
     */
    public Cube(Vector center, double sideLength, Vector rotation,  Material material) {
        this.center = center;
        this.sideLength = sideLength;
        this.material = material;

        Vector corner000 =new Vector();
        Vector corner100 =new Vector(sideLength,0,0);
        Vector corner110 =new Vector(sideLength,sideLength,0);
        Vector corner010 =new Vector(0,sideLength,0);
        Vector corner001 =new Vector(0,0,sideLength);
        Vector corner101 =new Vector(sideLength,0,sideLength);
        Vector corner011 =new Vector(0,sideLength,sideLength);
        Vector corner111 =new Vector(sideLength,sideLength,sideLength);
        Vector[] corners ={corner000,corner100,corner110,corner010,corner001,corner101,corner011,corner111};

        //shift cube center to origin to perform rotation
        Vector shift = new Vector(1, 1, 1).scale(0.5 * sideLength);
        for (int i = 0; i < corners.length; i++) {
            corners[i]=corners[i].sub(shift);
        }

        //rotate
        Matrix rotz= new RotationMatrix(rotation);
        for(int i=0;i<corners.length;i++)
            corners[i] = corners[i].add(shift);

        for (int i = 0; i < corners.length; i++) {
            corners[i] = rotz.map(corners[i]);
        }

        // Shift to world position
        for (int i = 0; i < corners.length; i++) {
            corners[i] = corners[i].add(center);
        }


        faces = new Face[6];
        faces[0] = new Face(corners,new int[]{0,3,2,1});
        faces[1] = new Face(corners,new int[]{0,1,5,4});
        faces[2] = new Face(corners,new int[]{0,4,6,3});
        faces[3] = new Face(corners,new int[]{7,2,3,6});
        faces[4] = new Face(corners,new int[]{7,5,1,2});
        faces[5] = new Face(corners,new int[]{7,6,4,5});

    }

    public Cube(Vector center, double sideLength,  Material material) {
       this(center,sideLength,new Vector(0,0,0),material);

    }


    @Override
    public HitRecord intersect(Ray ray) {
        //find possible intersection with all faces
        //recycle the stuff from the plane
        double tMin = Double.MAX_VALUE;
        int hittenFace = -1;
        Vector hitPoint = null;

        for (int i=0;i<faces.length;i++){
            Face face = faces[i];
            Vector normal = face.getNormal();
            Vector center = face.getCenter();

            Vector bmo = center.sub(ray.getStart());
            Vector d = ray.getDirection();
            double t = bmo.dot(normal)/d.dot(normal);


            if (t>0 && t<tMin) {

                Vector p = ray.at(t);
                if (face.contains(p)) {
                    tMin = t;
                    hittenFace = i;
                    hitPoint = p;
                }
            }
        }

        if (hitPoint == null) return null;
        Vector normal = faces[hittenFace].getNormal();
        return new HitRecord(hitPoint, normal, tMin);
    }

    @Override
    public Vector getNormal(Vector point) {
        for (Face face : faces) {
            if (face.contains(point)) {
                return face.getNormal();
            }
        }
        return null;
    }

    @Override
    public Material getMaterial() {
        return material;
    }
}
