package com.numbercruncher.rainbow.materials;

import com.numbercruncher.rainbow.Color;
import com.numbercruncher.rainbow.Scene;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.ray_tools.RaySpectral;
import com.numbercruncher.rainbow.ray_tools.HitRecord;

public abstract class Material {
    protected final Color color;
    private final double emission;

    public Material(Color color){
        this(color, 0.0);
    }

    public Material(Color color, double emission){
        this.color = color;
        this.emission = emission;
    }

    public abstract RaySpectral scatter(Ray incident, HitRecord hitRecord, double lambda, Scene scene);

    public Color getColor(){
        return color;
    }

    /**
     * Spectral emission intensity. A value of 0 means no emission (pure reflector),
     * a value of 1 means full white-body emission at all wavelengths.
     */
    public double getEmission(){
        return emission;
    }

}
