package com.cekmitl.pdpacameracensor.Process;

import androidx.annotation.NonNull;

public class Score {
    public String name;
    public double score;

    public Score(String name,double score){
        this.name = name;
        this.score = score;
    }

    @NonNull
    @Override
    public String toString() {
        return "Score{" +
                "name='" + name + '\'' +
                ", score=" + score +
                '}';
    }
}
