package com.nus.sentimentanalysis.training;

import com.nus.sentimentanalysis.shared.ConditionProbability;
import com.nus.sentimentanalysis.shared.Sentiment;
import com.nus.sentimentanalysis.shared.Tokenizer;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Trainer {
    private static final String NEWLINE = "\n";
    private static final String WHITESPACE = " ";
    private static final String TRAIN_FILE = "train.txt";
    private static final String DATASET_DIRECTORY = "data/";
    private static final String TERM_PATTERN = "\\w+";

    private int totalDoc = 0; // count total document
    private HashSet<String> vocab = new HashSet<>(); // extract vocabulary
    private int[] docCountInClass; // count docs in class
    private HashMap<String, ConditionProbability> condProb = new HashMap<>();

    private Tokenizer tokenizer;
    private Indexer indexer;

    public Trainer() {
        this.tokenizer = new Tokenizer();
        this.indexer = new Indexer();
    }

    public void start() throws IOException {
        int totalClass = Sentiment.values().length;
        // #1: ExtractVocabulary(D)
        extractVocabulary();

        // for debugging
        // System.out.println("Total # of files: " + totalNumOfFiles);
        // System.out.println("Total # of words: " + vocab.size());

        double[] prior = new double[totalClass];
        for (Sentiment sentiment : Sentiment.values()) {
            System.out.println("Training " + sentiment.name() + ">>>>> START");

            int classId = sentiment.ordinal();

            int docClassCount = docCountInClass[classId];
            prior[classId] = (double) docClassCount / totalDoc;

            // #6: for each t -> V
            for (String term : vocab) {
                // #7: N(ct) = countDocsInClassContainingTerm
                int docClassTermCount = countDocsInClassContainingTerm(sentiment, term);

                double probability = (double) (docClassTermCount + 1) / (docClassCount + 2);
                if (probability > 1) {
                    System.out.println("Term: " + term + " | N(ct) = " + docClassTermCount + " N(c) = " + docClassCount);
                    probability = 1;
                }
                if (condProb.containsKey(term)) {
                    ConditionProbability cp = condProb.get(term);
                    cp.setProbability(sentiment, probability);

                } else {
                    ConditionProbability cp = new ConditionProbability(totalClass);
                    cp.setProbability(sentiment, probability);
                    condProb.put(term, cp);
                }
            }

            System.out.println("Training " + sentiment.name() + ">>>>> DONE");
        }

        writeToFile(prior);
    }

    private void extractVocabulary() {
        docCountInClass = new int[Sentiment.values().length];

        for (Sentiment sentiment : Sentiment.values()) {
            String basePath = DATASET_DIRECTORY + sentiment.name().toLowerCase();
            File indexFolder = new File(basePath + "/index");
            if (!indexFolder.exists()) {
                System.out.println("Index folder for " + sentiment.name() + " class not found!");
                indexer.createIndex(sentiment);
            }

            File folder = new File(basePath);
            File[] files = folder.listFiles();
            if (files != null) {
                // #4: for each class, count doc in class
                int count = 0;
                for (File file : files) {
                    if (file.isDirectory()) {
                        continue;
                    }
                    count++;
                    totalDoc++;
                    String[] tokens = tokenizer.tokenize(file.getPath());
                    for (String token : tokens) {
                        // Skip non-words
                        if (!token.matches(TERM_PATTERN) || token.equals("_")) {
                            continue;
                        }
                        vocab.add(token);
                    }
                }
                docCountInClass[sentiment.ordinal()] = count;
                System.out.println("Total docs for " + sentiment.name() + " is: " + count);
            }
        }
    }

    private int countDocsInClassContainingTerm(Sentiment sentiment, String term) throws IOException {
           return indexer.query(sentiment, term);
    }

    private void writeToFile(double[] prior) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TRAIN_FILE),"utf-8"))) {
            for (int i = 0; i < prior.length; i++) {
                writer.write(Double.toString(prior[i]));
                writer.write(WHITESPACE);
            }
            writer.write(NEWLINE);

            Iterator<String> vocabIterator = vocab.iterator();
            while (vocabIterator.hasNext()) {
                String term = vocabIterator.next();
                double[] probability = condProb.get(term).getProbability();

                writer.write(term);
                writer.write(WHITESPACE);

                for (int i = 0; i < probability.length; i++) {
                    writer.write(Double.toString(probability[i]));
                    writer.write(WHITESPACE);
                }

                writer.write(NEWLINE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
