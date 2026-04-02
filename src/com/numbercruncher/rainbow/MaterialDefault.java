package com.numbercruncher.rainbow;

public class MaterialDefault extends Material {
    public MaterialDefault(Color color){
        super(color);
    }

    public  RayWithAttenuation scatter(Ray incident, HitRecord hitRecord){
        Vector scatteredDirection = Vector.lambertianReflection(hitRecord.normal);
        return new RayWithAttenuation(hitRecord.point,scatteredDirection,super.getColor());
    }
    public  RayWithAttenuation transmitted(Ray incident){
        return null;
    }

}
