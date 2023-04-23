package com.cekmitl.pdpacameracensor.Process;

public class Person {
    public final String name;
    public final float[][] features;
    public final String image_path;
    public boolean isOn;

    public Person(String name, float[][] features,String path,boolean isOn){
        this.name = name;
        this.features = features;
        this.image_path = path;
        this.isOn = isOn;
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
