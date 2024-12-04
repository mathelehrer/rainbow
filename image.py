import array
from time import time


class Color:
    def __init__(self,r:int,g:int,b:int):
        self.red = int(r)
        self.green = int(g)
        self.blue = int(b)

    def __str__(self):
        return "c("+str(self.red)+","+str(self.green)+","+str(self.blue)+")"

    def __repr__(self):
        return str(self)

class PPMImage:
    def __init__(self,width=256,height=128,maxval=255):
        self.width =int(width)
        self.height= int(height)
        self.maxval= int(maxval)

        self.ppm_header= f'P6 {width} {height} {maxval}\n'
        self.image = array.array('B',[0,0,255]*self.width*self.height)


    def create_sample_image(self):
        for y in range(10,90):
            for x in range(10,60):
                index = 3*(y*self.width+x)
                self.image[index]=255 #red
                self.image[index+1]=0 # green
                self.image[index+2]=0 # blue
        self.save()

    def save(self,filename="test_image.ppm"):
        with open(filename,'wb') as f:
            f.write(bytearray(self.ppm_header,'ascii'))
            self.image.tofile(f)

    def create_from_colors(self,colors,filename="test_image_"+str(time())+".ppm"):

        for i,color in enumerate(colors):
            self.image[3*i]=color.red
            self.image[3*i+1]=color.green
            self.image[3*i+2]=color.blue

        self.save(filename=filename)

    def __repr__(self):
        return str(self)
    def __str__(self):
        return str(self.width)+"x"+str(self.height)+"x"+str(self.maxval)+"_PPMImage"


if __name__ == '__main__':
    img = PPMImage()
    img.create_sample_image()