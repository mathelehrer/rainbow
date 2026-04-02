package com.numbercruncher.rainbow;

import static com.numbercruncher.rainbow.Utils.EPS;

public class Interval {
    public double min,max;
    public static final Interval UNIVERSE = new Interval();
    public static final Interval EMPTY = new Interval(Double.MAX_VALUE,Double.MIN_VALUE);
    public static final Interval UNIT_INTERVAL = new Interval(0,1);



    public Interval(){
        this.min=Double.MIN_VALUE;
        this.max=Double.MAX_VALUE;
    }
    public Interval(double min,double max){
        this.min=min;
        this.max=max;
    }
    public double size(){
        return max-min;
    }

    public boolean contains(double x){
        return x>=min && x<=max;
    }

    public boolean surrounds(double x){
        return x>=min-EPS && x<=max+EPS;
    }

    public void shrinkTo(double max){
        this.max=max;
    }

    public double clamp(double x){
        return Math.max(min,Math.min(max,x));
    }

}
