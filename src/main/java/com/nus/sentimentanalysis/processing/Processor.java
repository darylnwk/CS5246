package com.nus.sentimentanalysis.processing;

import com.nus.sentimentanalysis.shared.Sentiment;
import com.nus.sentimentanalysis.shared.ConditionProbability;
import com.nus.sentimentanalysis.shared.Tokenizer;
import com.nus.sentimentanalysis.shared.Utils;
import com.nus.sentimentanalysis.training.Trainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processor {
    private static final int POSITIVE_INDEX = 1;
    private static final int NEGATIVE_INDEX = 0;

    private HashSet<String> vocab = new HashSet<>();
    private HashMap<String, ConditionProbability> condProb = new HashMap<>();
    private double[] prior;

    private Tokenizer tokenizer;

    public Processor() throws IOException {
        this.tokenizer = new Tokenizer();
    }

    public void start(String folderPath) throws IOException {
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            System.err.println("Test data not found at " + folderPath);
            return;
        }
        File[] files = folder.listFiles();
        if (files == null) {
            System.err.println("Test data set is empty!");
            return;
        }

        loadTrainingResult(new File(Trainer.TRAIN_FILE));

        int count[] = new int[2];
        for (File file : files) {
            if (naiveBayes(file.getPath())) {
                count[POSITIVE_INDEX]++;
            } else {
                count[NEGATIVE_INDEX]++;
            }
        }

        System.out.println("# of positive: " + count[POSITIVE_INDEX]);
        System.out.println("# of negative: " + count[NEGATIVE_INDEX]);
    }

    private void loadTrainingResult(File file) throws IOException {
        if (!file.exists()) {
            System.out.println("Training file not found!");
            Trainer trainer = new Trainer();
            trainer.start();
        }

        System.out.print("Processor starts loading data... ");
        int classCount = Sentiment.values().length;
        prior = new double[classCount];

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();

            // first line for prior
            String[] prob = line.split(" ");
            for (int i = 0; i < prob.length; i++) {
                prior[i] = Double.parseDouble(prob[i]);
            }
            // the rest for tokens
            while ((line = br.readLine()) != null) {
                double[] probability = new double[classCount];
                String[] tokens = line.split(" ");
                String term = tokens[0];
                vocab.add(term);

                for (int j = 1; j < tokens.length; j++) {
                    probability[j - 1] = Double.parseDouble(tokens[j]);
                }
                ConditionProbability conditionProbability = new ConditionProbability(probability);
                condProb.put(term, conditionProbability);
            }
        }
        System.out.println("Done");
    }

    /**
     * This function uses Bernoulli model
     *
     * @param filePath document
     * @return true if positive
     */
    private boolean naiveBayes(String filePath) {
        System.out.println("Analyzing " + filePath);

        double[] scores = new double[Sentiment.values().length];
        String[] termsInDoc = tokenizer.tokenize(filePath);

        // convert to HashSet with negation handling
        boolean negated = false;
        HashSet<String> termSet = new HashSet<>();
        for (String term : termsInDoc) {
            term = term.toLowerCase();
            if (Utils.isPunctuation(term)) {
                negated = false; // reset when punctuation is encountered
            }
            else if (Utils.isNegationCue(term)) {
                negated = true; // mark negated and append to next word
            }
            else if (negated) {
                termSet.add(Utils.negate(term));
            }
            else {
                termSet.add(term);
            }
        }

        for (Sentiment sentiment : Sentiment.values()) {
            int index = sentiment.ordinal();
            scores[index] = Math.log(prior[index]);

            for (String term : vocab) {
                double conditionalProbability = condProb.get(term).getProbability(sentiment);

                if (termSet.contains(term)) {
                    scores[index] += Math.log(conditionalProbability);
                }
                else {
                    if (conditionalProbability != 1) {
                        scores[index] += Math.log(1 - conditionalProbability);
                    }
                }
            }
        }

        double max = scores[NEGATIVE_INDEX];
        for (int i = 1; i < scores.length; i++) {
            max = Math.max(max, scores[i]);
        }
        return scores[POSITIVE_INDEX] > scores[NEGATIVE_INDEX];
    }
}
