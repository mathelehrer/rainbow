package com.numbercruncher.rainbow;

import static com.numbercruncher.rainbow.Utils.EPS;

public class PPMImage {
    private final int width,height;
    private final  int maxVal;
    private final String ppm_header;
    private final int[] image;

    public PPMImage(int width,int height,int maxVal){
        this.width=width;
        this.height=height;
        this.maxVal=maxVal;

        this.ppm_header = "P3\n"+"# test\n"+this.width+" "+this.height+"\n"+this.maxVal+"\n";
        this.image = new int[3*this.width*this.height];
    }

    /**
     * Gamma correction suggested by llm
     * @param x
     * @return
     */
    private static double linear_to_gamma(double x){
        return x<=0.0031308 ? 12.92*x : 1.055*Math.pow(x,0.41666666666666663)-0.055;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }
    public int getMaxVal(){
        return maxVal;
    }

    public void create_sample_image(){

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                image[(y*width+x)*3+0] = 255;//(int)(Math.random()*maxVal);
                image[(y*width+x)*3+1] = 0*(int)(Math.random()*maxVal);
                image[(y*width+x)*3+2] = 0*(int)(Math.random()*maxVal);
            }
        }

        save("test_image");
    }


    private int toPixelValue(double linear) {
        if (Double.isNaN(linear) || Double.isInfinite(linear)) linear = 0.0;
        double gamma = linear_to_gamma(Math.max(0, linear));
        return (int)(Math.min(gamma, 1.0 - EPS) * maxVal);
    }

    public void create_from_color(Color[] pixels,String filename){
        for (int i = 0; i < pixels.length; i++) {
            Color pixel = pixels[i];
            this.image[3*i]   = toPixelValue(pixel.r);
            this.image[3*i+1] = toPixelValue(pixel.g);
            this.image[3*i+2] = toPixelValue(pixel.b);
        }

        save(filename);
    }

    /**
     * Export image as P3 (ASCII) PPM file.
     */
    public void save(String filename){
        String path = filename+".ppm";
        try{
            java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(path));
            writer.write("P3\n" + this.width + " " + this.height + "\n" + this.maxVal + "\n");
            for(int i = 0; i < image.length; i += 3){
                writer.write(image[i] + " " + image[i+1] + " " + image[i+2]);
                writer.newLine();
            }
            writer.close();
        }catch(java.io.IOException e){
            System.out.println("Error writing P3 file: " + e.getMessage());
        }

        // Also save as P6 (binary) — smaller and more widely supported
        String pathBin = filename + "_bin.ppm";
        try {
            java.io.FileOutputStream fos = new java.io.FileOutputStream(pathBin);
            String header = "P6\n" + this.width + " " + this.height + "\n" + this.maxVal + "\n";
            fos.write(header.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            byte[] bytes = new byte[image.length];
            for (int i = 0; i < image.length; i++) {
                bytes[i] = (byte) image[i];
            }
            fos.write(bytes);
            fos.close();
        } catch (java.io.IOException e) {
            System.out.println("Error writing P6 file: " + e.getMessage());
        }
    }
}
