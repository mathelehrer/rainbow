package com.numbercruncher.rainbow;

public class CupCausticMain {
    public static void main(String[] args) {
        int factor = args.length > 0 ? Integer.parseInt(args[0]) : 30;
        int photons = args.length > 1 ? Integer.parseInt(args[1]) : 100_000_000;

        Camera camera = new Camera(16. / 9, 1.62, 2.5,
                new Vector(0, 0, 3.0),
                new Vector(0, 0, 0),
                new Vector(0, 1, 0));

        CausticMap map = new CausticMap(-1.05, 1.05, -1.05, 1.05,
                CIE1931.LAMBDA_MIN, CIE1931.LAMBDA_MAX,
                1280, 1280, 8);
        CausticTracer.shoot(Scene.CUP_CAUSTIC, map, photons, 1.0, 10.0);
        Scene.CUP_CAUSTIC.setCausticMap(map);

        Renderer renderer = new Renderer(Scene.CUP_CAUSTIC, camera, factor);
        renderer.render("test_image_cup_caustic");
    }
}
