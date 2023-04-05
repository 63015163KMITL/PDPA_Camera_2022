package com.cekmitl.pdpacameracensor.Process;

public class Person {
    private String name;
    private float[][] features;
    private String image_path;

    public Person(String name, float[][] features,String path){

        this.name = name;
        this.features = features;
        this.image_path = path;
    }

    public String getName(){
        return name;
    }

    public float[][] getfeatures(){
        return features;
    }

    public String getImage() {
        return image_path;
    }







}
