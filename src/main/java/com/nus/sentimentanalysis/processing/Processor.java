package com.nus.sentimentanalysis.processing;

import com.nus.sentimentanalysis.shared.Sentiment;
import com.nus.sentimentanalysis.shared.ConditionProbability;
import com.nus.sentimentanalysis.shared.Tokenizer;
import com.nus.sentimentanalysis.training.Trainer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Processor {
    private HashSet<String> vocab = new HashSet<>();
    private HashMap<String, ConditionProbability> condProb = new HashMap<>();
    private double[] prior;

    private Tokenizer tokenizer;

    public Processor() throws IOException {
        this.tokenizer = new Tokenizer();
        loadData();
    }

    public void start(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            return;
        }

        int count[] = new int[2];

        // Can add filter to filter out .txt files only
        File[] files = folder.listFiles();
        for (File file : files) {
            if (naiveBayes(file.getPath())) {
                count[1]++;
            } else {
                count[0]++;
            }
        }

        System.out.println("# of negative: " + count[0]);
        System.out.println("# of positive: " + count[1]);
    }

    private void loadData() throws IOException {
        File file = new File("train.txt");
        if (!file.exists()) {
            System.out.println("Training file not found!");
            System.out.println("Starting training...");
            Trainer trainer = new Trainer();
            trainer.start();
        }

        System.out.print("Loading data... ");
        int numOfDocumentClasses = Sentiment.values().length;
        prior = new double[numOfDocumentClasses];

        String[] lines = new String(Files.readAllBytes(Paths.get("train.txt"))).split("\n");
        for (int i = 0; i < lines.length; i++) {
            String[] tokens = lines[i].split(" ");
            if (i == 0) {
                for (int j = 0; j < numOfDocumentClasses; j++) {
                    prior[j] = Double.parseDouble(tokens[j]);
                }

                // System.out.println(prior[0] + " " + prior[1]);
            } else {
                double[] probability = new double[numOfDocumentClasses];
                String term = tokens[0];
                vocab.add(term);
                for (int j = 1; j < tokens.length; j++) {
                    probability[j - 1] = Double.parseDouble(tokens[j]);
                }

                ConditionProbability conditionProbability = new ConditionProbability(probability);
                condProb.put(term, conditionProbability);

                // System.out.println(term + " " + condProb.get(term).getProbability()[0] + " " + condProb.get(term).getProbability()[1]);
            }
        }

        System.out.println("Done");
    }

    /**
     * This function uses Bernoulli model
     * @param filePath
     */
    public boolean naiveBayes(String filePath) {
        System.out.println("Analyzing " + filePath);

        double[] scores = new double[Sentiment.values().length];
        String[] termsInDoc = tokenizer.tokenize(filePath);

        // Add to HashSet for fast search
        HashSet<String> termsInDocSet = new HashSet<>();
        for (String termInDoc : termsInDoc) {
            termsInDocSet.add(termInDoc.toLowerCase());
        }

        for (Sentiment sentiment : Sentiment.values()) {
            int index = sentiment.ordinal();
            scores[index] = Math.log(prior[index]);

            Iterator<String> vocabIterator = vocab.iterator();
            while (vocabIterator.hasNext()) {
                String term = vocabIterator.next();
                double conditionalProbability = condProb.get(term).getProbability(sentiment);

                if (termsInDocSet.contains(term)) {
                    scores[index] += Math.log(conditionalProbability);
                } else {
                    if (conditionalProbability != 1) {
                        scores[index] += Math.log(1 - conditionalProbability);
                    }
                }
            }
        }

        double max = scores[0];
        for (int i = 1; i < scores.length; i++) {
            max = Math.max(max, scores[i]);
        }

//        System.out.println("Sentiment values: " + scores[0] + ", " + scores[1]);
//        System.out.println();

        return (scores[0] < scores[1]);
    }

    /**
     * This function negates all tokens following a "not"
     * @param tokens
     */
    private void negationHandling(String[] tokens) {
        boolean negate = false;
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("not")) {
                negate = !negate;
                continue;
            }

            if (negate) {
                tokens[i] = negate(tokens[i]);
            }
        }
    }

    private String negate(String token) {
        return "not_" + token;
    }
}
