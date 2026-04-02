package com.numbercruncher.rainbow;

import java.util.function.Function;

public class MaterialMetal extends Material {

    private Function<Vector,Color> albedoFunction;
    public MaterialMetal(){
        super(new Color(1.,1.,1.));

        this.albedoFunction=new Function<Vector,Color>(){
            @Override
            public Color apply(Vector point) {
                return new Color(1.,1.,1.);
            }
        };
    }


    public MaterialMetal(Color albedo, Function<Vector,Color> albedoFunction){
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

        Vector scatterDirection = Vector.metalReflection(hitRecord.normal,ray.getDirection(),0.0);
        RayWithAttenuation scatteredRay = new RayWithAttenuation(hitRecord.point,scatterDirection,attenuation);
        return scatteredRay;

    }
}