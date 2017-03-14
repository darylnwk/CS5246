import tokenizer.Tokenizer;

public class SentimentAnalysis {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: Please include name of file");
        }

        String fileName = args[0];

        Tokenizer tokenizer = new Tokenizer();
        String tokens[] = tokenizer.tokenize(fileName);

        System.out.println(tokens[0]);
    }
}
