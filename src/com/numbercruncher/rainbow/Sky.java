package com.numbercruncher.rainbow;

public class Sky implements SceneObject{


    public Sky(){

    }

    public Color getColor(Ray ray){
        double a =0.5* (1.+ray.getDirection().y) ;
        return new Color(new Vector(1.,1.,1.).scale(1.0-a).add(new Vector(0.5,0.7,1.0).scale(a)));
    }

    @Override
    public HitRecord intersect(Ray ray) {
        return null;
    }

    @Override
    public Vector getNormal(Vector point) {
        return null;
    }

    @Override
    public Material getMaterial() {
        return null;
    }
}
