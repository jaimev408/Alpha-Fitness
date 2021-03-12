package edu.sjsu.jaime.fitnessapp;

public class stepsData {

    private int steps;
    private int time;

    public stepsData(int time, int steps)
    {
        this.steps = steps;
        this.time = time;
    }

    public int getValueX()
    {
        return time;
    }

    public int getValueY()
    {
        return steps;
    }
}
