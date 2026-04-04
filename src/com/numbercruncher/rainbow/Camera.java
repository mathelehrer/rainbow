package com.numbercruncher.rainbow;
import com.numbercruncher.rainbow.ray_tools.Ray;
import static java.util.concurrent.ThreadLocalRandom.current;

public class Camera {
    public static final int samplesPerPixel=10000;
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

    public Ray getRay(int x, int y, double du, double dv){
        //random fluctuation for antialiasing

        double [] uv = {(x+0.5)*du,(y+0.5)*dv};
        //getFlatRandomSample(uv,du,dv);
        getLinearRandomSample(uv,du,dv);

        return new Ray(origin,
                this.lowerLeftCorner.add(this.viewportU.scale(uv[0])).add(this.viewportV.scale(uv[1])).sub(origin));
    }

    /**
     * this is a very simplistic approach, where the sample is uniformly distributed in the unit square.
     * @param uv
     * @param du
     * @param dv
     */
    private void getFlatRandomSample(double [] uv, double du, double dv){
        uv[0]+=1*(-0.5+ current().nextDouble())*du;
        uv[1]+=1*(-0.5+ current().nextDouble())*dv;
    }

    /**
     * this is a more sophisticated approach, where the sample probability drops linearly to the edge of the pixel
     *
     */
    public void getLinearRandomSample(double[] uv, double du, double dv){
       double x = current().nextDouble();  // uniform [0, 1)
       double y = current().nextDouble();

       // Invert tent (triangular) CDF on [-1, 1]:
       //   u < 0.5 → x = -1 + sqrt(2u)    (left half)
       //   u ≥ 0.5 → x =  1 - sqrt(2-2u)  (right half)
       if (x < 0.5){
           x = -1 + Math.sqrt(2 * x);
       } else {
           x = 1 - Math.sqrt(2 - 2 * x);
       }
       if (y < 0.5){
           y = -1 + Math.sqrt(2 * y);
       } else {
           y = 1 - Math.sqrt(2 - 2 * y);
       }
       uv[0] += x * du;
       uv[1] += y * dv;
    }



}
