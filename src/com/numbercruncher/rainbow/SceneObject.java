package com.numbercruncher.rainbow;

import com.numbercruncher.rainbow.materials.Material;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;

public interface SceneObject {
    public HitRecord intersect(Ray ray);
    public Vector getNormal(Vector point);
    public Material getMaterial();
}
