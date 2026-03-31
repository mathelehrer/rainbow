package com.numbercruncher.rainbow;

public interface SceneObject {
    public boolean intersects(Ray ray);
    public double intersect(Ray ray);
    public Vector getNormal(Vector point);
    public Material getMaterial();
}
