package com.numbercruncher.rainbow;

public class Vector {
    public double x,y,z;

    public Vector(){
        x=y=z=0.;
    }
    public Vector(double x,double y,double z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    public Vector(Double... values){
        this.x=values[0];
        this.y=values[1];
        this.z=values[2];
    }

    public Vector add(Vector other){
        return new Vector(this.x+other.x,this.y+other.y,this.z+other.z);
    }

    public Vector neg(){
        return new Vector(-this.x,-this.y,-this.z);
    }

    public Vector sub(Vector other){
        return this.add(other.neg());
    }

    public double dot(Vector other){
        return this.x*other.x+this.y*other.y+this.z*other.z;
    }

    public double length(){
        return Math.sqrt(this.dot(this));
    }

    public Vector normalize(){
        return this.scale(1./this.length());
    }

    public Vector scale(double scalar){
        return new Vector(this.x*scalar,this.y*scalar,this.z*scalar);
    }

    public String toString(){
        return "(" + x + "," + y + "," + z+")";
    }
}
