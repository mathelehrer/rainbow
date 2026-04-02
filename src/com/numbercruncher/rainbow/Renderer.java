package com.numbercruncher.rainbow;

import static com.numbercruncher.rainbow.CIE1931.RADIANCE_NORMALIZATION;

public class Renderer {
    private final Camera camera;
    private int width,height;
    private PPMImage image;
    private Scene scene;
    private final int factor = 3;

    public Renderer(Scene scene){
        this(scene, new Camera(16./9, 2, 0.5,
                new Vector(0, -2, 0.5),   // position: behind and above ground
                new Vector(0, 1, 0),       // look at center of scene
                new Vector(0, 0, 1)));     // world up = +z
    }

    public Renderer(Scene scene, Camera camera){
        this.camera = camera;
        this.width = 192*factor;
        this.height = 108*factor;
        this.image = new PPMImage(this.width,this.height,255);
        this.scene = scene;
    }



    /**
     * setup renderer with default scene
     */
    public Renderer(){
        this(Scene.DEFAULT_SCENE);
    }

    private Color rayColor(Ray ray,int depth){
        HitRecord record = scene.intersect(ray,new Interval(0.0001,Double.MAX_VALUE));
        if (record==null || record.objectIndex==-1) {
            //sample background
            return scene.getSky().getColor(ray);
        }
        else {
            if (depth >= this.camera.maxDepth) {
                return Color.BLACK;
            }
            else{

                Material mat  = scene.getObjects().get(record.objectIndex).getMaterial();
                RayWithAttenuation scatter = mat.scatter(ray, record);
                Vector normal = record.normal;

                return scatter.getAttenuation().mul(rayColor(new Ray(record.point, scatter.getDirection()), depth + 1))
                        .scale(0.5);
            }
        }

    }

    /**
     * Spectral ray color: trace a ray for a single wavelength.
     * Returns the spectral radiance (scalar) that arrives at the camera
     * for this wavelength along this ray path.
     *
     * For now, only the sky emits light spectrally. Objects attenuate
     * uniformly (grayscale) — wavelength-dependent materials come in step 2.
     */
    private Radiance spectralRayRadiance(Ray ray, double lambda, int depth) {
        HitRecord record = scene.intersect(ray, new Interval(0.0001, Double.MAX_VALUE));
        if (record == null || record.objectIndex == -1) {
            // Sky emission
            Sky sky = scene.getSky();
            if (sky instanceof SpectralSky) {
                return ((SpectralSky) sky).getSpectralRadiance(ray, lambda);
            } else if (sky instanceof CheckerSky) {
                return ((CheckerSky) sky).getSpectralRadiance(ray, lambda);
            }
            // Fallback: use luminance of RGB sky color
            Color c = sky.getColor(ray);
            return new Radiance(0.2126 * c.r + 0.7152 * c.g + 0.0722 * c.b);
        }
        if (depth >= this.camera.maxDepth) {
            return Radiance.ZERO;
        }
        Material mat = scene.getObjects().get(record.objectIndex).getMaterial();
        // Emission term: material emits light uniformly across all wavelengths
        double emitted = mat.getEmission();
        RayWithAttenuation scatter = mat.scatter(ray, record, lambda);
        // Use average of attenuation RGB as wavelength-independent reflectance
        Color att = scatter.getAttenuation();
        Radiance reflectance = new Radiance((att.r + att.g + att.b) / 3.0);
        Radiance reflected = reflectance.combine(spectralRayRadiance(
                new Ray(record.point, scatter.getDirection()), lambda, depth + 1).diminish(0.5));
        // L = emission + albedo * L_incoming
        return new Radiance(emitted + reflected.value);
    }

    public void render(String filename){
        Color[] colors = new Color[this.width*this.height];
        double hm = 1./this.height;
        double wm = 1./this.width;
        for (int y = 0; y< this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                Color pixelColor = new Color(0,0,0);
                for (int sample =0;sample<camera.samplesPerPixel;sample++){
                    Ray ray = this.camera.getRay(x*wm,y*hm,wm,hm);
                    pixelColor=pixelColor.add(rayColor(ray,0));
                }
                colors[y*width+x] = pixelColor.scale(1./camera.samplesPerPixel);
            }
        }
        this.image.create_from_color(colors,filename);
    }

    /**
     * Spectral render: for each pixel, sample random wavelengths,
     * trace each wavelength independently, accumulate XYZ tristimulus
     * values, then convert to sRGB.
     *
     * This produces physically correct color from spectral data.
     */
    public void renderSpectral(String filename) {
        Color[] colors = new Color[this.width * this.height];
        double hm = 1.0 / this.height;
        double wm = 1.0 / this.width;

        // We sample wavelengths uniformly in [380, 780].
        // The XYZ integral is:  X = integral( L(lambda) * xBar(lambda) dlambda )
        // Monte Carlo estimate: X ≈ (lambdaRange / N) * sum( L(lambda_i) * xBar(lambda_i) )
        double lambdaRange = CIE1931.LAMBDA_MAX - CIE1931.LAMBDA_MIN;

        for (int y = 0; y < this.height; y++) {
            if (y % 50 == 0) {
                System.out.println("Spectral render: row " + y + "/" + this.height);
            }
            for (int x = 0; x < this.width; x++) {
                double X = 0, Y = 0, Z = 0;
                int totalSamples = camera.samplesPerPixel;

                for (int sample = 0; sample < totalSamples; sample++) {
                    Ray ray = this.camera.getRay(x * wm, y * hm, wm, hm);
                    // Pick a random wavelength
                    double lambda = CIE1931.LAMBDA_MIN + Math.random() * lambdaRange;
                    Radiance l = spectralRayRadiance(ray, lambda, 0);
                    double nl = l.value / RADIANCE_NORMALIZATION;
                    // Accumulate XYZ weighted by color matching functions
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
        }

        this.image.create_from_color(colors, filename);
    }

    public void renderTestScene(){
        Color[] colors = new Color[this.width*this.height];
        double hm = 1./this.height;
        double wm = 1./this.width;
        for (int y = 0; y< this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                Ray ray = this.camera.getRay(x*wm,y*hm,wm,hm);
                colors[y*width+x]=new Color( (float) (y * hm), (float) (x * wm),(float) (1.-0.5*y*hm-0.5*x*wm));
            }

        }
        this.image.create_from_color(colors,"test_image2");
    }
}
