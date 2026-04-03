package com.numbercruncher.rainbow.ray_tools;

import com.numbercruncher.rainbow.Vector;

public class RaySpectral extends Ray {

    private final double wavelength;
    private double throughput;
    private double radiance;

    public RaySpectral(Vector start, Vector direction, double wavelength) {
        super(start, direction);
        this.wavelength = wavelength;
        this.throughput = 1;
        this.radiance = 0;
    }

    public RaySpectral(Vector start, Vector direction, double wavelength, double throughput) {
        super(start, direction);
        this.wavelength = wavelength;
        this.throughput = throughput;
        this.radiance = 0;
    }

    public RaySpectral(Ray ray, double wavelength, double throughput, double radiance) {
        super(ray.getStart(), ray.getDirection());
        this.wavelength = wavelength;
        this.throughput = throughput;
        this.radiance = radiance;
    }

    public double getWavelength() {
        return wavelength;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    public double getRadiance() {
        return radiance;
    }

    public void setRadiance(double radiance) {
        this.radiance = radiance;
    }

    @Override
    public String toString() {
        return "RaySpectral: from " + getStart() + " in direction " + getDirection() +
                " [λ=" + wavelength + " nm, throughput=" + throughput + ", radiance=" + radiance + "]";
    }
}
