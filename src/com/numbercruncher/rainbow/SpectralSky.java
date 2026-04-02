package com.numbercruncher.rainbow;

/**
 * A sky that displays the full visible spectrum horizontally.
 * The horizontal direction of the ray maps linearly to wavelength:
 *   left edge  (x = -1) -> 380 nm (violet)
 *   right edge (x = +1) -> 780 nm (red)
 *
 * For spectral rendering: given a wavelength, returns the spectral
 * radiance (a scalar) at the sky point the ray hits.
 *
 * For RGB preview: converts the mapped wavelength to an RGB color
 * via the CIE 1931 color matching functions.
 */
public class SpectralSky extends Sky {

    private final double lambdaMin;
    private final double lambdaMax;

    public SpectralSky() {
        this(CIE1931.LAMBDA_MIN, CIE1931.LAMBDA_MAX);
    }

    public SpectralSky(double lambdaMin, double lambdaMax) {
        this.lambdaMin = lambdaMin;
        this.lambdaMax = lambdaMax;
    }

    /**
     * Map a ray direction to a wavelength.
     * Uses the azimuthal angle (horizontal angle) so that iso-wavelength
     * lines appear as vertical bands regardless of the vertical ray component.
     * The camera looks in +y, so the horizontal angle is atan2(dx, dy).
     */
    public double directionToWavelength(Ray ray) {
        Vector d = ray.getDirection();
        double angle = Math.atan2(d.x, d.y); // range [-pi, pi]
        double t = 0.5 * (3*angle / Math.PI + 1.0); // map [-pi,pi] to [0,1]
        return lambdaMin + t * (lambdaMax - lambdaMin);
    }

    /**
     * Get the spectral radiance of the sky for a given ray and wavelength.
     * The sky emits light only at the wavelength that matches the ray's
     * horizontal direction. We use a Gaussian peak centered on the
     * "sky wavelength" so that nearby wavelengths still contribute,
     * producing smooth color bands.
     *
     * @param ray the ray direction
     * @param lambda the wavelength being traced (nm)
     * @return spectral radiance (scalar, dimensionless for now)
     */
    public Radiance getSpectralRadiance(Ray ray, double lambda) {
        double skyLambda = directionToWavelength(ray);
        // Gaussian bandwidth controls how "pure" each color band is.
        // sigma=10nm -> nearly monochromatic, sharp gamut-clipping edges
        // sigma=30nm -> smooth blending between neighboring hues
        double sigma = 50.0;
        double diff = lambda - skyLambda;
        return new Radiance(Math.exp(-0.5 * diff * diff / (sigma * sigma)));
    }

    /**
     * RGB fallback: convert the sky's wavelength directly to an RGB color.
     * Useful for non-spectral rendering or previews.
     */
    @Override
    public Color getColor(Ray ray) {
        double lambda = directionToWavelength(ray);
        Color c = CIE1931.wavelengthToColor(lambda);
        // Clamp negatives (out-of-gamut wavelengths) and normalize brightness
        double scale = 1.5; // boost brightness
        double r = Math.max(0, c.r * scale);
        double g = Math.max(0, c.g * scale);
        double b = Math.max(0, c.b * scale);
        return new Color(r, g, b);
    }
}
