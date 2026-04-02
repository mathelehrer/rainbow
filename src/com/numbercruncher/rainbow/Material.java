package com.numbercruncher.rainbow;

public abstract class Material {
    private final Color color;
    private final double emission;

    public Material(Color color){
        this(color, 0.0);
    }

    public Material(Color color, double emission){
        this.color = color;
        this.emission = emission;
    }

    public abstract RayWithAttenuation scatter(Ray incident, HitRecord hitRecord);

    /**
     * Wavelength-aware scatter. Override for dispersive materials (glass).
     * Default delegates to the wavelength-independent scatter.
     *
     * @param lambda wavelength in nm (380-780)
     */
    public RayWithAttenuation scatter(Ray incident, HitRecord hitRecord, double lambda) {
        return scatter(incident, hitRecord);
    }

    public abstract RayWithAttenuation transmitted(Ray incident);

    public Color getColor(){
        return color;
    }

    /**
     * Spectral emission intensity. A value of 0 means no emission (pure reflector),
     * a value of 1 means full white-body emission at all wavelengths.
     */
    public double getEmission(){
        return emission;
    }

}
