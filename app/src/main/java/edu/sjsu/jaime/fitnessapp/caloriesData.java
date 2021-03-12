package edu.sjsu.jaime.fitnessapp;

public class caloriesData {

    private int time;
    private double calories;

    public caloriesData(int time, double calories)
    {
        this.time = time;
        this.calories=calories;
    }

    public int getValueX()
    {
        return time;
    }

    public double getValueY()
    {
        return calories;
    }

}
