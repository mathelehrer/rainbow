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
        Renderer renderer = new Renderer(Scene.DEFAULT_SCENE);
        renderer.render("testDefaultSphere");
    }

    @Test
    void renderSpectralSky(){
        Renderer renderer = new Renderer(Scene.SPECTRAL_SKY_SCENE);
        renderer.renderSpectral("spectral_sky");
    }

    @Test
    void renderSpectralWithObjects(){
        Renderer renderer = new Renderer(Scene.SPECTRAL_SKY_SCENE);
        renderer.renderSpectral("spectral_with_objects");
    }

    @Test
    void lightOven(){
        Renderer renderer = new Renderer(Scene.LIGHT_OVEN);
        renderer.renderSpectral("light_oven");
    }

    @Test
    void renderCheckerScene(){
        Renderer renderer = new Renderer(Scene.CHECKER_SCENE);
        renderer.renderSpectral("checker_scene");
    }
}