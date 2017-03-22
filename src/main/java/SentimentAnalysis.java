import tokenizer.Tokenizer;
import train.Train;

import java.io.IOException;

public class SentimentAnalysis {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: Please include name of file");
        }

        String fileName = args[0];

        Train train = new Train();
        train.start();
    }
}
