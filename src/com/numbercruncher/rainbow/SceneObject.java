package com.numbercruncher.rainbow;

public interface SceneObject {
    public HitRecord intersect(Ray ray);
    public Vector getNormal(Vector point);
    public Material getMaterial();
}
