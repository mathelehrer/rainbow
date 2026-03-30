package com.numbercruncher.rainbow;



import static org.junit.jupiter.api.Assertions.*;

class PPMImageTest {

    @org.junit.jupiter.api.Test
    void create_sample_image() {
        PPMImage image = new PPMImage(100,100,255);
        image.create_sample_image();

    }
}