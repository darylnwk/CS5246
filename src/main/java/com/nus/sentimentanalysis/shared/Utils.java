package com.nus.sentimentanalysis.shared;

public class Utils {

    public static final String DATASET_DIRECTORY = "data/";
    public static final String INDEX_DIR = "/index";
    public static final String PROCESSED_DIR = "/processed/";

    public static String getSentimentBaseDir(Sentiment sentiment) {
        return DATASET_DIRECTORY + sentiment.name().toLowerCase();
    }

    public static String negate(String token) {
        return "not_" + token;
    }

    public static boolean isPunctuation(String str) {
        return str.matches("[!\"#$%&'()*+,-./:;<=>?@\\^_`{|}~]");
    }

    public static boolean isNegationCue(String str) {
        return str.equalsIgnoreCase("not") || str.contains("n't");
    }
}
