package com.numbercruncher.rainbow.ray_tools;

import com.numbercruncher.rainbow.Vector;

public class HitRecord {
    public Vector point;
    public Vector normal;
    public double t;
    public int objectIndex;

    public HitRecord(Vector point, Vector normal, double t, int objectIndex){
        this.point=point;
        this.normal=normal;
        this.t=t;
        this.objectIndex=objectIndex;
    }

    /**
     *
     * @param point
     * @param normal
     * @param t
     */
    public HitRecord(Vector point, Vector normal, double t){
        this(point,normal,t,-1);
    }
}