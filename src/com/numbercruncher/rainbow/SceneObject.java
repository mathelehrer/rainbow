package com.numbercruncher.rainbow;

import com.numbercruncher.rainbow.materials.Material;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;

public interface SceneObject {
    HitRecord intersect(Ray ray);
    Material getMaterial();
    /** Returns the axis-aligned bounding box, or null for unbounded objects (e.g. Plane). */
    AABB getBounds();
}
