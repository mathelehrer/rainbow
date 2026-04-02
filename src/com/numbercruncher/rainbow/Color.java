package com.numbercruncher.rainbow;

public class Color {
    public static final Color BLACK = new Color(0,0,0);
    public static final Color WHITE = new Color(1,1,1);
    public static final Color RED = new Color(1,0,0);
    public static final Color GREEN = new Color(0,1,0);
    public static final Color BLUE = new Color(0,0,1);
    public static final Color YELLOW = new Color(1,1,0);
    public static final Color CYAN = new Color(0,1,1);
    public static final Color MAGENTA = new Color(1,0,1);


    public double r,g,b;


    public Color(Vector rgb){
        this.r = rgb.x;
        this.g = rgb.y;
        this.b = rgb.z;
    }
    public Color(double r,double g,double b){
        this.r=r;
        this.g=g;
        this.b=b;
    }
    public Color(){
        this(0,0,0);
    }

    public Color add(Color other){
        return new Color(r+other.r,g+other.g,b+other.b);
    }

    public Color scale(double scalar){
        return new Color(r*scalar,g*scalar,b*scalar);
    }

    public Color mul(Color color){
        return new Color(r*color.r,g*color.g,b*color.b);
    }

    public String toString(){
        return "Color: " + r + " " + g + " " + b;
    }



}
