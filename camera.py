from mathutils import Vector
from ray import Ray


class Camera:
    """
    When first developing a ray tracer, I always do a simple camera for getting the code up and running.
    I’ve often gotten into trouble using square images for debugging because I transpose 𝑥 and 𝑦
    too often, so we’ll use a non-square image.
    A square image has a 1∶1 aspect ratio, because its width is the same as its height. Since we want
    a non-square image, we'll choose 16∶9 because it's so common. A 16∶9 aspect ratio means that the ratio of
    image width to image height is 16∶9. Put another way, given an image with a 16∶9 aspect ratio,
    width/height=16/9=1.7778

    For a practical example, an image 800 pixels wide by 400 pixels high has a 2∶1 aspect ratio.

    The image's aspect ratio can be determined from the ratio of its width to its height. However, since we have
    a given aspect ratio in mind, it's easier to set the image's width and the aspect ratio, and then using this
    to calculate for its height. This way, we can scale up or down the image by changing the image width, and it won't
    throw off our desired aspect ratio. We do have to make sure that when we solve for the image height the resulting
    height is at least 1.

    In addition to setting up the pixel dimensions for the rendered image, we also need to set up a virtual viewport
    through which to pass our scene rays. The viewport is a virtual rectangle in the 3D world that contains the grid of
    image pixel locations. If pixels are spaced the same distance horizontally as they are vertically, the viewport that
    bounds them will have the same aspect ratio as the rendered image. The distance between two adjacent pixels
    is called the pixel spacing, and square pixels is the standard.

    To start things off, we'll choose an arbitrary viewport height of 2.0, and scale the viewport width to give us the
    desired aspect ratio.
    """
    def __init__(self,aspect_ratio: float=16./9, viewport_height: float=2., focal_length: float=1.):
        self.aspect_ratio:float = aspect_ratio
        self.viewport_height:float = viewport_height
        self.viewport_width: float = viewport_height*aspect_ratio
        self.focal_length:float = focal_length

        self.origin=Vector()
        self.viewport_u = Vector([self.viewport_width,0,0])
        self.viewport_v = Vector([0,0,self.viewport_height])

        # make the viewport centered relatively to the camera
        self.lower_left_corner = self.origin-0.5*self.viewport_u-0.5*self.viewport_v-Vector([0,focal_length,0])

    def get_ray(self,u:float, v:float)->Ray:
        return Ray(self.origin,self.lower_left_corner+u*self.viewport_u+v*self.viewport_v-self.origin)

