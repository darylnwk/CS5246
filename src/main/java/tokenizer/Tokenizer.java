package tokenizer;

import opennlp.tools.tokenize.SimpleTokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Tokenizer {
    public String[] tokenize(String fileName) {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokenizer.tokenize(contents);
    }
}
