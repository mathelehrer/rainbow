package com.numbercruncher.rainbow;

/**
 * Represents a ray in 3D space.
 */
public class Ray {

    private final Vector start;
    private final Vector direction;

    /**
     * Creates a ray.
     * makes sure that the direction is normalized
     * @param start
     * @param direction
     */
    public Ray(Vector start, Vector direction){
        this.start = start;
        this. direction = direction.scale(1/direction.length());
    }

    public Vector getStart() {
        return start;
    }

    public Vector getDirection() {
        return direction;
    }

    public Vector at(double t){
        return start.add(direction.scale(t));
    }

    public String toString(){
        return "Ray: from " + start + " in direction " + direction;
    }
}
