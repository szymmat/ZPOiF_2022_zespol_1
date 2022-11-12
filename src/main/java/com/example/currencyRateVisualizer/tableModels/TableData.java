package com.example.currencyRateVisualizer.tableModels;

public class TableData {
    private String name;
    private double earlyRate;
    private double currentRate;
    private double increase;

    public TableData(String name, double earlyRate, double currentRate, double increase) {
        this.name = name;
        this.earlyRate = earlyRate;
        this.currentRate = currentRate;
        this.increase = increase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getEarlyRate() {
        return earlyRate;
    }

    public void setEarlyRate(double earlyRate) {
        this.earlyRate = earlyRate;
    }

    public double getCurrentRate() {
        return currentRate;
    }

    public void setCurrentRate(double currentRate) {
        this.currentRate = currentRate;
    }

    public double getIncrease() {
        return increase;
    }

    public void setIncrease(double increase) {
        this.increase = increase;
    }

}
