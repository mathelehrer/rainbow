package com.numbercruncher.rainbow.sky;

import com.numbercruncher.rainbow.Color;
import com.numbercruncher.rainbow.ray_tools.Radiance;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.Vector;

/**
 * A sky with a black and white checkerboard pattern.
 * Uses planar projection onto a virtual plane at y=1 in front of the camera,
 * so tiles have equal world-space size and appear smaller toward the edges,
 * giving the appearance of an infinite grid.
 */
public class SkyChecker extends Sky {

    private final double tileSize; // world-space size of each checker tile

    public SkyChecker() {
        this(0.5);
    }

    public SkyChecker(double tileSize) {
        this.tileSize = tileSize;
    }

    @Override
    public Color getColor(Ray ray) {
        Vector d = ray.getDirection();
        // Project onto plane at y=1: (u, v) = (dx/dy, dz/dy)
        // For rays going backward (dy <= 0), use a solid color to avoid artifacts
        if (d.y <= 0.001) return Color.BLACK;
        double u = d.x / d.y;
        double v = d.z / d.y;

        int cu = (int) Math.floor(u / tileSize);
        int cv = (int) Math.floor(v / tileSize);

        boolean isWhite = (cu + cv) % 2 == 0;
        return isWhite ? Color.WHITE : Color.BLACK;
    }

    /**
     * Spectral radiance: white tiles emit 1.0, black tiles emit 0.0
     * (flat across all wavelengths).
     */
    public Radiance getSpectralRadiance(Ray ray, double lambda) {
        Color c = getColor(ray);
        return new Radiance(c.r); // 0 or 1
    }
}