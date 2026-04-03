package com.numbercruncher.rainbow.objects;

import com.numbercruncher.rainbow.*;
import com.numbercruncher.rainbow.materials.Material;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;

public class Plane implements SceneObject {
    private final Vector normal;
    private final Vector base;
    private final Material material;

    public Plane(Vector normal,Vector base,Material material){
        this.normal=normal.normalize();
        this.base=base;
        this.material=material;
    }

    /**
     * intersection between plane
     * (x-b).n=0
     * and
     * x=o+t*d
     *
     * (o+t*d-b).n=0
     * (o-b).n+t*d.n=0
     * t=(b-o).n/d.n
     *
     * @param ray
     * @return
     */
    @Override
    public HitRecord intersect(Ray ray) {
        Vector bmo = base.sub(ray.getStart());
        Vector d = ray.getDirection();
        double t = bmo.dot(normal)/d.dot(normal);

        if (t>0) return new HitRecord(ray.at(t),normal,t);
        else return null;
    }

    @Override
    public Vector getNormal(Vector point) {
        return this.normal;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

}
