package com.nus.sentimentanalysis.training;

import org.apache.commons.io.FileUtils;
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

import static com.nus.sentimentanalysis.shared.Utils.INDEX_DIR;
import static com.nus.sentimentanalysis.shared.Utils.PROCESSED_DIR;

/**
 * Used by Trainer class
 */
public class Indexer {
    private static final String CONTENTS_FIELD = "contents";
    private static final String FILE_PATH_FIELD = "file_path";

    public void createIndex(String basePath) {
        System.out.print("Creating index for " + basePath + " START...");
        String indexPath = basePath + INDEX_DIR;
        File indexFolder = new File(indexPath);
        if (indexFolder.exists()) {
            try {
                FileUtils.deleteDirectory(indexFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File trainingSetDir = new File(basePath + PROCESSED_DIR);
        File[] files = trainingSetDir.listFiles();
        if (files == null) {
            throw new RuntimeException("No training data is found in " + trainingSetDir.getPath());
        }

        try (
                SimpleFSDirectory indexDirectory = new SimpleFSDirectory(indexFolder.toPath());
                IndexWriter indexWriter = new IndexWriter(indexDirectory, new IndexWriterConfig(new StandardAnalyzer()))
        ) {
            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }

                Field contentField = new TextField(CONTENTS_FIELD, new FileReader(file));
                Field filePathField = new TextField(FILE_PATH_FIELD, file.getCanonicalPath(), Field.Store.YES);

                Document document = new Document();
                document.add(contentField);
                document.add(filePathField);
                indexWriter.addDocument(document);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(" >>>>> DONE");
    }

    public int query(String path, String term) throws IOException {
        Directory indexDirectory = FSDirectory.open(new File(path).toPath());
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new TermQuery(new Term(CONTENTS_FIELD, term));

        return indexSearcher.count(query);
    }
}
