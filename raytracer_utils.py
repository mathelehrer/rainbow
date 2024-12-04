from enum import Enum

import numpy as np
from mathutils import Vector

class QUALITY (Enum):
    HIGH = 1
    MEDIUM = 2
    LOW = 3

RESOLUTION = {QUALITY.HIGH: 3440, QUALITY.MEDIUM: 1920, QUALITY.LOW: 800}

def vector_str(v: (Vector, list), d: int):
    """
    display vector as string
    """

    lst = [float(np.round(c,d)) for c in list(v)]
    return str(lst)


