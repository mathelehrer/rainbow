package com.numbercruncher.rainbow;

public class Sphere implements SceneObject{
    private final double radius;
    private final Vector center;

    private final Material material;
    private final double EPS=1e-8;

    public Sphere(double radius,Vector center, Material material){
        this.radius=radius;
        this.center=center;
        this.material = material;
    }

    /**
     * Just checks the discriminant of the quadratic equation
     *
     * ((o-c).d)^2 -(o-c).(o-c)+r^2 >0
     * @param ray
     * @return
     */
    @Override
    public boolean intersects(Ray ray) {
        Vector omc = ray.getStart().sub(center);
        Vector d = ray.getDirection();
        double b = omc.dot(d);
        return  b*b-omc.dot(omc) + radius*radius>0;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    /**
     * Compute the distance from the ray's origin to the nearest intersection point
     * Compute the intersection of the sphere equation with the ray equation
     * x(t) = (o + t*d)
     * sphere = (x-c)^2 = r^2
     *
     * (t*d+o-c).(t*d+o-c) = r^2
     * t^2 d.d+ 2t(o-c).d+(o-c).(o-c)-r^2=0
     *
     * t = (-b +- sqrt(b^2 - 4ac)) / 2a
     * 0 <= t <= inf
     * a = d^2=1
     * b = 2(o - c).d
     * c = (o - c).(o-c) - r^2
     *
     *
     *
     * @param ray
     * @return
     */
    @Override
    public double intersect(Ray ray) {

        Vector omc = ray.getStart().sub(center);
        Vector d = ray.getDirection();
        double b = omc.dot(d);
        double disc  =  b*b-omc.dot(omc) + radius*radius;
        double t1 = -b-Math.sqrt(disc);
        double t2 = -b+Math.sqrt(disc);
        if (t1>EPS) return t1;
        if (t2>EPS) return t2;
        return -1;
    }

    @Override
    public Vector getNormal(Vector point) {
        return point.sub(center).normalize();
    }
}
