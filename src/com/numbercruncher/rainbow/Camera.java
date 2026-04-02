package com.numbercruncher.rainbow;


public class Camera {
    public static final int samplesPerPixel=1000;
    public static final int maxDepth = 25;

    private final double aspectRatio;
    private final double height;
    private final double width;

    private final Vector viewportU ;
    private final Vector viewportV ;
    private final Vector origin;

    private final Vector lowerLeftCorner;


    /**
     * Original constructor: camera on -y axis looking toward +y.
     */
    public Camera(double aspectRatio, double height, double focal_length){
        this(aspectRatio, height, focal_length,
                new Vector(0., -focal_length, 0.),
                new Vector(0., 1., 0.),
                new Vector(0., 0., 1.));
    }

    /**
     * Look-at constructor.
     *
     * @param aspectRatio  width/height ratio
     * @param height       viewport height in world units
     * @param focal_length distance from camera to viewport
     * @param position     camera position in world space
     * @param lookAt       point the camera looks at
     * @param worldUp      world up direction (typically (0,0,1))
     */
    public Camera(double aspectRatio, double height, double focal_length,
                  Vector position, Vector lookAt, Vector worldUp){
        this.aspectRatio = aspectRatio;
        this.height = height;
        this.width = height * aspectRatio;

        // Build orthonormal basis: forward, right, up
        Vector forward = lookAt.sub(position).normalize();
        Vector right = forward.cross(worldUp).normalize();
        Vector up = right.cross(forward).normalize();

        // Viewport spans in right (U) and -up (V, flipped for top-to-bottom pixel order)
        viewportU = right.scale(this.width);
        viewportV = up.scale(-this.height);

        this.origin = position;
        // Lower-left corner of viewport, offset by focal_length along forward
        this.lowerLeftCorner = forward.scale(focal_length)
                .sub(viewportU.scale(0.5))
                .sub(viewportV.scale(0.5));
    }

    public Ray getRay(double u,double v,double du, double dv){
        //random fluctuation for antialiasing
        u+=1*(-0.5+Math.random())*du;
        v+=1*(-0.5+Math.random())*dv;

        return new Ray(origin,
                this.lowerLeftCorner.add(this.viewportU.scale(u)).add(this.viewportV.scale(v)).sub(origin));
    }

    public Color getRayColor(Ray ray){
        return new Color(0,0,0);
    }


}
