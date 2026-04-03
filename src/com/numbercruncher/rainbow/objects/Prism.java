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
public class Prism implements SceneObject {

    private final Vector center;      // center of the prism
    private final double sideLength;  // side length of equilateral triangle
    private final double halfHeight;  // half-extent along y-axis
    private final Material material;

    // Precomputed plane data: normals (outward) and offsets
    // Each plane is defined by: normal.dot(point) <= offset  (inside)
    private final Vector[] normals;
    private final double[] offsets;
    private final AABB bounds;

    /**
     * @param center     center of the prism in world space
     * @param sideLength side length of the equilateral triangle
     * @param height     extent along the y-axis
     * @param material   glass material (should be MaterialGlass for dispersion)
     */
    public Prism(Vector center, double sideLength, double height, Material material) {
        this(center, new Vector(0, 0, 0), sideLength, height, material);
      }

    public Prism(Vector center, Vector rotation, double sideLength, double height, Material material) {
        this.center = center;
        this.sideLength = sideLength;
        this.halfHeight = height / 2.0;
        this.material = material;

        // Build rotation matrix from Euler angles (XYZ convention)
        RotationMatrix rot = new RotationMatrix(rotation);

        // Equilateral triangle with one vertex pointing up (+z), flat side at bottom
        double R = sideLength / Math.sqrt(3.0);

        // 3 vertices of the triangle in local coordinates (centered at origin), then rotated
        Vector v0 = rot.map(new Vector(0, 0, R));
        Vector v1 = rot.map(new Vector(-sideLength / 2.0, 0, -R / 2.0));
        Vector v2 = rot.map(new Vector(sideLength / 2.0, 0, -R / 2.0));

        // Rotated cap direction (local y-axis rotated)
        Vector capDir = rot.map(new Vector(0, 1, 0));

        // Shift to world position
        v0 = v0.add(center);
        v1 = v1.add(center);
        v2 = v2.add(center);

        // 5 planes: 3 sides + 2 caps
        normals = new Vector[5];
        offsets = new double[5];

        // Side planes: outward normal for each edge
        normals[0] = edgeOutwardNormal(v0, v1, v2, capDir);
        offsets[0] = normals[0].dot(v0);

        normals[1] = edgeOutwardNormal(v1, v2, v0, capDir);
        offsets[1] = normals[1].dot(v1);

        normals[2] = edgeOutwardNormal(v2, v0, v1, capDir);
        offsets[2] = normals[2].dot(v2);

        // Cap planes along rotated y-axis
        normals[3] = capDir.neg();
        offsets[3] = normals[3].dot(center.sub(capDir.scale(halfHeight)));

        normals[4] = capDir;
        offsets[4] = normals[4].dot(center.add(capDir.scale(halfHeight)));

        // Compute AABB from the 6 prism vertices
        Vector capOffset = capDir.scale(halfHeight);
        this.bounds = AABB.fromPoints(
                v0.add(capOffset), v0.sub(capOffset),
                v1.add(capOffset), v1.sub(capOffset),
                v2.add(capOffset), v2.sub(capOffset));
    }

    /**
     * Compute the outward normal for edge (a -> b) of the triangular face,
     * pointing away from the opposite vertex c.
     * The normal is perpendicular to both the edge and the prism axis (capDir).
     */
    private Vector edgeOutwardNormal(Vector a, Vector b, Vector opposite, Vector capDir) {
        Vector edge = b.sub(a);
        // Normal lies in the plane perpendicular to capDir
        Vector n = edge.cross(capDir).normalize();
        // Pick the direction pointing away from the opposite vertex
        Vector mid = a.add(b).scale(0.5);
        Vector toOpp = opposite.sub(mid);
        if (n.dot(toOpp) > 0) {
            n = n.neg();
        }
        return n;
    }

    @Override
    public HitRecord intersect(Ray ray) {
        // Slab intersection: find tNear (entry) and tFar (exit)
        // across all 5 half-spaces
        double tNear = -Double.MAX_VALUE;
        double tFar = Double.MAX_VALUE;
        int nearFace = -1;
        int farFace = -1;

        Vector orig = ray.getStart();
        Vector dir = ray.getDirection();

        for (int i = 0; i < 5; i++) {
            double denom = normals[i].dot(dir);
            double dist = offsets[i] - normals[i].dot(orig);

            if (Math.abs(denom) < 1e-12) {
                // Ray parallel to plane
                if (dist < 0) return null; // outside this slab, no intersection
            } else {
                double t = dist / denom;
                if (denom < 0) {
                    // Entering this slab
                    if (t > tNear) {
                        tNear = t;
                        nearFace = i;
                    }
                } else {
                    // Exiting this slab
                    if (t < tFar) {
                        tFar = t;
                        farFace = i;
                    }
                }
            }
        }

        if (tNear > tFar) return null; // miss

        double EPS = 1e-8;
        if (tNear > EPS) {
            Vector p = ray.at(tNear);
            return new HitRecord(p, normals[nearFace], tNear);
        } else if (tFar > EPS) {
            // Ray starts inside the prism — exit hit
            Vector p = ray.at(tFar);
            return new HitRecord(p, normals[farFace], tFar);
        }
        return null;
    }


    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public AABB getBounds() { return bounds; }
}
