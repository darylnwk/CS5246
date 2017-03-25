package processor;

import condition_probability.ConditionProbability;
import opennlp.tools.cmdline.sentdetect.SentenceDetectorTool;
import sentiment.Sentiment;
import tokenizer.Tokenizer;
import train.Train;

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

    public void start(String folderpath) {
        File folder = new File(folderpath);
        if (!folder.isDirectory()) {
            return;
        }

        // Can add filter to filter out .txt files only
        File[] files = folder.listFiles();
        for (File file : files) {
            naiveBayes(file.getPath());
        }

    }

    private void loadData() throws IOException {
        File file = new File("train.txt");
        if (!file.exists()) {
            System.out.println("Training file not found!");
            System.out.println("Starting training...");
            Train train = new Train();
            train.start();
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
    public void naiveBayes(String filePath) {
        System.out.println("Analyzing " + filePath);

        double[] scores = new double[Sentiment.values().length];
        String[] termsInDoc = tokenizer.tokenize(filePath);

        // Add to HashSet for fast search
        HashSet<String> termsInDocSet = new HashSet<>();
        for (String termInDoc : termsInDoc) {
            termsInDocSet.add(termInDoc);
        }

        for (Sentiment sentiment : Sentiment.values()) {
            System.out.println(sentiment.name());
            scores[sentiment.ordinal()] = Math.log(prior[sentiment.ordinal()]);

            Iterator<String> vocabIterator = vocab.iterator();
            while (vocabIterator.hasNext()) {
                String term = vocabIterator.next();

                if (termsInDocSet.contains(term)) {
                    scores[sentiment.ordinal()] += Math.log(condProb.get(term).getProbability(sentiment));
                } else {
                    scores[sentiment.ordinal()] += Math.log(1 - condProb.get(term).getProbability(sentiment));
                }
            }
        }

        double max = scores[0];
        for (int i = 1; i < scores.length; i++) {
            max = Math.max(max, scores[i]);
        }

        System.out.println("Sentiment values: " + scores[0] + ", " + scores[1]);
        System.out.println("Max: " + max);
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
