package com.numbercruncher.rainbow;

public class Color {
    public float r,g,b;
    public Color(float r,float g,float b){
        this.r=r;
        this.g=g;
        this.b=b;
    }
    public Color(){
        this(0,0,0);
    }

    public String toString(){
        return "Color: " + r + " " + g + " " + b;
    }



}
