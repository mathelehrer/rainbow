package com.numbercruncher.rainbow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
}