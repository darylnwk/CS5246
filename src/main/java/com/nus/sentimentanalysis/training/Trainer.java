package com.nus.sentimentanalysis.training;

import com.nus.sentimentanalysis.shared.ConditionProbability;
import com.nus.sentimentanalysis.shared.Sentiment;
import com.nus.sentimentanalysis.shared.Tokenizer;
import com.nus.sentimentanalysis.shared.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

import static com.nus.sentimentanalysis.shared.Utils.DATASET_DIRECTORY;
import static com.nus.sentimentanalysis.shared.Utils.INDEX_DIR;
import static com.nus.sentimentanalysis.shared.Utils.getSentimentBaseDir;

public class Trainer {
    public static final String TRAIN_FILE = "output/train.txt";

    private static final String NEWLINE = "\n";
    private static final String WHITESPACE = " ";


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
        System.out.println("Start training...");

        // #1: ExtractVocabulary(D)
        extractVocabulary();

        int totalClass = Sentiment.values().length;
        double[] prior = new double[totalClass];

        for (Sentiment sentiment : Sentiment.values()) {
            System.out.println("Training " + sentiment.name() + ">>>>> START");

            createDocIndex(sentiment);

            int classId = sentiment.ordinal();

            int docClassCount = docCountInClass[classId];
            prior[classId] = (double) docClassCount / totalDoc;

            // #6: for each t -> V
            for (String term : vocab) {
                // #7: N(ct) = countDocsInClassContainingTerm
                int docClassTermCount = countDocsInClassContainingTerm(sentiment, term);

                double probability = (double) (docClassTermCount + 1) / (docClassCount + 2);
                if (probability > 1) {
                    System.err.println("Term: " + term + " | N(ct) = " + docClassTermCount + " N(c) = " + docClassCount);
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
            System.out.println();
        }

        writeToFile(prior);
    }

    private void extractVocabulary() {
        docCountInClass = new int[Sentiment.values().length];

        for (Sentiment sentiment : Sentiment.values()) {
            clearTrainingData(sentiment);

            String basePath = Utils.getSentimentBaseDir(sentiment);

            File folder = new File(basePath);
            File[] files = folder.listFiles();
            if (files != null) {
                // #4: for each class, count doc in class
                int count = 0;
                for (File file : files) {
                    if (file.isDirectory()) {
                        System.out.println("skip folder " + file.getName());
                        continue;
                    }
                    boolean negated = false;
                    StringBuilder sb = new StringBuilder();
                    count++;
                    totalDoc++;
                    String[] tokens = tokenizer.tokenize(file.getPath());
                    for (String token : tokens) {
                        if (Utils.isPunctuation(token)) {
                            negated = false; // reset when punctuation is encountered
                        }
                        else if (Utils.isNegationCue(token)) {
                            negated = true; // mark negated and append to next word
                        }
                        else if (negated) {
                            token = Utils.negate(token);
                            vocab.add(token);
                        }
                        else {
                            vocab.add(token);
                        }
                        sb.append(token);
                        sb.append(" ");
                    }

                    generateTrainingData(sb.toString(), file.getName(), sentiment);

                }
                docCountInClass[sentiment.ordinal()] = count;
                System.out.println("Total docs for " + sentiment.name() + " is: " + count);
            }
        }
    }

    private void clearTrainingData(Sentiment sentiment) {
        try {
            File processedDir = new File(Utils.getSentimentBaseDir(sentiment) + Utils.PROCESSED_DIR);
            if (processedDir.exists()) {
                FileUtils.deleteDirectory(processedDir);
            }
            processedDir.mkdir();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateTrainingData(String data, String filename, Sentiment sentiment) {
        try {
            // add processed file (with negation handling)
            String path = Utils.getSentimentBaseDir(sentiment);
            File newFile = new File(path + Utils.PROCESSED_DIR + filename);
            if (newFile.createNewFile()) {
                FileWriter fw = new FileWriter(newFile);
                fw.append(data);
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDocIndex(Sentiment sentiment) {
        indexer.createIndex(getSentimentBaseDir(sentiment));
    }

    private int countDocsInClassContainingTerm(Sentiment sentiment, String term) throws IOException {
        return indexer.query(getSentimentBaseDir(sentiment) + INDEX_DIR, term);
    }

    private void writeToFile(double[] prior) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TRAIN_FILE), "utf-8"))) {
            for (double aPrior : prior) {
                writer.write(Double.toString(aPrior));
                writer.write(WHITESPACE);
            }
            writer.write(NEWLINE);

            for (String term : vocab) {
                double[] probability = condProb.get(term).getProbability();

                writer.write(term);
                writer.write(WHITESPACE);

                for (double aProbability : probability) {
                    writer.write(Double.toString(aProbability));
                    writer.write(WHITESPACE);
                }

                writer.write(NEWLINE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
