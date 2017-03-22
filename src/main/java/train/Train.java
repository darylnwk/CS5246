package train;

import tokenizer.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

public class Train {
    private static final String[] DOCUMENT_CLASSES = new String[] {"POSITIVE", "NEGATIVE"};
    private static final String POSITIVE_DATASET_DIRECTORY = "dataset/pos";
    private static final String NEGATIVE_DATASET_DIRECTORY = "dataset/neg";

    private HashSet<String> vocab = new HashSet<>();

    private Tokenizer tokenizer;

    public Train() {
        this.tokenizer = new Tokenizer();
    }

    public void start() throws IOException {
        File positiveFolder = new File(POSITIVE_DATASET_DIRECTORY);
        File[] listOfPositiveFiles = positiveFolder.listFiles();
        int numOfPositiveFiles = listOfPositiveFiles.length;
        System.out.println("# of positive files: " + numOfPositiveFiles);
        extractVocabulary(listOfPositiveFiles);

        File negativeFolder = new File(NEGATIVE_DATASET_DIRECTORY);
        File[] listOfNegativeFiles = negativeFolder.listFiles();
        int numOfNegativeFiles = listOfNegativeFiles.length;
        System.out.println("# of negative files: " + numOfNegativeFiles);
        extractVocabulary(listOfNegativeFiles);

        int totalNumOfDocuments = numOfPositiveFiles + numOfNegativeFiles;
        System.out.println("# of files: " + totalNumOfDocuments);

        double priorPositive = (double) numOfPositiveFiles / totalNumOfDocuments;
        System.out.println("prior[positive]: " + priorPositive);

        double priorNegative = (double) numOfNegativeFiles / totalNumOfDocuments;
        System.out.println("prior[negative]: " + priorNegative);

        String textPositive = concatTextOfAllDocumentsInClass(listOfPositiveFiles);
        String textNegative = concatTextOfAllDocumentsInClass(listOfNegativeFiles);

        // Refer to slide 22 of lecture 6
        // TODO: count tokens of term, line 7 - 8

        // TODO: get cond probability, line 9 - 10
    }

    private void extractVocabulary(File[] listOfFiles) {
        for (File file : listOfFiles) {
            String[] tokens = tokenizer.tokenize(file.getPath());

            for (String token : tokens) {
                vocab.add(token);
            }
        }
    }

    private String concatTextOfAllDocumentsInClass(File[] listOfFiles) throws IOException {
        String text = "";

        for (File file : listOfFiles) {
            text += new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8);
            text += " ";
        }

        return text;
    }
}
