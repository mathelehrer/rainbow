package com.numbercruncher.rainbow;

/**
 * CIE 1931 2-degree standard observer color matching functions.
 * Data at 5nm intervals from 380nm to 780nm.
 *
 * Provides:
 * - xBar(lambda), yBar(lambda), zBar(lambda): color matching functions
 * - wavelengthToXYZ(): convert a single wavelength to XYZ tristimulus
 * - xyzToLinearRGB(): convert XYZ to linear sRGB
 * - wavelengthToColor(): full pipeline from wavelength to Color (linear RGB)
 */
public class CIE1931 {

    public static final double LAMBDA_MIN = 380.0;
    public static final double LAMBDA_MAX = 780.0;
    public static final double LAMBDA_STEP = 5.0;

    public static final double RADIANCE_NORMALIZATION = 80.;//106.86555;

    // CIE 1931 2-degree observer, 5nm steps, 380-780nm
    // Source: CIE 015:2004 (official standard)
    private static final double[] WAVELENGTHS = {
        380, 385, 390, 395, 400, 405, 410, 415, 420, 425,
        430, 435, 440, 445, 450, 455, 460, 465, 470, 475,
        480, 485, 490, 495, 500, 505, 510, 515, 520, 525,
        530, 535, 540, 545, 550, 555, 560, 565, 570, 575,
        580, 585, 590, 595, 600, 605, 610, 615, 620, 625,
        630, 635, 640, 645, 650, 655, 660, 665, 670, 675,
        680, 685, 690, 695, 700, 705, 710, 715, 720, 725,
        730, 735, 740, 745, 750, 755, 760, 765, 770, 775, 780
    };

    private static final double[] X_BAR = {
        0.0014, 0.0022, 0.0042, 0.0076, 0.0143, 0.0232, 0.0435, 0.0776, 0.1344, 0.2148,
        0.2839, 0.3285, 0.3483, 0.3481, 0.3362, 0.3187, 0.2908, 0.2511, 0.1954, 0.1421,
        0.0956, 0.0580, 0.0320, 0.0147, 0.0049, 0.0024, 0.0093, 0.0291, 0.0633, 0.1096,
        0.1655, 0.2257, 0.2904, 0.3597, 0.4334, 0.5121, 0.5945, 0.6784, 0.7621, 0.8425,
        0.9163, 0.9786, 1.0263, 1.0567, 1.0622, 1.0456, 1.0026, 0.9384, 0.8544, 0.7514,
        0.6424, 0.5419, 0.4479, 0.3608, 0.2835, 0.2187, 0.1649, 0.1212, 0.0874, 0.0636,
        0.0468, 0.0329, 0.0227, 0.0158, 0.0114, 0.0081, 0.0058, 0.0041, 0.0029, 0.0020,
        0.0014, 0.0010, 0.0007, 0.0005, 0.0003, 0.0002, 0.0002, 0.0001, 0.0001, 0.0001, 0.0000
    };

    private static final double[] Y_BAR = {
        0.0000, 0.0001, 0.0001, 0.0002, 0.0004, 0.0006, 0.0012, 0.0022, 0.0040, 0.0073,
        0.0116, 0.0168, 0.0230, 0.0298, 0.0380, 0.0480, 0.0600, 0.0739, 0.0910, 0.1126,
        0.1390, 0.1693, 0.2080, 0.2586, 0.3230, 0.4073, 0.5030, 0.6082, 0.7100, 0.7932,
        0.8620, 0.9149, 0.9540, 0.9803, 0.9950, 1.0000, 0.9950, 0.9786, 0.9520, 0.9154,
        0.8700, 0.8163, 0.7570, 0.6949, 0.6310, 0.5668, 0.5030, 0.4412, 0.3810, 0.3210,
        0.2650, 0.2170, 0.1750, 0.1382, 0.1070, 0.0816, 0.0610, 0.0446, 0.0320, 0.0232,
        0.0170, 0.0119, 0.0082, 0.0057, 0.0041, 0.0029, 0.0021, 0.0015, 0.0010, 0.0007,
        0.0005, 0.0004, 0.0002, 0.0002, 0.0001, 0.0001, 0.0001, 0.0000, 0.0000, 0.0000, 0.0000
    };

    private static final double[] Z_BAR = {
        0.0065, 0.0105, 0.0201, 0.0362, 0.0679, 0.1102, 0.2074, 0.3713, 0.6456, 1.0391,
        1.3856, 1.6230, 1.7471, 1.7826, 1.7721, 1.7441, 1.6692, 1.5281, 1.2876, 1.0419,
        0.8130, 0.6162, 0.4652, 0.3533, 0.2720, 0.2123, 0.1582, 0.1117, 0.0782, 0.0573,
        0.0422, 0.0298, 0.0203, 0.0134, 0.0087, 0.0057, 0.0039, 0.0027, 0.0021, 0.0018,
        0.0017, 0.0014, 0.0011, 0.0010, 0.0008, 0.0006, 0.0003, 0.0002, 0.0002, 0.0001,
        0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000,
        0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000,
        0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000
    };

    /**
     * Linearly interpolate the x_bar color matching function at the given wavelength.
     */
    public static double xBar(double lambda) {
        return interpolate(lambda, X_BAR);
    }

    /**
     * Linearly interpolate the y_bar color matching function at the given wavelength.
     */
    public static double yBar(double lambda) {
        return interpolate(lambda, Y_BAR);
    }

    /**
     * Linearly interpolate the z_bar color matching function at the given wavelength.
     */
    public static double zBar(double lambda) {
        return interpolate(lambda, Z_BAR);
    }

    private static double interpolate(double lambda, double[] data) {
        if (lambda < LAMBDA_MIN || lambda > LAMBDA_MAX) return 0.0;
        double idx = (lambda - LAMBDA_MIN) / LAMBDA_STEP;
        int i = (int) idx;
        if (i >= data.length - 1) return data[data.length - 1];
        double t = idx - i;
        return data[i] * (1.0 - t) + data[i + 1] * t;
    }

    /**
     * Convert XYZ tristimulus values to linear sRGB.
     * Uses the standard sRGB D65 matrix (IEC 61966-2-1).
     */
    public static Color xyzToLinearRGB(double X, double Y, double Z) {
        double r =  3.2406 * X - 1.5372 * Y - 0.4986 * Z;
        double g = -0.9689 * X + 1.8758 * Y + 0.0415 * Z;
        double b =  0.0557 * X - 0.2040 * Y + 1.0570 * Z;
        return new Color(r, g, b);
    }

    /**
     * Convert a monochromatic wavelength to a linear sRGB Color.
     * The result may have negative components (out of gamut); callers should clamp.
     *
     * @param lambda wavelength in nm (380-780)
     * @return Color in linear sRGB space
     */
    public static Color wavelengthToColor(double lambda) {
        double X = xBar(lambda);
        double Y = yBar(lambda);
        double Z = zBar(lambda);
        return xyzToLinearRGB(X, Y, Z);
    }

    /**
     * Get the number of tabulated wavelength entries.
     */
    public static int size() {
        return WAVELENGTHS.length;
    }

    /**
     * Get the wavelength at a given table index.
     */
    public static double getWavelength(int index) {
        return WAVELENGTHS[index];
    }

    public static void main(String[] args) {
        //compute normalization
        double ySum = 0.0;
        for (double y: Y_BAR) {
            ySum += y;
        }
        double normalization = LAMBDA_STEP*ySum;
        System.out.println(normalization);
    }
}
