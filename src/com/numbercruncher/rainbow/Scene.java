package com.numbercruncher.rainbow;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple container for a list of scene objects.
 * It can be passed to the renderer.
 *
 */
public class Scene {
    public static final Scene DEFAULT_SCENE = new Scene()
            .addObject(new Sphere(1,new Vector(1.5,1,0),new Material(Color.RED)))
            .addObject(new Sphere(1,new Vector(-1.5,1,0),new Material(Color.RED)))
            .addObject(new Sphere(0.1,new Vector(0,1,-1),new Material(Color.RED)))
            .addObject(new Sphere(50,new Vector(0,0,-51),new Material(Color.BLUE)));

    private List<SceneObject> objects;
    private Sky sky;

    public Scene(){
        objects = new ArrayList<>();
        sky = new Sky();

    }

    public Scene addObject(SceneObject object){
        objects.add(object);
        return this;
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
