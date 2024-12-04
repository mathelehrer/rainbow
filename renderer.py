from camera import Camera
from image import PPMImage, Color
from raytracer_utils import RESOLUTION, QUALITY

class Renderer:
    def __init__(self,render_function=None,camera=Camera(), quality=QUALITY.LOW):
        self.quality = quality
        self.camera = camera
        self.render_function = render_function

        self.width = RESOLUTION[self.quality]
        self.height = int(self.width / self.camera.aspect_ratio)
        self.image =PPMImage(self.width, self.height)

    def render(self):
        colors = []
        for h in range(self.height):
            for w in range(self.width):
                colors.append(Color(255*h/self.height,255*w/self.width,255*(1-0.5*h/self.height-0.5*w/self.width)))

        self.image.create_from_colors(colors)

if __name__ == '__main__':
    render = Renderer()
    render.render()