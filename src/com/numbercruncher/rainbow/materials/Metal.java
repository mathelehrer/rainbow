package com.numbercruncher.rainbow.materials;

import com.numbercruncher.rainbow.*;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.ray_tools.RaySpectral;

import java.util.function.Function;

public class Metal extends Material {

    private Function<Vector, Color> albedoFunction;
    private double fuzzy;
    private double absorption;

    public Metal(){
        this(0,0);
    }
    public Metal(double fuzzy,double absorption){
        super(new Color(1,1,1));
        this.fuzzy=fuzzy;
        this.absorption=absorption;

        this.albedoFunction=new Function<Vector,Color>(){
            @Override
            public Color apply(Vector point) {
                return new Color(1,1,1).scale(1.-absorption);
            }
        };
    }


    public Metal(Color albedo, Function<Vector,Color> albedoFunction){
        super(albedo);
        this.albedoFunction=albedoFunction;
    }

    @Override
    public RaySpectral scatter(Ray ray, HitRecord hitRecord, double lambda, Scene scene){
        Color attenuation = albedoFunction.apply(hitRecord.point);

        Vector scatterDirection = Vector.metalReflection(hitRecord.normal,ray.getDirection(),this.fuzzy);
        double reflectance = attenuation.spectralReflectance(lambda);
        return new RaySpectral(hitRecord.point, scatterDirection, lambda, reflectance);

    }
}
