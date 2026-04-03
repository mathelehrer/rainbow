package com.numbercruncher.rainbow;

import com.numbercruncher.rainbow.ray_tools.Ray;

/**
 * Axis-aligned bounding box for BVH acceleration.
 * Uses the slab method for fast ray-box intersection.
 */
public class AABB {
    public final Vector min, max;

    public AABB(Vector min, Vector max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Test if a ray intersects this box within [tMin, tMax].
     * Uses the slab method with inline per-axis checks for speed.
     */
    public boolean hit(Ray ray, double tMin, double tMax) {
        Vector orig = ray.getStart();
        Vector dir = ray.getDirection();
        double invD, t0, t1, tmp;

        // X axis
        invD = 1.0 / dir.x;
        t0 = (min.x - orig.x) * invD;
        t1 = (max.x - orig.x) * invD;
        if (invD < 0) { tmp = t0; t0 = t1; t1 = tmp; }
        if (t0 > tMin) tMin = t0;
        if (t1 < tMax) tMax = t1;
        if (tMax <= tMin) return false;

        // Y axis
        invD = 1.0 / dir.y;
        t0 = (min.y - orig.y) * invD;
        t1 = (max.y - orig.y) * invD;
        if (invD < 0) { tmp = t0; t0 = t1; t1 = tmp; }
        if (t0 > tMin) tMin = t0;
        if (t1 < tMax) tMax = t1;
        if (tMax <= tMin) return false;

        // Z axis
        invD = 1.0 / dir.z;
        t0 = (min.z - orig.z) * invD;
        t1 = (max.z - orig.z) * invD;
        if (invD < 0) { tmp = t0; t0 = t1; t1 = tmp; }
        if (t0 > tMin) tMin = t0;
        if (t1 < tMax) tMax = t1;
        if (tMax <= tMin) return false;

        return true;
    }

    /**
     * Compute the smallest AABB enclosing both input boxes.
     */
    public static AABB surrounding(AABB a, AABB b) {
        return new AABB(
                new Vector(Math.min(a.min.x, b.min.x), Math.min(a.min.y, b.min.y), Math.min(a.min.z, b.min.z)),
                new Vector(Math.max(a.max.x, b.max.x), Math.max(a.max.y, b.max.y), Math.max(a.max.z, b.max.z))
        );
    }

    /**
     * Build an AABB from a set of points (finds componentwise min/max).
     */
    public static AABB fromPoints(Vector... points) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (Vector p : points) {
            if (p.x < minX) minX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.z < minZ) minZ = p.z;
            if (p.x > maxX) maxX = p.x;
            if (p.y > maxY) maxY = p.y;
            if (p.z > maxZ) maxZ = p.z;
        }
        return new AABB(new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
    }
}
