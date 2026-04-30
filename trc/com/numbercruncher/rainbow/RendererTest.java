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
        renderer.render("test_image_prism_sunny_tent");
    }

    @Test
    void renderRainbow(){
        // Camera close to origin, looking forward and upward into the drops.
        // Sun is behind camera at 20° altitude → rainbow arc at ~42° from anti-solar point
        // which is ~22° above horizontal.
        // Keeping origin close to (0,0,0) gives wide effective FOV after .sub(origin).
        Camera camera = new Camera(16./9, 2, 1,
                new Vector(0, -1, 0.1),   // close to origin for wide FOV
                new Vector(0, 8, 6),      // look forward and up toward rainbow arc
                new Vector(0, 0, 1));
        Renderer renderer = new Renderer(Scene.createRainbowScene(), camera, 5);
        renderer.render("test_image_rainbow");
    }

    @Test
    void renderCupCaustic(){
        // Camera straight above the cup looking down. The cup is taller than
        // its radius so the camera frustum stays inside the cup walls — the
        // outside floor never appears, leaving the cardioid caustic on the
        // shadowed bottom as the only bright feature. World-up is +y so the
        // sun (azimuth 0 → +y) appears "up" in the image; the heart-shaped
        // caustic opens toward the top of the frame.
        Camera camera = new Camera(16./9, 1.62, 2.5,
                new Vector(0, 0, 3.0),
                new Vector(0, 0, 0),
                new Vector(0, 1, 0));

        // Pre-pass: forward photon trace from the sun through specular
        // bounces, depositing onto a 2D + spectral grid covering the cup
        // floor. The renderer then reads the grid as extra incoming
        // irradiance for the floor's Lambertian — clean caustic, no
        // sample-starved noise.
        CausticMap map = new CausticMap(-1.05, 1.05, -1.05, 1.05,
                CIE1931.LAMBDA_MIN, CIE1931.LAMBDA_MAX,
                256, 256, 24);
        CausticTracer.shoot(Scene.CUP_CAUSTIC, map,
                2_000_000,   // photon count
                1.2,         // disk radius — covers cup top opening
                10.0);       // disk distance along sun direction
        Scene.CUP_CAUSTIC.setCausticMap(map);

        Renderer renderer = new Renderer(Scene.CUP_CAUSTIC, camera, 8);
        renderer.render("test_image_cup_caustic");
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