
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
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
import java.nio.charset.StandardCharsets;
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
	static Boolean simpleTextCodec = false;
    public static void homework2_index() {

        try {
        	Directory directory = FSDirectory.open(pathIdx);
            homework2_IndexDocs(pathSource, directory, simpleTextCodec ? new SimpleTextCodec() : null);

            directory.close();

        } catch (Exception e){

            // Deal with e as you please.
            //e may be any type of exception at all.
        	System.out.println("Exception!");

        }
    }

	private static void homework2_IndexDocs(Path pathSourceDirectory, Directory indexDirectory, Codec codec) throws IOException {
		Analyzer defaultAnalyzer = new StandardAnalyzer();
		CharArraySet italianStopWords = new CharArraySet(Arrays.asList(
				"a","ai","al","ben","che","chi","con","cui","da","dei","del","di","due","e",
				"fra","giu","ha","hai","ho","il","in","io","la","le","lei","lo","lui","ma",
				"me","nei","no","noi","o","ora","piu","qua","qui","sei","sia","su","sul","te",
				"tra","tre","un","una","uno","va","vai","voi"), true);
		CharArraySet englishStopWords = new CharArraySet(Arrays.asList(
				"a", "an", "and", "are", "as", "at", "be", "but", "by",
				"for", "if", "in", "into", "is", "it",
				"no", "not", "of", "on", "or", "such",
				"that", "the", "their", "then", "there", "these",
				"they", "this", "to", "was", "will", "with"), true);
		CharArraySet stopWords = new CharArraySet(englishStopWords, true);
		stopWords.addAll(italianStopWords);

		Map<String, Analyzer> perFieldAnalyzers = new HashMap<String, Analyzer>();
		perFieldAnalyzers.put(fieldContenuto, new StandardAnalyzer(stopWords));
		perFieldAnalyzers.put(fieldNome, new WhitespaceAnalyzer());

		Analyzer analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		if (codec != null) {
			config.setCodec(codec);
		}
		IndexWriter writer = new IndexWriter(indexDirectory, config);
		writer.deleteAll();

		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(pathSourceDirectory, "*.txt");
			int nFiles = 0;
			int nFilesErr = 0;
			for (Path entry: stream) {
				nFiles++;

				Document doc1 = new Document();
				doc1.add(new TextField(fieldNome, entry.getFileName().toString(), Field.Store.YES));
				String content = "";
				try {
					content = Files.readString(entry);
				}   catch (Exception e1){
					try {
						content = Files.readString(entry, StandardCharsets.ISO_8859_1);
					}   catch (Exception e2){
						try {
							content = Files.readString(entry, StandardCharsets.US_ASCII);
						}   catch (Exception e3){
							// Deal with e as you please.
							// e2 may be any type of exception at all.
							System.out.println(nFiles + ": " + entry.getFileName() + " Discard file due to Exception: "+e3);
						}
					}
				}
				try {
					doc1.add(new TextField(fieldContenuto, content, Field.Store.NO));
					writer.addDocument(doc1);
					System.out.println(nFiles + ": " + entry.getFileName());
				}   catch (Exception e){
					// Deal with e as you please.
					//e may be any type of exception at all.
					System.out.println(nFiles + ": " + entry.getFileName() + " Discard file due to Exception!");
					nFilesErr++;
				}
			}
			System.out.println(nFiles + " processed files, " + nFilesErr + " discarded file"+(nFilesErr==1?"":"s")+".");
		} catch (IOException x) {
			// IOException can never be thrown by the iteration.
			// In this snippet, it can // only be thrown by newDirectoryStream.
			System.err.println("Exception: "+x);
		}
		writer.commit();
		writer.close();
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();

		// First argument Source directory
		// Second argument Index directory
		// Third argument, if starts by 't' or 'T' then set simpleTextCodec = true
		if (args.length > 0) {
			try {
				pathSource = Paths.get(args[0]);
			} catch (Exception e) {
				System.err.println("Exception parsing first argument " + args[0]);
				System.exit(1);
			}
		}
		if (args.length > 1) {
			try {
				pathIdx = Paths.get(args[1]);
			} catch (Exception e) {
				System.err.println("Exception parsing second argument " + args[1]);
				System.exit(2);
			}
		}
		if (args.length > 2) {
			try {
				simpleTextCodec =  (args[2].toLowerCase().charAt(0) == 't');
			} catch (Exception e) {
				System.err.println("Exception parsing third argument " + args[2]);
				System.exit(3);
			}
		}
		System.out.println("Source directory: "+ pathSource.toString());
		System.out.println("Index directory : "+ pathIdx.toString());
		if (simpleTextCodec) {
			System.out.println("SimpleTextCodec Index...");
		}
		homework2_index();
		long end = System.currentTimeMillis();

		int ms = Math.round(end - start);
		System.out.println("Elapsed time: "+ ms + " ms");
	}
}
