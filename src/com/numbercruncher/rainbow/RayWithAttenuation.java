package com.numbercruncher.rainbow;

public class RayWithAttenuation extends Ray{

    private Color attenuation;
    public RayWithAttenuation(Ray ray, Color attenuation){
        super(ray.getStart(),ray.getDirection());
        this.attenuation=attenuation;
    }

    public RayWithAttenuation(Vector start, Vector direction, Color attenuation){
        super(start,direction);
        this.attenuation=attenuation;
    }

    public Color getAttenuation(){
        return attenuation;
    }
}
