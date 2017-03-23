import indexer.Indexer;
import sentiment.Sentiment;
import train.Train;

import java.io.IOException;

public class SentimentAnalysis {
    private static final String INDEX = "index";
    private static final String TRAIN = "train";

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: index|train");
        }

        if (args.length == 1 && args[0].equals(INDEX)) {
            Indexer indexer = new Indexer();

            for (Sentiment sentiment : Sentiment.values()) {
                indexer.createIndex(sentiment);
            }
        } else if (args.length == 1 && args[0].equals(TRAIN)) {
            Train train = new Train();
            train.start();
        }
    }
}
