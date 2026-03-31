package com.numbercruncher.rainbow;

public class Sky {


    public Sky(){

    }

    public Color getColor(Ray ray){
        double skyHeight =0.5* (1+ray.getDirection().z) ;
        return new Color(skyHeight/3,skyHeight/3,skyHeight);
    }

}
