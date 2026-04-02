package com.numbercruncher.rainbow;

import static com.numbercruncher.rainbow.Utils.EPS;

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

    public static Vector randomUnitVector(){
        while(true) {
            Vector rnd = new Vector(2 * Math.random() - 1, 2 * Math.random() - 1, 2 * Math.random() - 1);
            if (rnd.dot(rnd) > EPS * EPS) {
                return rnd.normalize();
            }
        }
    }

    public static Vector randomUnitOnHemisphere(Vector normal){
        Vector rnd = randomUnitVector();
        return rnd.dot(normal) > 0 ? rnd : rnd.neg();
    }

    public static Vector lambertianReflection(Vector normal){
        //create random reflection
        //Vector newDirection = Vector.randomUnitOnHemisphere(record.normal);
        //create random direction weighted with cos(phi)
        //create random normal basis first

        Vector a = normal.getRandomOrthonormal();
        Vector b = a.cross(normal).normalize();
        //generate random point on the tangent unit disk

        double r = Math.sqrt(Math.random());
        double phi = 2 * Math.PI * Math.random();
        double x = r*Math.cos(phi);
        double y = r*Math.sin(phi);
        double z = Math.sqrt(1-r*r);

        //lift it up into the hemisphere
        return normal.scale(z).add(a.scale(x)).add(b.scale(y)).normalize();

    }

    /**
     * reflects like a mirror
     * adds some fuzziness
     *
     * @param normal
     * @param incoming
     * @param fuzz
     * @return
     */
    public static Vector metalReflection(Vector normal,Vector incoming,double fuzz){
        Vector trans = incoming.sub(normal.scale(normal.dot(incoming)));
        Vector out = incoming.neg().add(trans.scale(2.*(1.-fuzz)));
        return out.normalize();
    }

    public Vector getRandomOrthonormal(){
        double maxAbs = 0;
        int maxIndex = 0;
        if (Math.abs(x) > maxAbs) maxAbs = Math.abs(x);
        if (Math.abs(y) > maxAbs) {
            maxAbs = Math.abs(y);
            maxIndex = 1;
        }
        if (Math.abs(z) > maxAbs){
            maxIndex = 2;
        }
        Vector a = new Vector(0,0,0);
        if (maxIndex == 0) a.y = 1;
        else if (maxIndex==1) a.z = 1;
        else a.x = 1;
        a = a.sub(this.scale(a.dot(this)));
        return a.normalize();
    }

    public Vector cross(Vector other){
        return new Vector(y*other.z-z*other.y,z*other.x-x*other.z,x*other.y-y*other.x);
    }

    public String toString(){
        return "(" + x + "," + y + "," + z+")";
    }
}
