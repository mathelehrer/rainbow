package com.numbercruncher.rainbow;

public class Color {
    public static final Color BLACK = new Color(0,0,0);
    public static final Color WHITE = new Color(1,1,1);
    public static final Color RED = new Color(1,0.5,0.5);
    public static final Color GREEN = new Color(0,1,0);
    public static final Color GRAY = new Color(0.5,.5,0.5);
    public static final Color BLUE = new Color(0,0,1);
    public static final Color YELLOW = new Color(1,1,0);
    public static final Color CYAN = new Color(0,1,1);
    public static final Color MAGENTA = new Color(1,0,1);


    public double r,g,b;


    public Color(Vector rgb){
        this.r = rgb.x;
        this.g = rgb.y;
        this.b = rgb.z;
    }
    public Color(double r,double g,double b){
        this.r=r;
        this.g=g;
        this.b=b;
    }
    public Color(){
        this(0,0,0);
    }

    public Color add(Color other){
        return new Color(r+other.r,g+other.g,b+other.b);
    }

    public Color scale(double scalar){
        return new Color(r*scalar,g*scalar,b*scalar);
    }

    public Color mul(Color color){
        return new Color(r*color.r,g*color.g,b*color.b);
    }

    public String toString(){
        return "Color: " + r + " " + g + " " + b;
    }

    /**
     * Convert this RGB color to a spectral reflectance at the given wavelength.
     * Uses Gaussian basis functions centered on the sRGB primary wavelengths:
     *   R ~ 630nm, G ~ 532nm, B ~ 465nm
     *
     * Normalized so that Color(1,1,1) (white) returns ~1.0 at all wavelengths.
     * A red surface reflects strongly at 630nm and weakly at 465nm, etc.
     *
     * @param lambda wavelength in nm (380-780)
     * @return spectral reflectance in [0, ~1] (may slightly exceed 1 for saturated colors)
     */
    public double spectralReflectance(double lambda) {
        // Narrower Gaussians (sigma=35) preserve color saturation much better.
        // With sigma~55, the basis overlap so much that Color(0.3,0.5,0.2) produces
        // a nearly flat spectrum (beige). With sigma=35, green/red ratio is 1.6:1
        // instead of 1.2:1, giving visibly green surfaces under white light.
        double rBasis = gaussian(lambda, 630, 35);
        double gBasis = gaussian(lambda, 532, 35);
        double bBasis = gaussian(lambda, 465, 35);

        double white = rBasis + gBasis + bBasis;
        if (white < 1e-6) return 0.0;

        return Math.max(0, (r * rBasis + g * gBasis + b * bBasis) / white);
    }

    private static double gaussian(double x, double mean, double sigma) {
        double d = x - mean;
        return Math.exp(-0.5 * d * d / (sigma * sigma));
    }

}
