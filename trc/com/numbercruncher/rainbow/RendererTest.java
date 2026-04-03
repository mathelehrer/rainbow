package com.numbercruncher.rainbow;


import org.junit.jupiter.api.Test;

class RendererTest {

    @Test
    void renderTestScene() {


        Renderer renderer = new Renderer();
        renderer.renderTestScene();
    }

    @Test
    void render(){
        Renderer renderer = new Renderer(Scene.DEFAULT_SCENE,10);
        renderer.render("test_image_default_sphere");
    }

    @Test
    void renderSpectralSky(){
        Renderer renderer = new Renderer(Scene.SPECTRAL_SKY_SCENE,10);
        renderer.render("test_image_spectral_sky");
    }

    @Test
    void renderSpectralWithObjects(){
        Renderer renderer = new Renderer(Scene.SPECTRAL_SKY_SCENE,10);
        renderer.render("test_image_spectral_with_objects");
    }

    @Test
    void lightOven(){
        Renderer renderer = new Renderer(Scene.LIGHT_OVEN,4);
        renderer.render("test_image_light_oven");
    }

    @Test
    void renderCheckerScene(){
        Renderer renderer = new Renderer(Scene.CHECKER_SCENE,10);
        renderer.render("test_image_checker_scene");
    }

    @Test
    void renderSunnyScene(){
        Renderer renderer = new Renderer(Scene.SUNNY_SCENE,1);
        renderer.render("test_image_sunny_scene");
    }

    @Test
    void renderPrismSunny(){
        // Same camera, sunny sky — white sunlight through glass shows dispersion.
        Camera camera = new Camera(16./9, 2, 0.75,
                new Vector(0, -4, -0.5),//ray origin
                new Vector(0, 1, 0),//lookAt
                new Vector(0, 0, 1));//world up
        Renderer renderer = new Renderer(Scene.PRISM_SUNNY, camera, 40);
        renderer.render("test_image_prism_sunny");
    }

    @Test
    void renderPrismSunny_two(){
        // Same camera, sunny sky — white sunlight through glass shows dispersion.
        Camera camera = new Camera(16./9, 2, 0.75,
                new Vector(0, -4, 0),//ray origin
                new Vector(0, 1, 0),//lookAt
                new Vector(0, 0, 1));//world up
        Renderer renderer = new Renderer(Scene.PRISM_SUNNY_TWO, camera, 5);
        renderer.render("test_image_prism_sunny_two");
    }
}