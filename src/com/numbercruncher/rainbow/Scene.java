package com.numbercruncher.rainbow;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple container for a list of scene objects.
 * It can be passed to the renderer.
 *
 */
public class Scene {
    public static final Scene DEFAULT_SCENE = new Scene(new SpectralSky())
            .addObject(new Plane(new Vector(0,0,1),new Vector(0,0,-1),new MaterialLambertian(Color.GREEN)))
            .addObject(new Sphere(1,new Vector(1.5,1,0),new MaterialLambertian(Color.RED)))
            .addObject(new Sphere(1,new Vector(-1.5,1,0),new MaterialDefault(Color.RED)))
            .addObject(new Sphere(0.5,new Vector(0,0.5,-0.5),new MaterialMetal()));

    public static final Scene SPECTRAL_SKY_SCENE = new Scene(new SpectralSky())
            .addObject(new Plane(new Vector(0,0,1),new Vector(0,0,-1),new MaterialLambertian(Color.GREEN)))
            .addObject(new Sphere(1,new Vector(1.5,1,0),new MaterialLambertian(Color.RED)))
            .addObject(new Sphere(1,new Vector(-1.5,1,0),new MaterialGlass(1.7,0.01)))
            .addObject(new Sphere(0.5,new Vector(0,0.5,-0.5),new MaterialMetal()));

    // Light oven: large enclosing sphere (albedo=0.5, emission=0.5)
    // with a small sphere inside (albedo=0.5, emission=0.5).
    // No sky needed — all light comes from emission.
    private static final Color OVEN_GRAY = new Color(0.5, 0.5, 0.5);
    public static final Scene LIGHT_OVEN = new Scene()
            .addObject(new Sphere(100, new Vector(0, 0, 0), new MaterialLambertian(OVEN_GRAY, 0.5)))
            .addObject(new Sphere(0.5, new Vector(0, 1, 0), new MaterialLambertian(OVEN_GRAY, 0.5)));

    public static final Scene CHECKER_SCENE = new Scene(new CheckerSky())
            .addObject(new Plane(new Vector(0,0,1),new Vector(0,0,-1),new MaterialLambertian(Color.GREEN)))
            .addObject(new Sphere(1,new Vector(1.5,1,0),new MaterialLambertian(Color.RED)))
            .addObject(new Sphere(1,new Vector(-1.5,1,0),new MaterialGlass(1.7, 0.01)))//1.7, 0.01
            .addObject(new Sphere(0.5,new Vector(0,0.5,-0.5),new MaterialMetal()));

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

    public HitRecord intersect(Ray ray,Interval tInterval){
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
