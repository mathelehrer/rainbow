package com.numbercruncher.rainbow.sky;

import com.numbercruncher.rainbow.*;
import com.numbercruncher.rainbow.materials.Material;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;

public class Sky implements SceneObject {

    public Sky(){
    }

    /**
     * Spectral radiance for a given ray direction and wavelength.
     * All sky subclasses should override this for spectral rendering.
     * Default: blue-white gradient using luminance as flat spectrum.
     */
    public double getSpectralRadiance(Ray ray, double lambda) {
        double a = 0.5 * (1.0 + ray.getDirection().z);
        // Blend white (1.0) to light blue (~0.7 luminance)
        return 1.0 * (1.0 - a) + 0.7 * a;
    }

    @Override
    public HitRecord intersect(Ray ray) {
        return null;
    }

    @Override
    public Material getMaterial() {
        return null;
    }

    @Override
    public AABB getBounds() { return null; }

    public Color getColor(Ray ray){
        return Color.WHITE;
    }
}
