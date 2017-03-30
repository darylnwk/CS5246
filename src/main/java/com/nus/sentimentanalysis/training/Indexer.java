package com.nus.sentimentanalysis.training;

import com.nus.sentimentanalysis.shared.Sentiment;
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Used by Trainer class
 */
public class Indexer {
    private static final String CONTENTS_FIELD = "contents";
    private static final String FILE_PATH_FIELD = "file_path";
    private static final String DATASET_DIRECTORY = "dataset/";

    public void createIndex(Sentiment sentiment) {
        System.out.print("Creating index for " + sentiment.name() + " class >>>>> START");

        File dir = new File(DATASET_DIRECTORY + sentiment.name());
        File[] files = dir.listFiles();
        if (files == null) {
            throw new RuntimeException("No data set is found in " + DATASET_DIRECTORY);
        }

        try (
                SimpleFSDirectory indexDirectory = new SimpleFSDirectory(
                        new File(DATASET_DIRECTORY + sentiment.name() + "/index").toPath());
                IndexWriter indexWriter = new IndexWriter(
                        indexDirectory, new IndexWriterConfig(new StandardAnalyzer()))
        ) {
            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }

                Field contentField = new TextField(CONTENTS_FIELD, new FileReader(file));
                // TODO(xzhang): why do we need to add file path?
                Field filePathField = new TextField(FILE_PATH_FIELD, file.getCanonicalPath(), Field.Store.YES);

                Document document = new Document();
                document.add(contentField);
                document.add(filePathField);

                indexWriter.addDocument(document);
            }

            indexWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print("Creating index for " + sentiment.name() + " class >>>>> DONE");
    }

    public int query(Sentiment sentiment, String term) throws IOException {
        Directory indexDirectory = FSDirectory.open(
                new File(DATASET_DIRECTORY + sentiment.name() + "/index").toPath());
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new TermQuery(new Term(CONTENTS_FIELD, term));

        return indexSearcher.count(query);
    }
}
