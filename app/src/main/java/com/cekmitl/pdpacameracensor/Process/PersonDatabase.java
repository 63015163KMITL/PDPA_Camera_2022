package com.cekmitl.pdpacameracensor.Process;

import android.graphics.Bitmap;

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
import java.util.Objects;

public class PersonDatabase {

    public Person[] persons;

    private final int MIN_MATCH = AIProperties.MIN_MATCH;
    private final float CONF = AIProperties.DIFF_RATIO;

    public PersonDatabase() throws IOException {
        create_ParentFolder("/Features");
        File[] person_list = getPersonList();
        persons = new Person[person_list.length];
        for (int i =0;i<persons.length;i++){
            persons[i] = new Person(person_list[i].getName(),getVectorList(person_list[i].getName()),AIProperties.DOC_PATH  + "/Features/"+person_list[i].getName()+"/display.jpg",isOn(person_list[i].getName()));
        }
    }

    //Only Init case
    public PersonDatabase(int code) throws IOException {

        if (code == -1){
            createCheckingFile();
            create_ParentFolder("/Features");
            File[] person_list = getPersonList();
            persons = new Person[person_list.length];
            for (int i =0;i<persons.length;i++){

                persons[i] = new Person(person_list[i].getName(),null,AIProperties.DOC_PATH  + "/Features/"+person_list[i].getName()+"/display.jpg",isOn(person_list[i].getName()));
            }
        }
    }

    public void create_ParentFolder(String name){
        File newF = new File(AIProperties.DOC_PATH ,name);
        if (!newF.exists()){
            newF.mkdirs();
        }
    }

    public void add_newPerson_folder(String person){
        File newF = new File(AIProperties.DOC_PATH +"/Features/",person);
        if (!newF.exists()){
            newF.mkdirs();
        }
    }

    public void save2file(float[] vec, String person) throws IOException {

        int i = 0;
        File file = getValidPath(person,i);

        while (file.exists()){
            i = i + 1;
            file = getValidPath(person,i);
        }

        FileWriter writer = new FileWriter(file);

        String s;
        s = Arrays.toString(vec);
        s = s.substring(1,s.length()-1);
        writer.append(s);
        writer.flush();
        writer.close();


        createCheckingFile(person);
    }

    public void save_image(Bitmap bmp,String person){
        try (FileOutputStream out = new FileOutputStream(AIProperties.DOC_PATH  + "/Features/" +person+"/"+"display.jpg")) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getValidPath(String person,int i){
        String path = AIProperties.DOC_PATH  + "/Features/" + person;
        return new File(path+"/"+ i +".txt");
    }

    public float[] getArray(File f) throws IOException {
        StringBuilder myData = new StringBuilder();
        FileInputStream fs = new FileInputStream(f);

        DataInputStream in = new DataInputStream(fs);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        float[] v = new float[192];
        String strLine;
        while ((strLine = br.readLine()) != null) {
            myData.append(strLine);
        }
        in.close();

        String[] s = myData.toString().split(",");
        for (int i=0;i<s.length;i++){
            v[i] = Float.parseFloat(s[i]);
        }
        return v;
    }

    public File[] getPersonList(){
        File f = new File(AIProperties.DOC_PATH  +"/Features");
        return f.listFiles();
    }

    public float[][] getVectorList(String person) throws IOException {
        File f = new File(AIProperties.DOC_PATH  + "/Features/" +person);
        File[] files = f.listFiles();
        assert files != null;
        float[][] vectors = new float[files.length][192];
        int i = 0;
        for (File file: files) {
            String filename = file.getName();

            if (filename.endsWith("txt") && !filename.startsWith("detail")){
                float[] v = getArray(file);
                vectors[i]=v;
                i++;
            }
        }
        return vectors;
    }

    // คัดแยกทุกใบหน้า
    public Score recognize(float[] array){
        ArrayList<Score> scores = new ArrayList<>();
        for (Person p: persons) {
            if (p.isOn){
                double minSim = 1.00;
                int i = 0;
                for (float[] vector: p.getfeatures()) {
                    double r = EuclideanDistance.run(vector,array);
                    if (r < CONF){
                        minSim = r;
                        i += 1;
                    }
                }

                if (i >= MIN_MATCH){
                    scores.add(new Score(p.getName(),minSim));
                }
            }
        }
        return bestScore(scores);
    }

    // คัดแยกเฉพาะใบหน้าที่เลือก
    public Score recognize(float[] array,ArrayList<String> filter){
        ArrayList<Score> scores = new ArrayList<>();
        for (Person p: persons) {
            double minSim = 1.00;
            int i = 0;
            if (isSelectedPerson(filter,p.getName())){
                for (float[] vector: p.getfeatures()) {
                    double r = EuclideanDistance.run(vector,array);
                    if (r < CONF){
                        minSim = r;
                        i += 1;
                    }
                }
                if (i >= MIN_MATCH){
                    scores.add(new Score(p.getName(),minSim));
                }
            }

        }
        return bestScore(scores);
    }

    boolean isSelectedPerson(ArrayList<String> filterPerson,String person){
        for (String p : filterPerson){
            if (p.equals(person)){
                return true;
            }
        }
        return false;
    }

    private Score bestScore(ArrayList<Score> scores){
        Score bestScore = null;
        for (Score s: scores) {
            if (bestScore == null){
                bestScore = s;
            }else{
                if (s.score < bestScore.score){
                    bestScore = s;
                }
            }
        }
        return bestScore;
    }


    public void createCheckingFile() throws IOException {
        for (File f : getPersonList()){
            File checkFile = new File(AIProperties.DOC_PATH  + "/Features/" +f.getName()+"/"+"detail.txt");
            if (!checkFile.exists()){
                FileWriter writer = new FileWriter(checkFile);
                String s = "1";
                writer.append(s);
                writer.flush();
                writer.close();
            }
        }
    }

    public void createCheckingFile(String person) throws IOException {
        File checkFile = new File(AIProperties.DOC_PATH  + "/Features/" +person+"/"+"detail.txt");
        if (!checkFile.exists()){
            FileWriter writer = new FileWriter(checkFile);
            String s = "1";
            writer.append(s);
            writer.flush();
            writer.close();
        }
    }

    public boolean isOn(String person) throws IOException {
        File checkFile = new File(AIProperties.DOC_PATH  + "/Features/" +person+"/"+"detail.txt");
        if (checkFile.exists()){
            FileInputStream fs = new FileInputStream(checkFile);

            DataInputStream in = new DataInputStream(fs);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            if (Objects.equals(br.readLine(), "1")){
                return true;
            }
            in.close();

        }
        return false;
    }

    public void changState(String person,boolean isOn) throws IOException {
        File checkFile = new File(AIProperties.DOC_PATH  + "/Features/" +person+"/"+"detail.txt");
        if (checkFile.exists()){
            FileWriter writer = new FileWriter(checkFile);
            String s = "1";
            if (!isOn){
                s="0";
            }
            writer.append(s);
            writer.flush();
            writer.close();
        }else{
            FileWriter writer = new FileWriter(checkFile);
            String s = "1";
            writer.append(s);
            writer.flush();
            writer.close();
        }
    }


    public boolean test(float[] arr,String person){
        if (recognize(arr) != null){
            if (Objects.equals(recognize(arr).name, person)){
                return true;
            }else {
                return false;
            }
        }
        return false;

    }

    public float test(ArrayList<float[]> arr,String person){
        float true_predict = 0f;
        float num = 10f;

        for (float[] a : arr){
            if (test(a, person)){
                true_predict += 1;
            }
        }
        return (true_predict / num) * 100;
    }


}