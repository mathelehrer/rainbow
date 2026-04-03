package com.numbercruncher.rainbow;

import com.numbercruncher.rainbow.materials.Default;
import com.numbercruncher.rainbow.materials.Glass;
import com.numbercruncher.rainbow.materials.Lambertian;
import com.numbercruncher.rainbow.materials.Metal;
import com.numbercruncher.rainbow.objects.Cube;
import com.numbercruncher.rainbow.objects.Plane;
import com.numbercruncher.rainbow.objects.Prism;
import com.numbercruncher.rainbow.objects.Sphere;
import com.numbercruncher.rainbow.sky.Sky;
import com.numbercruncher.rainbow.sky.SkyChecker;
import com.numbercruncher.rainbow.sky.SkySpectral;
import com.numbercruncher.rainbow.sky.SkySunny;
import com.numbercruncher.rainbow.ray_tools.HitRecord;
import com.numbercruncher.rainbow.ray_tools.Ray;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple container for a list of scene objects.
 * It can be passed to the renderer.
 *
 */
public class Scene {
    public static final Scene DEFAULT_SCENE = new Scene(new SkySpectral())
            .addObject(new Plane(new Vector(0,0,1),new Vector(0,0,-1),new Lambertian(Color.GREEN)))
            .addObject(new Sphere(1,new Vector(1.5,1,0),new Lambertian(Color.RED)))
            .addObject(new Sphere(1,new Vector(-1.5,1,0),new Default(Color.RED)))
            .addObject(new Sphere(0.5,new Vector(0,0.5,-0.5),new Metal()));

    public static final Scene SPECTRAL_SKY_SCENE = new Scene(new SkySpectral())
            .addObject(new Plane(new Vector(0,0,1),new Vector(0,0,-1),new Lambertian(Color.GREEN)))
            .addObject(new Sphere(1,new Vector(1.5,1,0),new Lambertian(Color.RED)))
            .addObject(new Sphere(1,new Vector(-1.5,1,0),new Glass(1.7,0.01)))
            .addObject(new Sphere(0.5,new Vector(0,0.5,-0.5),new Metal()));

    // Light oven: large enclosing sphere (albedo=0.5, emission=0.5)
    // with a small sphere inside (albedo=0.5, emission=0.5).
    // No sky needed — all light comes from emission.
    private static final Color OVEN_GRAY = new Color(0.5, 0.5, 0.5);
    public static final Scene LIGHT_OVEN = new Scene()
            .addObject(new Sphere(100, new Vector(0, 0, 0), new Lambertian(OVEN_GRAY, 0.5)))
            .addObject(new Sphere(0.5, new Vector(0, 1, 0), new Lambertian(OVEN_GRAY, 0.5)));

    public static final Scene CHECKER_SCENE = new Scene(new SkyChecker())
            .addObject(new Plane(new Vector(0,0,1),new Vector(0,0,-1),new Lambertian(Color.GREEN)))
          .addObject(new Sphere(1,new Vector(0,3,0),new Lambertian(Color.RED)))
            .addObject(new Prism(new Vector(1,0.5,0),1,1.0, new Glass(1.7,0.01)))
            .addObject(new Sphere(1,new Vector(-1.5,1,0),new Glass(1.7, 0.01)))//1.7, 0.01
            .addObject(new Sphere(0.5,new Vector(0,0.5,-0.5), new Metal()));
;
    public static final Scene SUNNY_SCENE = new Scene(new SkySunny())
            .addObject(new Plane(new Vector(0,0,1),new Vector(0,0,-1),new Lambertian(Color.GRAY)))
            .addObject(new Sphere(1,new Vector(0,3,0),new Lambertian(Color.RED)))
            .addObject(new Prism(new Vector(1,0.5,0),1,1.0,new Glass(1.7,0.01)))
            .addObject(new Sphere(1,new Vector(-1.5,1,0),new Glass(1.7, 0.01)))//1.7, 0.01
            .addObject(new Sphere(0.5,new Vector(0,0.5,-0.5),new Metal()));

    // Option A: Prism on a floor with checker sky.
    // Camera on -x side to look through the angled faces (where dispersion happens).
    // Small prism, floor visible, sky visible around it.
    public static final Scene PRISM_SUNNY = new Scene(
            new SkySunny(Math.toRadians(45), Math.PI/2, 5800.0, 5.0, 0.1))
            .addObject(new Plane(new Vector(0, 0, 1), new Vector(0, 0, -1),
                    new Lambertian(Color.WHITE)))  // gray floor
            .addObject(new Prism(new Vector(0, 5, -0.5),
                    new Vector(0*Math.PI/2,0*Math.PI/2,0*Math.PI/2),
                    2.0,2.0,
                    new Glass(1.7, 0.01,0.8)))
//            .addObject(new Cube( new Vector(-1.5, 7, -1),3,
//                    new Vector(0,0,0), new Metal(0.1,0.1)))
;


    private List<SceneObject> objects;
    private Sky sky;

    public Scene(){
        this(new Sky());
    }

    public Scene(Sky sky){
        objects = new ArrayList<>();
        this.sky = sky;
    }

    public Scene addObject(SceneObject object){
        objects.add(object);
        return this;
    }

    public HitRecord intersect(Ray ray, Interval tInterval){
        HitRecord returnRecord = null;


        for (int i = 0; i < this.getObjects().size(); i++) {
            SceneObject obj = this.getObjects().get(i);
            HitRecord record = obj.intersect(ray);
            if (record!=null)
                if (tInterval.contains(record.t)) {
                    tInterval.shrinkTo(record.t);
                    returnRecord = record;
                    returnRecord.objectIndex = i;
                }
        }
        return returnRecord;
    }

    public List<SceneObject> getObjects(){
        return objects;
    }

    public void removeObject(SceneObject object){
        objects.remove(object);
    }

    public Sky getSky(){
        return sky;
    }


}
