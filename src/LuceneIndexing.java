
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LuceneIndexing {
    static Path pathSource = Paths.get("D:/Univ/ID/HW2/text");
	static Path pathIdx = Paths.get("D:/Univ/ID/HW2/LuceneIndex");
	static String fieldNome = "nome";
	static String fieldContenuto = "contenuto";
    public static void homework2_index() {

        try {
        	Directory directory = FSDirectory.open(pathIdx);
            homework2_IndexDocs(pathSource, directory, null); // new SimpleTextCodec());

            directory.close();

        } catch (Exception e){

            // Deal with e as you please.
            //e may be any type of exception at all.
        	System.out.println("Exception!");

        }
    }

    private static void homework2_IndexDocs(Path pathSourceDirectory, Directory indexDirectory, Codec codec) throws IOException {
    	Analyzer defaultAnalyzer = new StandardAnalyzer();
        CharArraySet italianStopWords = new CharArraySet(Arrays.asList("in", "dei", "di"), true);
        CharArraySet englishStopWords = new CharArraySet(Arrays.asList(
        		"a", "an", "and", "are", "as", "at", "be", "but", "by",
  		      "for", "if", "in", "into", "is", "it",
  		      "no", "not", "of", "on", "or", "such",
  		      "that", "the", "their", "then", "there", "these",
  		      "they", "this", "to", "was", "will", "with"), true);
        CharArraySet stopWords = new CharArraySet(englishStopWords, true);
        stopWords.addAll(italianStopWords);

        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        perFieldAnalyzers.put(fieldContenuto, new StandardAnalyzer(stopWords));
        perFieldAnalyzers.put(fieldNome, new WhitespaceAnalyzer());

        Analyzer analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        if (codec != null) {
            config.setCodec(codec);
        }
        IndexWriter writer = new IndexWriter(indexDirectory, config);
        writer.deleteAll();

		DirectoryStream<Path> stream;
		try {
        	int nFiles = 0;
			stream = Files.newDirectoryStream(pathSourceDirectory, "*.txt");
    	    for (Path entry: stream) {
    	    	nFiles++;

    	        Document doc1 = new Document();
    	        doc1.add(new TextField(fieldNome, entry.getFileName().toString(), Field.Store.YES));
    	        
    	        try {
    	        	String content = Files.readString(entry);
    	        	doc1.add(new TextField(fieldContenuto, content, Field.Store.NO));
    	        	writer.addDocument(doc1);
        	        System.out.println(nFiles + ": " + entry.getFileName());
    	        }   catch (Exception e){
    	            // Deal with e as you please.
    	            //e may be any type of exception at all.
        	        System.out.println(nFiles + ": " + entry.getFileName() + " Discard file due to Exception!");
    	        }
    	    }
    	} catch (IOException x) {
    	    // IOException can never be thrown by the iteration.
    	    // In this snippet, it can // only be thrown by newDirectoryStream.
			x.printStackTrace();
    	}        
        writer.commit();
        writer.close();
    }

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		homework2_index();
		long end = System.currentTimeMillis();

		float sec = (end - start) / 1000F;
		System.out.println(sec + " seconds");
	}
}
