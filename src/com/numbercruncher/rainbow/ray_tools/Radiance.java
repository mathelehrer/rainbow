package com.numbercruncher.rainbow.ray_tools;

public class Radiance {
    public static final Radiance ZERO = new Radiance(0);

    public double value;

    /**
     * This is just a container for readability
     * to separate the value of the radiance from a simple double variable
     * @param value
     */
    public Radiance(double value){
        this.value = value;
    }

    public Radiance combine(Radiance other){
        return new Radiance(this.value*other.value);
    }

    public Radiance diminish(double factor){
        return new Radiance(this.value*factor);
    }
}
