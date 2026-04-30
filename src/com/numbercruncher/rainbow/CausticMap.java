package com.numbercruncher.rainbow;

/**
 * 2D + spectral grid storing incoming irradiance per wavelength on a flat
 * receiver (a horizontal plane in xy). Used as a caustic photon map: a
 * forward photon trace from the light deposits flux into the appropriate
 * (x, y, λ) bin; backward path tracing then queries the bin as an extra
 * incoming-irradiance term for Lambertian scatter.
 *
 * Stored value units: W / (m² · nm) — i.e. spectral irradiance.
 * This integrates cleanly with Lambertian's outgoing radiance:
 *    L_out(λ) = ρ(λ)/π · E(x, y, λ)
 *
 * The grid is finite; samples outside the (x, y) or λ range return 0.
 */
public class CausticMap {
    private final double xMin, yMin, lambdaMin;
    private final double dx, dy, dlambda;
    private final int xBins, yBins, lambdaBins;
    private final double cellArea;
    private final double[] data;

    public CausticMap(double xMin, double xMax, double yMin, double yMax,
                      double lambdaMin, double lambdaMax,
                      int xBins, int yBins, int lambdaBins) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.lambdaMin = lambdaMin;
        this.dx = (xMax - xMin) / xBins;
        this.dy = (yMax - yMin) / yBins;
        this.dlambda = (lambdaMax - lambdaMin) / lambdaBins;
        this.xBins = xBins;
        this.yBins = yBins;
        this.lambdaBins = lambdaBins;
        this.cellArea = dx * dy;
        this.data = new double[xBins * yBins * lambdaBins];
    }

    /**
     * Deposit a photon's flux Φ (Watts) at (x, y, λ). Stored value is converted
     * to spectral irradiance by dividing by cell area and λ-bin width.
     */
    public void deposit(double x, double y, double lambda, double flux) {
        int ix = (int) Math.floor((x - xMin) / dx);
        int iy = (int) Math.floor((y - yMin) / dy);
        int il = (int) Math.floor((lambda - lambdaMin) / dlambda);
        if (ix < 0 || ix >= xBins || iy < 0 || iy >= yBins) return;
        if (il < 0 || il >= lambdaBins) return;
        int idx = (ix * yBins + iy) * lambdaBins + il;
        // Synchronized add for thread-safety when photon shooting is parallel.
        synchronized (data) {
            data[idx] += flux / (cellArea * dlambda);
        }
    }

    /**
     * Query spectral irradiance at (x, y, λ). Bilinear interpolation in
     * (x, y) — smooths bin-boundary discontinuities — and nearest-neighbour
     * in λ. Returns 0 outside the grid.
     */
    public double sample(double x, double y, double lambda) {
        int il = (int) Math.floor((lambda - lambdaMin) / dlambda);
        if (il < 0 || il >= lambdaBins) return 0;

        // Bin centres are at xMin + (i+0.5)*dx; bilinear weights between
        // neighbouring centres in x and y.
        double fx = (x - xMin) / dx - 0.5;
        double fy = (y - yMin) / dy - 0.5;
        int ix = (int) Math.floor(fx);
        int iy = (int) Math.floor(fy);
        double tx = fx - ix;
        double ty = fy - iy;

        double v00 = at(ix,     iy,     il);
        double v10 = at(ix + 1, iy,     il);
        double v01 = at(ix,     iy + 1, il);
        double v11 = at(ix + 1, iy + 1, il);

        return (1 - tx) * (1 - ty) * v00
             +      tx  * (1 - ty) * v10
             + (1 - tx) *      ty  * v01
             +      tx  *      ty  * v11;
    }

    private double at(int ix, int iy, int il) {
        if (ix < 0 || ix >= xBins || iy < 0 || iy >= yBins) return 0;
        return data[(ix * yBins + iy) * lambdaBins + il];
    }
}
