package com.nus.sentimentanalysis.shared;

public class ConditionProbability {
    // each ConditionProbability covers both Positive and Negative
    private double[] probability;

    public ConditionProbability(int size) {
        probability = new double[size];
    }

    public ConditionProbability(double[] probability) {
        this.probability = probability;
    }

    public void setProbability(Sentiment sentiment, double probability) {
        this.probability[sentiment.ordinal()] = probability;
    }

    public double[] getProbability() {
        return this.probability;
    }

    public double getProbability(Sentiment sentiment) {
        return this.probability[sentiment.ordinal()];
    }

    public double getProbability(int sentiment) {
        return this.probability[sentiment];
    }
}
