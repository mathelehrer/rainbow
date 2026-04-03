package com.numbercruncher.rainbow;

import static com.numbercruncher.rainbow.Utils.EPS;

public class Face {

private Vector[] corners;
private int[] indices;
private Vector normal;
private Vector center;

public Face(Vector[] corners, int[] indices){
    this.corners = corners;
    this.indices = indices;
    this.normal = compute_normal();
    this.center = compute_center();
}

    /**
     * Warning: This only works for squares so far
     * @param p
     * @return
     */
    public boolean contains(Vector p) {
    Vector u = p.sub(corners[indices[0]]);
    if (Math.abs(u.dot(normal)) > EPS) return false;
    u = corners[indices[1]].sub(corners[indices[0]]);
    double uLength = u.length();
    u = u.scale(1.0/uLength);
    Vector v = corners[indices[3]].sub(corners[indices[0]]);
    double vLength = v.length();
    v = v.scale(1.0/vLength);
    double uValue = u.dot(p.sub(corners[indices[0]]));
    double vValue = v.dot(p.sub(corners[indices[0]]));
    if (uValue >= 0 && vValue >= 0 && uValue <=uLength  && vValue <= vLength)
        return true;
    return false;
}

private Vector compute_normal(){
    Vector u = corners[indices[1]].sub(corners[indices[0]]);
    Vector v = corners[indices[2]].sub(corners[indices[0]]);
    Vector normal = u.cross(v).normalize();
    //System.out.println("Normal: "+normal);
    return normal;
}

    private Vector compute_center(){
        Vector sum = new Vector();
        for (int i:indices){
            sum=sum.add(corners[i]);
        }
        Vector cntr=sum.scale(1.0/indices.length);
        //System.out.println("Center: "+cntr);
        return cntr;
    }

    public Vector getNormal() {
        return normal;
    }

    public Vector getCenter() {
        return center;
    }

@Override
    public String toString() {
    String cornerString = "";
    for (int i:indices){
        cornerString += corners[i].toString()+"->";
    }
    cornerString = cornerString.substring(0,cornerString.length()-2);

    String changeString = "";
    for (int i = 0; i <4 ; i++) {
        int start = indices[i];
        int end = indices[(i+1)%4];
        Vector diff = corners[end].sub(corners[start]);
        changeString += diff+"->";
    }
    changeString = changeString.substring(0,changeString.length()-2);
    return "Face: "+cornerString+" changes: "+changeString;
    }
}
