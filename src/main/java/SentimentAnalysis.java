import indexer.Indexer;
import processor.Processor;
import sentiment.Sentiment;
import train.Train;

import java.io.IOException;

public class SentimentAnalysis {
    private static final String INDEX = "index";
    private static final String TRAIN = "train";
    private static final String RUN = "run";

    public static void main(String[] args) throws IOException {
        if (args.length == 1 && args[0].equals(INDEX)) {
            Indexer indexer = new Indexer();

            for (Sentiment sentiment : Sentiment.values()) {
                indexer.createIndex(sentiment);
            }
        } else if (args.length == 1 && args[0].equals(TRAIN)) {
            Train train = new Train();
            train.start();
        } else if (args.length == 2 && args[0].equals(RUN)) {
            Processor processor = new Processor();
            processor.start(args[1]);
        } else {
            System.out.println("Usage: <index|train|run /path/to/folder/containing/files/to/analyze>");
        }
    }
}
