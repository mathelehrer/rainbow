package com.numbercruncher.rainbow;

public class Matrix {

/*****************************
 **** Attribute **************
 *****************************/
protected double[][] values;
/*****************************
 **** Konstruktor*************
 *****************************/

public Matrix(double[][] values){
    this.values=values;
}

public Matrix (Double[][] values){
    this.values=new double[3][3];
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            this.values[i][j]=values[i][j];
        }
    }
}

public Matrix(){
    this.values = new double[3][3];
}

public Matrix (Vector... vectors){
    this.values = new double[3][3];
    for(int i=0;i<3;i++){
        this.values[i][0]=vectors[i].x;
        this.values[i][1]=vectors[i].y;
        this.values[i][2]=vectors[i].z;
    }
}



/*****************************
 **** Getter    **************
 *****************************/

/*****************************
 **** Setter    **************
 *****************************/

public double[][] getValues(){
    return values;
}
/*****************************
 **** public methods *********
 *****************************/

public Matrix add(Matrix m){
    double[][] result = new double[3][3];
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            result[i][j]=values[i][j]+m.values[i][j];
        }
    }
    return new Matrix(result);
}

public Matrix mul(Matrix m){
    double[][] result = new double[3][3];
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            double sum = 0;
            for (int k = 0; k < 3; k++) {
                sum+=values[i][k]*m.values[k][j];
            }
            result[i][j]=sum;
        }
    }
    return new Matrix(result);
}

public Vector map(Vector v){
   Double[] result = new Double[3];
    for (int i = 0; i < 3; i++) {
        double sum = 0;
        for (int j = 0; j < 3; j++) {
            sum+=values[i][j]*v.get(j);
        }
        result[i]=sum;
    }
    return new Vector(result);
}
/*****************************
 **** private methods  *******
 *****************************/

/*****************************
 **** overrides     **********
 *****************************/

}
