package com.numbercruncher.rainbow;

public class RotationMatrix extends Matrix{

/*****************************
 **** Attribute **************
 *****************************/

/*****************************
 **** Konstruktor*************
 *****************************/
public RotationMatrix(Vector eulerAngles){
    this(eulerAngles, "XYZ");
}

public RotationMatrix(Vector eulerAngles, String order){
    super(new Vector(),new Vector(),new Vector());

    if (order.length() != 3)
        throw new IllegalArgumentException("Rotation order must be a 3-character string, e.g. \"XYZ\". Got: \"" + order + "\"");

    Matrix result = rotationForAxis(order.charAt(0), eulerAngles);
    result = rotationForAxis(order.charAt(1), eulerAngles).mul(result);
    result = rotationForAxis(order.charAt(2), eulerAngles).mul(result);

    super.values = result.getValues();
}

private static Matrix rotationForAxis(char axis, Vector angles){
    switch (Character.toUpperCase(axis)){
        case 'X': return new Matrix(
                new Vector(1,0,0),
                new Vector(0,Math.cos(angles.x),-Math.sin(angles.x)),
                new Vector(0,Math.sin(angles.x),Math.cos(angles.x))
        );
        case 'Y': return new Matrix(
                new Vector(Math.cos(angles.y),0,Math.sin(angles.y)),
                new Vector(0,1,0),
                new Vector(-Math.sin(angles.y),0,Math.cos(angles.y))
        );
        case 'Z': return new Matrix(
                new Vector(Math.cos(angles.z),-Math.sin(angles.z),0),
                new Vector(Math.sin(angles.z),Math.cos(angles.z),0),
                new Vector(0,0,1)
        );
        default: throw new IllegalArgumentException("Invalid rotation axis: '" + axis + "'. Must be X, Y, or Z.");
    }
}
/*****************************
 **** Getter    **************
 *****************************/

/*****************************
 **** Setter    **************
 *****************************/

/*****************************
 **** public methods *********
 *****************************/


/*****************************
 **** private methods  *******
 *****************************/

/*****************************
 **** overrides     **********
 *****************************/

}
