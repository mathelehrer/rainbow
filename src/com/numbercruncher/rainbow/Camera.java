package com.numbercruncher.rainbow;


public class Camera {

    private final double aspectRatio;
    private final double height;
    private final double width;

    private final Vector viewportU ;
    private final Vector viewportV ;
    private final Vector origin;

    private final Vector lowerLeftCorner;

    public Camera(double aspectRatio, double height,double focal_length){
        this.aspectRatio=aspectRatio;
        this.height=height;
        width=height*aspectRatio;

        viewportU= new Vector(this.width,0.,0.);
        viewportV= new Vector(0.,0.,-this.height); //the minus sign accounts for the pixel convention from up to down
        this.origin= new Vector(0.,-focal_length,0.);
        this.lowerLeftCorner = new Vector().sub(viewportU.scale(0.5))
                .sub(viewportV.scale(0.5));
    }

    public Ray getRay(double u,double v){
        return new Ray(origin,
                this.lowerLeftCorner.add(this.viewportU.scale(u)).add(this.viewportV.scale(v)).sub(origin));
    }



}
