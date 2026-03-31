from mathutils import Vector

from objects.hitable import HitRecord
from old_python_scripts.ray import Ray


class Sphere:
    def __init__(self,r:float =1,center: (Vector,list)=Vector(),material= None):
        self.r = r
        self.center = center
        self.material = material

    def hit(self,ray: Ray, t_min: float, t_max: float)-> HitRecord:

