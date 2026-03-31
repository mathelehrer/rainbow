package com.numbercruncher.rainbow;

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


    public void create_from_color(Color[] pixels,String filename){

        for (int i = 0; i < pixels.length; i++) {
            Color pixel = pixels[i];
            this.image[3*i] = (int)(pixel.r*maxVal);
            this.image[3*i+1] = (int)(pixel.g*maxVal);
            this.image[3*i+2] = (int)(pixel.b*maxVal);

        }

        save(filename);
    }

    /**
     * export imate to ppm file
     */
    public void save(String filename){
        filename = filename+".ppm";
        try{
            java.io.FileWriter writer = new java.io.FileWriter(filename);
            writer.write(ppm_header);
            for(int i = 0; i < image.length; i++){
                writer.write(image[i]+" ");
            }
            writer.close();
        }catch(java.io.IOException e){
            System.out.println("Error writing file: " + e.getMessage());
        }
    }
}
