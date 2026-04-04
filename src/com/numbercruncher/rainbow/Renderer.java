package com.numbercruncher.rainbow;

import com.numbercruncher.rainbow.materials.Material;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;
import com.numbercruncher.rainbow.ray_tools.RaySpectral;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.numbercruncher.rainbow.CIE1931.RADIANCE_NORMALIZATION;



public class Renderer {
    public static final Camera DEFAULT_CAMERA  =new Camera(16./9, 2, 1,
            new Vector(0, -4, 0.5),   // origin
            new Vector(0, 1, 0),       // look at center of scene
            new Vector(0, 0, 1));
    private Camera camera;
    private int width,height;
    private PPMImage image;
    private Scene scene;
    private int factor;


    public Renderer(Scene scene,Camera camera, int factor){
        this.scene = scene;
        this.camera = camera;
        this.factor = factor;
        this.width = 48*factor;
        this.height = 27*factor;
        this.image = new PPMImage(this.width,this.height,255);
    }

    public Renderer(Scene scene, int factor){
        this(scene,DEFAULT_CAMERA,factor);
    }
    public Renderer(Scene scene){
        this(scene,DEFAULT_CAMERA, 40);
    }

    public Renderer(Scene scene, Camera camera){
        this(scene,camera,40);
    }



    /**
     * setup renderer with default scene
     */
    public Renderer(){
        this(Scene.DEFAULT_SCENE);
    }

    /**
     * Trace a ray for a single wavelength.
     * Returns the spectral radiance (scalar) that arrives at the camera
     * for this wavelength along this ray path.
     *
     * @param excludeSunFromSky if true, the sun disk is excluded when the ray
     *                          misses all objects (avoids double-counting with NEE)
     */
    private double rayRadiance(Ray ray, double lambda, int depth, boolean excludeSunFromSky) {
        HitRecord record = scene.intersect(ray, new Interval(0.0001, Double.MAX_VALUE));
        if (record == null || record.objectIndex == -1) {
            return scene.getSky().getSpectralRadiance(ray, lambda, excludeSunFromSky);
        }
        if (depth >= Camera.maxDepth) {
            return 0.0;
        }
        Material mat = scene.getObjects().get(record.objectIndex).getMaterial();
        RaySpectral scatter = mat.scatter(ray, record, lambda, scene);
        double local = scatter.getRadiance();
        // If this material uses NEE (direct sun sampling), exclude the sun
        // from the sky for the bounce ray to avoid double-counting.
        boolean nextExcludeSun = mat.usesDirectLightSampling();
        double reflected = scatter.getThroughput() * rayRadiance(
                new Ray(record.point, scatter.getDirection()), lambda, depth + 1, nextExcludeSun);
        return local + reflected;
    }

    /**
     * Spectral render: for each pixel, sample random wavelengths,
     * trace each wavelength independently, accumulate XYZ tristimulus
     * values, then convert to sRGB.
     *
     * This produces physically correct color from spectral data.
     */
    public void render(String filename) {
        Color[] colors = new Color[this.width * this.height];
        double pixelHeight = 1.0 / this.height;
        double pixelWidth = 1.0 / this.width;

        // We sample wavelengths uniformly in [380, 780].
        // The XYZ integral is:  X = integral( L(lambda) * xBar(lambda) dlambda )
        // Monte Carlo estimate: X ≈ (lambdaRange / N) * sum( L(lambda_i) * xBar(lambda_i) )
        double lambdaRange = CIE1931.LAMBDA_MAX - CIE1931.LAMBDA_MIN;
        AtomicInteger progress = new AtomicInteger(0);

        IntStream.range(0, this.height).parallel().forEach(y -> {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (int x = 0; x < this.width; x++) {
                double X = 0, Y = 0, Z = 0;
                int totalSamples = camera.samplesPerPixel;

                for (int sample = 0; sample < totalSamples; sample++) {
                    Ray ray = this.camera.getRay(x,y, pixelWidth, pixelHeight);
                    double lambda = CIE1931.LAMBDA_MIN + rng.nextDouble() * lambdaRange;
                    double nl = rayRadiance(ray, lambda, 0, false) / RADIANCE_NORMALIZATION;
                    X += nl * CIE1931.xBar(lambda);
                    Y += nl * CIE1931.yBar(lambda);
                    Z += nl * CIE1931.zBar(lambda);
                }

                // Monte Carlo normalization: (lambdaRange / totalSamples)
                double norm = lambdaRange / totalSamples;
                X *= norm;
                Y *= norm;
                Z *= norm;

                // Convert XYZ to linear sRGB
                Color rgb = CIE1931.xyzToLinearRGB(X, Y, Z);

                // Clamp negative values (out of gamut)
                colors[y * width + x] = new Color(
                        Math.max(0, rgb.r),
                        Math.max(0, rgb.g),
                        Math.max(0, rgb.b)
                );
            }
            int done = progress.incrementAndGet();
            if (done % 50 == 0) System.out.println("Render: row " + done + "/" + height);
        });

        this.image.create_from_color(colors, filename);
    }


    /**
     * unphysical test scene
     *
     */
    public void renderTestScene(){
        Color[] colors = new Color[this.width*this.height];
        double pixelHeight = 1./this.height;
        double pixelWidth = 1./this.width;
        for (int y = 0; y< this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                Ray ray = this.camera.getRay(x,y,pixelWidth,pixelHeight);
                colors[y*width+x]=new Color( (float) (y * pixelHeight), (float) (x * pixelWidth),(float) (1.-0.5*y*pixelHeight-0.5*x*pixelWidth));
            }

        }
        this.image.create_from_color(colors,"test_image_gradient");
    }
}
