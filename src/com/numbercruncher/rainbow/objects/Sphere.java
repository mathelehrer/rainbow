package com.numbercruncher.rainbow.objects;

import com.numbercruncher.rainbow.*;
import com.numbercruncher.rainbow.materials.Material;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;

public class Sphere implements SceneObject {
    private final double radius;
    private final Vector center;

    private final Material material;
    private final double EPS=1e-8;

    public Sphere(double radius,Vector center, Material material){
        this.radius=radius;
        this.center=center;
        this.material = material;
    }


     @Override public Material getMaterial() {
     return material;
     }

     /**
     * Compute the distance from the ray's origin to the nearest intersection point
     * Compute the intersection of the sphere equation with the ray equation
     * x(t) = (o + t*d)
     * sphere = (x-c)^2 = r^2
     *
     * (t*d+o-c).(t*d+o-c) = r^2
     * t^2 d.d+ 2t(o-c).d+(o-c).(o-c)-r^2=0
     *
     * t = (-b +- sqrt(b^2 - 4ac)) / 2a
     * 0 <= t <= inf
     * a = d^2=1
     * b = 2(o - c).d
     * c = (o - c).(o-c) - r^2
     *
     *
     *
     * @param ray
     * @return
     */
    @Override
    public HitRecord intersect(Ray ray) {

        Vector omc = ray.getStart().sub(center);
        Vector d = ray.getDirection();
        double b = omc.dot(d);
        double disc  =  b*b-omc.dot(omc) + radius*radius;
        if (disc<0) return null;
        double t1 = -b-Math.sqrt(disc);
        double t2 = -b+Math.sqrt(disc);
        double tSelected=-1;
        if (t1>EPS) tSelected = t1;
        else if (t2>EPS) tSelected = t2;
        if (tSelected>EPS) return new HitRecord(ray.at(tSelected),this.getNormal(ray.at(tSelected)),tSelected);
        return null;
    }

    @Override
    public Vector getNormal(Vector point) {
        return point.sub(center).normalize();
    }
}
