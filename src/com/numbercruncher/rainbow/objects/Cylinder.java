package com.numbercruncher.rainbow.objects;

import com.numbercruncher.rainbow.*;
import com.numbercruncher.rainbow.materials.Material;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;

/**
 * Finite open tube — the lateral surface of a cylinder, axis along z.
 * No caps. Useful as the inner wall of a cup, where reflections form a
 * cardioid/nephroid caustic on the cup bottom.
 *
 * Intersection: substitute x(t)=ox+t*dx, y(t)=oy+t*dy into
 *   (x-cx)^2 + (y-cy)^2 = r^2
 * giving the quadratic
 *   a t^2 + 2 b t + c = 0
 * with a = dx^2+dy^2, b = ex*dx+ey*dy, c = ex^2+ey^2-r^2 (ex=ox-cx, ey=oy-cy).
 * Each candidate root is accepted only if zMin <= oz+t*dz <= zMax.
 */
public class Cylinder implements SceneObject {
    private final double radius;
    private final double cx, cy;
    private final double zMin, zMax;
    private final Material material;
    private final AABB bounds;
    private static final double EPS = 1e-8;

    /**
     * @param center base-center of the cylinder (x,y,z); the tube extends from
     *               z=center.z up to z=center.z+height
     * @param radius cylinder radius
     * @param height axial extent along +z
     * @param material material applied to the lateral surface
     */
    public Cylinder(Vector center, double radius, double height, Material material) {
        this.cx = center.x;
        this.cy = center.y;
        this.zMin = center.z;
        this.zMax = center.z + height;
        this.radius = radius;
        this.material = material;
        this.bounds = new AABB(
                new Vector(cx - radius, cy - radius, zMin),
                new Vector(cx + radius, cy + radius, zMax));
    }

    @Override
    public HitRecord intersect(Ray ray) {
        Vector o = ray.getStart();
        Vector d = ray.getDirection();
        double ex = o.x - cx;
        double ey = o.y - cy;
        double a = d.x * d.x + d.y * d.y;
        if (a < EPS) return null; // ray parallel to axis
        double b = ex * d.x + ey * d.y;
        double c = ex * ex + ey * ey - radius * radius;
        double disc = b * b - a * c;
        if (disc < 0) return null;
        double sq = Math.sqrt(disc);
        double t1 = (-b - sq) / a;
        double t2 = (-b + sq) / a;

        double tHit = -1;
        if (t1 > EPS) {
            double z = o.z + t1 * d.z;
            if (z >= zMin && z <= zMax) tHit = t1;
        }
        if (tHit < 0 && t2 > EPS) {
            double z = o.z + t2 * d.z;
            if (z >= zMin && z <= zMax) tHit = t2;
        }
        if (tHit < 0) return null;

        Vector p = ray.at(tHit);
        Vector normal = new Vector((p.x - cx) / radius, (p.y - cy) / radius, 0);
        return new HitRecord(p, normal, tHit);
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public AABB getBounds() {
        return bounds;
    }
}
