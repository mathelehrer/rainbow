package com.numbercruncher.rainbow;

public class Renderer {
    private final Camera camera;
    private int width,height;
    private PPMImage image;

    public Renderer(){
        this.camera = new Camera(16./9,2,1.);
        this.width = 192;
        this.height = 108;
        this.image = new PPMImage(this.width,this.height,255);


    }

    public void render(){
        Color[] colors = new Color[this.width*this.height];
        double hm = 1./this.height;
        double wm = 1./this.width;
        for (int y = 0; y< this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                colors[y*width+x]=new Color( (float) (y * hm), (float) (x * wm),(float) (1.-0.5*y*hm-0.5*x*wm));
            }

        }
        this.image.create_from_color(colors,"test_image");
    }
}
