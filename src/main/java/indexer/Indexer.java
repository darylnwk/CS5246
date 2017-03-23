package indexer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import sentiment.Sentiment;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Daryl on 24/3/2017.
 */
public class Indexer {
    private static final String CONTENTS_FIELD = "contents";
    private static final String FILEPATH_FIELD = "filepath";
    private static final String DATASET_DIRECTORY = "dataset/";

    public void createIndex(Sentiment sentiment) throws IOException {
        System.out.print("Creating index for " + sentiment.name() + " class... ");

        SimpleFSDirectory indexDirectory = new SimpleFSDirectory(
                new File(DATASET_DIRECTORY + sentiment.name().toLowerCase() + "/index").toPath());
        IndexWriter indexWriter = new IndexWriter(
                indexDirectory,
                new IndexWriterConfig(new StandardAnalyzer()));

        File dir = new File(DATASET_DIRECTORY + sentiment.name().toLowerCase());
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) { continue; }

            Field contentField = new TextField(CONTENTS_FIELD, new FileReader(file));
            Field filePathField = new TextField(FILEPATH_FIELD, file.getCanonicalPath(), Field.Store.YES);

            Document document = new Document();
            document.add(contentField);
            document.add(filePathField);

            indexWriter.addDocument(document);
        }

        indexWriter.close();

        System.out.println("Done");
        System.out.println();
    }

    public int query(Sentiment sentiment, String term) throws IOException {
        Directory indexDirectory = FSDirectory.open(
                new File(DATASET_DIRECTORY + sentiment.name().toLowerCase() + "/index").toPath());
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new TermQuery(new Term(CONTENTS_FIELD, term));

        return indexSearcher.count(query);
    }
}
