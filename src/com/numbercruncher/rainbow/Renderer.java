package com.numbercruncher.rainbow;

public class Renderer {
    private final Camera camera;
    private int width,height;
    private PPMImage image;
    private Scene scene;


    public Renderer(Scene scene){
        this.camera = new Camera(16./9,2,1.);
        this.width = 192;
        this.height = 108;
        this.image = new PPMImage(this.width,this.height,255);
        this.scene = scene;
    }

    /**
     * setup renderer with default scene
     */
    public Renderer(){
        this(Scene.DEFAULT_SCENE);
    }

    public void render(String filename){
        Color[] colors = new Color[this.width*this.height];
        double hm = 1./this.height;
        double wm = 1./this.width;
        for (int y = 0; y< this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                Ray ray = this.camera.getRay(x*wm,y*hm);
                int hit = -1;
                double minT = Double.MAX_VALUE;
                for (int i = 0; i < scene.getObjects().size(); i++) {
                    SceneObject obj = scene.getObjects().get(i);
                    if (obj.intersects(ray)) {
                        double t = obj.intersect(ray);
                        if (t>0 && t<minT) {
                            minT = t;
                            hit = i;
                        }
                    }
                }

                if (hit==-1) {
                    System.out.println("No it for (" + x + "," + y + ") by ray "+ray+".");
                    colors[y*width+x]=scene.getSky().getColor(ray);
                }
                else{
                    // get normal
                    Vector normal = scene.getObjects().get(hit).getNormal(ray.at(minT));
                    colors[y*width+x]=new Color(new Vector(1,1,1).add(normal).scale(0.5));
                    System.out.println("object "+hit+" hit for (" + x + "," + y + ") by ray "+ray);
                }
            }

        }
        this.image.create_from_color(colors,filename);
    }

    public void renderTestScene(){
        Color[] colors = new Color[this.width*this.height];
        double hm = 1./this.height;
        double wm = 1./this.width;
        for (int y = 0; y< this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                Ray ray = this.camera.getRay(x*wm,y*hm);
                colors[y*width+x]=new Color( (float) (y * hm), (float) (x * wm),(float) (1.-0.5*y*hm-0.5*x*wm));
            }

        }
        this.image.create_from_color(colors,"test_image2");
    }
}
