package com.numbercruncher.rainbow;

import java.util.function.Function;

public class MaterialLambertian extends Material {

    private Function<Vector,Color> albedoFunction;

    public MaterialLambertian(Color albedo){
        this(albedo, 0.0);
    }

    public MaterialLambertian(Color albedo, double emission){
        super(albedo, emission);
        this.albedoFunction = point -> albedo;
    }

    public MaterialLambertian(Color albedo, Function<Vector,Color> albedoFunction){
        super(albedo);
        this.albedoFunction=albedoFunction;
    }



    @Override
    public RayWithAttenuation transmitted(Ray incident) {
        return null;
    }


    @Override
    public RayWithAttenuation scatter(Ray ray,  HitRecord hitRecord){
        Color attenuation = albedoFunction.apply(hitRecord.point);

        Vector scatterDirection = Vector.lambertianReflection(hitRecord.normal);
        RayWithAttenuation scatteredRay = new RayWithAttenuation(hitRecord.point,scatterDirection,attenuation);
        return scatteredRay;

    }
}