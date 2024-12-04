import numpy as np
from mathutils import Vector

from raytracer_utils import vector_str


class Ray:
    """
    The one thing that all ray tracers have is a ray class and a computation of what color is seen along a ray.
    Let’s think of a ray as a function 𝐏(𝑡)=𝐀+𝑡𝐛. Here 𝐏
    is a 3D position along a line in 3D. 𝐀 is the ray origin and 𝐛 is the ray direction.
    The ray parameter 𝑡 is a real number (float in the code). Plug in a different 𝑡
    and 𝐏(𝑡) moves the point along the ray. Add in negative 𝑡 values and you can go anywhere on the 3D line.
    For positive 𝑡, you get only the parts in front of 𝐀, and this is what is often called a half-line or a ray.
    """
    def __init__(self,origin:Vector,direction:Vector):
        self.origin: Vector=origin
        self.direction: Vector=direction

    def at(self,t):
        return self.origin+t*self.direction

    def __repr__(self):
        return str(self)

    def __str__(self):
        """
        >>> Ray(Vector(),Vector([1,1.56,1.11]))
        [0.0, 0.0, 0.0]+t*[1.0, 1.6, 1.1]
        """
        return vector_str(self.origin,1)+"+t*"+vector_str(self.direction,1)


class RayWithAttenuation(Ray):
    def __init__(self,origin:Vector, direction:Vector, attenuation: Vector):
        self.attenuation: Vector = attenuation
        super().__init__(origin,direction)

