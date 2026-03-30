package com.numbercruncher.rainbow;

/**
 * Represents a ray in 3D space.
 */
public class Ray {

    private final Vector start;
    private final Vector direction;


    public Ray(Vector start, Vector direction){
        this.start = start;
        this. direction = direction;
    }

    public Vector getStart() {
        return start;
    }

    public Vector getDirection() {
        return direction;
    }

    public String toString(){
        return "Ray: " + start + " " + direction;
    }
}
