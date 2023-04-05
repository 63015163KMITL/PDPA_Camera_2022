package com.cekmitl.pdpacameracensor.Process;

public class Score {
    public String name;
    public double score;

    public Score(String name,double score){
        this.name = name;
        this.score = score;
    }

    @Override
    public String toString() {
        return "Score{" +
                "name='" + name + '\'' +
                ", score=" + score +
                '}';
    }
}
