package train;

import sentiment.Sentiment;
import indexer.Indexer;
import tokenizer.Tokenizer;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

class ConditionProbability {
    private double[] probability;

    public ConditionProbability(int size) {
        probability = new double[size];
    }

    public void setProbability(Sentiment sentiment, double probability) {
        this.probability[sentiment.ordinal()] = probability;
    }

    public double[] getProbability() {
        return this.probability;
    }
}

public class Train {
    private static final String NEWLINE = "\n";
    private static final String WHITESPACE = " ";
    private static final String TRAIN_FILE = "train.txt";
    private static final String DATASET_DIRECTORY = "dataset/";

    private HashSet<String> vocab = new HashSet<>();
    private HashMap<String, ConditionProbability> condProb = new HashMap<>();

    private Tokenizer tokenizer;
    private Indexer indexer;

    public Train() {
        this.tokenizer = new Tokenizer();
        this.indexer = new Indexer();
    }

    public void start() throws IOException {
        int numOfDocumentClasses = Sentiment.values().length;

        int totalNumOfFiles = 0;
        int[] numOfDocsInClasses = new int[numOfDocumentClasses];
        for (Sentiment sentiment : Sentiment.values()) {
            File file = new File(DATASET_DIRECTORY + sentiment.name().toLowerCase() + "/index");
            if (!file.exists()) {
                System.out.println("Index folder for " + sentiment.name() + " class not found!");
                indexer.createIndex(sentiment);
            }

            File folder = new File(DATASET_DIRECTORY + sentiment.name().toLowerCase());
            numOfDocsInClasses[sentiment.ordinal()] = folder.listFiles().length;
            totalNumOfFiles += folder.listFiles().length;

            extractVocabulary(folder.listFiles());
        }

        // for debugging
        // System.out.println("Total # of files: " + totalNumOfFiles);
        // System.out.println("Total # of words: " + vocab.size());

        double[] prior = new double[numOfDocumentClasses];
        for (Sentiment sentiment : Sentiment.values()) {
            System.out.print("Training " + sentiment.name() + " class... ");

            int numOfDocsInClass = numOfDocsInClasses[sentiment.ordinal()];

            prior[sentiment.ordinal()] = (double) numOfDocsInClasses[sentiment.ordinal()] / totalNumOfFiles;

            Iterator<String> vocabIterator = vocab.iterator();

            while (vocabIterator.hasNext()) {
                String term = vocabIterator.next();

                // Skip non-words
                if (!term.matches("\\w+") || term.equals("_")) {
                    continue;
                }

                int numOfDocsInClassContainingTerm = countDocsInClassContainingTerm(sentiment, term);
                double probability = (double) (numOfDocsInClassContainingTerm + 1) / (numOfDocsInClass + 2);
                if (condProb.containsKey(term)) {
                    condProb.get(term).setProbability(sentiment, probability);
                } else {
                    ConditionProbability cp = new ConditionProbability(numOfDocumentClasses);
                    cp.setProbability(sentiment, probability);

                    condProb.put(term, cp);
                }
            }

            System.out.println("Done");
            System.out.println();
        }

        writeToFile(prior);
    }

    private void extractVocabulary(File[] listOfFiles) {
        for (File file : listOfFiles) {
            if (file.isDirectory()) { continue; }

            String[] tokens = tokenizer.tokenize(file.getPath());

            for (String token : tokens) {
                // Skip non-words
                if (!token.matches("\\w+") || token.equals("_")) {
                    continue;
                }

                vocab.add(token);
            }
        }
    }

    private int countDocsInClassContainingTerm(Sentiment sentiment, String term) throws IOException {
           return indexer.query(sentiment, term);
    }

    private void writeToFile(double[] prior) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TRAIN_FILE),"utf-8"))) {
            Iterator<String> vocabIterator = vocab.iterator();
            while (vocabIterator.hasNext()) {
                String term = vocabIterator.next();
                double[] probability = condProb.get(term).getProbability();

                writer.write(term);
                writer.write(WHITESPACE);
                for (int i = 0; i < prior.length; i++) {
                    writer.write(Double.toString(prior[i]));
                    writer.write(WHITESPACE);
                }

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
