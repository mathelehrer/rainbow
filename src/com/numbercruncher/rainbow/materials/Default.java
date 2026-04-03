package com.numbercruncher.rainbow.materials;

import com.numbercruncher.rainbow.*;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.ray_tools.RaySpectral;

public class Default extends Material {
    public Default(Color color){
        super(color);
    }

    @Override
    public RaySpectral scatter(Ray incident, HitRecord hitRecord, double lambda, Scene scene){
        Vector normal = hitRecord.normal;
        if (incident.getDirection().dot(normal) > 0) normal = normal.neg();
        Vector scatteredDirection = Vector.lambertianReflection(normal);
        double reflectance = color.spectralReflectance(lambda);
        return new RaySpectral(hitRecord.point, scatteredDirection, lambda, reflectance);
    }

}
