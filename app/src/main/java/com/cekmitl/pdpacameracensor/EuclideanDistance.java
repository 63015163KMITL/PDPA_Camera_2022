package com.cekmitl.pdpacameracensor;


public class EuclideanDistance {
    public static double run(float[] array1, float[] array2) {
        double distance = 0;
        for (int i = 0;i<array1.length;i++){

            double diff = array1[i] - array2[i];
            distance += diff*diff;
        }
        distance = (double) Math.sqrt(distance);
        return distance;
    }
}