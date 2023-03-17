package com.cekmitl.pdpacameracensor;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class PersonDatabase {

    private File DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    public Person[] persons;
    private String restricName = "temp";
    private int MIN_MATCH = 1;

    public PersonDatabase() throws IOException {

        create_ParentFolder("/Features");

        File[] person_list = getPersonList();
        persons = new Person[person_list.length];

        for (int i =0;i<persons.length;i++){

            persons[i] = new Person(person_list[i].getName(),getVectorList(person_list[i].getName()),DOC_PATH + "/Features/"+person_list[i].getName()+"/display.png");
            Log.d("PERSONIMAGE", DOC_PATH + "/Features/"+person_list[i].getName()+"/display.png");

        }
    }

    public void create_ParentFolder(String name){

        File newF = new File(DOC_PATH,name);
        if (!newF.exists()){
            newF.mkdirs();
        }
    }

    public void add_newPerson_folder(String person){
        File newF = new File(DOC_PATH+"/Features/",person);
        if (!newF.exists()){
            newF.mkdirs();
        }
    }

    public void save2file(float[] vec,String person) throws IOException {
//        File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
//        String newFeaturePath = person+"txt";
        int i = 0;
        File file = getValidPath(person,i);

        while (file.exists() == true){
            Log.d("SAVING", "save file name : "+ file.exists());
            i = i + 1;
            file = getValidPath(person,i);
        }




        FileWriter writer = new FileWriter(file);

        String s = "";
        s = Arrays.toString(vec);
        s = s.substring(1,s.length()-1);
        writer.append(s);
        writer.flush();
        writer.close();
    }

    public void save_image(Bitmap bmp,String person){
        try (FileOutputStream out = new FileOutputStream(DOC_PATH + "/Features/" +person+"/"+"display.png")) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getValidPath(String person,int i){
        String path = DOC_PATH + "/Features/" + person;
//
//        for (int i = 0;i<999;i++){
        File checkFile = new File(path+"/"+String.valueOf(i)+".txt");
//            if (!checkFile.exists() ){
//                return checkFile;
//            }
//        }
        return checkFile;
    }

    public float[] getArray(File f) throws IOException {

        File file = f;
        String myData = "";
        FileInputStream fs = new FileInputStream(file);

        DataInputStream in = new DataInputStream(fs);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        float[] v = new float[192];
        String strLine;
        int a = 0;
        while ((strLine = br.readLine()) != null) {
            myData = myData + strLine;
        }
        in.close();

        String[] s = myData.split(",");
        for (int i=0;i<s.length;i++){
            v[i] = Float.parseFloat(s[i]);
        }

        return v;
    }

    public File[] getPersonList(){
        File f = new File(DOC_PATH +"/Features");
        return f.listFiles();
    }

    public float[][] getVectorList(String person) throws IOException {
        File f = new File(DOC_PATH + "/Features/" +person);

        File[] files = f.listFiles();
        float[][] vectors = new float[files.length][192];
        int i = 0;
        for (File file: files) {
            String filename = file.getName();

            if (filename.substring(filename.length() - 3).equals("txt") ){
                float[] v = getArray(file);
                vectors[i]=v;
                i++;
            }

        }
        return vectors;
    }

    public Score recognize(float[] array,double confident){
        ArrayList<Score> scores = new ArrayList<>();

        for (Person p: persons) {
            double minSim = 1.00;
            int i = 0;
            for (float[] vector: p.getfeatures()) {
                double r = EuclideanDistance.run(vector,array);

                if (r < confident){
                    minSim = r;
                    i += 1;
                }
            }

            if (i >= MIN_MATCH){
                scores.add(new Score(p.getName(),minSim));
            }
        }
        return bestScore(scores);
    }

    private Score bestScore(ArrayList<Score> scores){
        Score bestScore = null;
        for (Score s: scores) {
            if (bestScore==null){
                bestScore = s;
            }else{
                if (s.score < bestScore.score){
                    bestScore = s;
                }
            }
        }
        return bestScore;
    }


}