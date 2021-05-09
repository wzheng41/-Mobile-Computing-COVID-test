package com.example.assignment1mc.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class SimpleMovingAverage {

    // queue used to store list so that we get the average
    private final Queue<Float> dataSet = new LinkedList<>();
    private final int period;
    private float sum;

    // constructor to initialize period
    public SimpleMovingAverage(int period) {
        this.period = period;
    }

    // function to add new data in the
    // list and update the sum so that
    // we get the new mean
    public void addData(float num) {
        sum += num;
        dataSet.add(num);

        // Updating size so that length
        // of data set should be equal
        // to period as a normal mean has
        if (dataSet.size() > period) {
            sum -= dataSet.remove();
        }
    }

    // function to calculate mean
    public float getMean() {
        return sum / period;
    }

    public static ArrayList<Float> simpleMovingAverage(ArrayList<Float> bitmapArray) {

        //Assigning Period
        int per = 21;
        SimpleMovingAverage obj = new SimpleMovingAverage(per);

        ArrayList<Float> movingAvgArray = new ArrayList<>();
        for (Float aFloat : bitmapArray) {
            obj.addData(aFloat);
            movingAvgArray.add(obj.getMean());
        }

        return movingAvgArray;
    }
}
