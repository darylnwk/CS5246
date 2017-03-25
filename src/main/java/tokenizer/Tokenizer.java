package tokenizer;

import opennlp.tools.tokenize.SimpleTokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Tokenizer {
    /**
     * This function uses OpenNLP SimpleTokenizer to tokenize the document
     *
     * SimpleTokenizer tokenize every word and punctuations into tokens
     * Eg. This is a CS5246 project.
     * ['This', 'is', 'a', 'CS5246', 'project', '.']
     *
     * @param filePath  - path to document
     * @return          - tokenized document
     */
    public String[] tokenize(String filePath) {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

        String document = "";
        try {
            // Extract contents in the document
            document = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokenizer.tokenize(document);
    }
}
