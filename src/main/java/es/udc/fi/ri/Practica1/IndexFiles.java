package es.udc.fi.ri.Practica1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import Parser.Reuters21578Parser;

public class IndexFiles {
	
	static void Indexer(String indexPath, String path , String mode) {
		//INDEXADOR
		final Path docDir = Paths.get(path);
	    if (!Files.isReadable(docDir)) {
	      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");
    	
			Directory dir = FSDirectory.open(Paths.get(indexPath)); //abre ruta para almacenar los indices
		
	        Analyzer analyzer = new StandardAnalyzer(); //analizador
	        IndexWriterConfig iwc = new IndexWriterConfig(analyzer); //establece configuracion para determinar modelo de indexacion
	        try {
				iwc.setOpenMode(Utils.getOpenMode(mode));	//modo de crear el indice
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        IndexWriter writer = new IndexWriter(dir, iwc);	//inicializar indexwriter
	        indexDocs(writer, docDir);
	        try {
				writer.close();
			} catch (CorruptIndexException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Graceful message: exception " + e);
				e.printStackTrace();
			}
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private static void indexDocs(final IndexWriter writer, Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	        	if (Utils.isSgmExtension(file))
					fileToArticle(writer, file);
	              return FileVisitResult.CONTINUE;
	        }
			});
		} else {
			if (Utils.isSgmExtension(path))
				fileToArticle(writer, path);
	    }
	  }
	
	//to extract the articles of a file.sgm
	private static void fileToArticle(IndexWriter writer, Path file) {
		Article article = null;
		StringBuffer strBuffer = null;
		try {
			strBuffer = new StringBuffer(Utils.readFile(file.toString()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//INDEXAMOS EL THREAD
		Thread thread = Thread.currentThread();
		List<List<String>> data = Reuters21578Parser.parseString(strBuffer);
		Iterator<List<String>> it1 = data.iterator();
		while(it1.hasNext()) {
			List<String> fields = it1.next();
			try {
				article = listArticle(fields, file.toString(),
						InetAddress.getLocalHost().getHostName(), thread.toString(), article);
				indexDoc(writer, article);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static Article listArticle(List<String> tags, String path, String hostname, String thread,Article article) {
		//						Title		Topics		Body
		article = new Article(tags.get(0), tags.get(2), tags.get(1), 
				DateTools.dateToString(Utils.formatDate(tags.get(4)), Resolution.MILLISECOND), 
				tags.get(3), tags.get(5).toString(), tags.get(6).toString(), path, hostname, thread);
		//		Dateline		OldId		NewId
		
		return article; 
	}
	
	/** Indexes a single document */
	private static void indexDoc(IndexWriter writer, Article article) {
		try {
			// make a new, empty document
			Document doc = new Document();
			FieldType type = new FieldType();
			type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
			type.setStored(true);
			type.setTokenized(true);
			type.setStoreTermVectors(true);
			type.setStoreTermVectorPositions(true);
			// Add the path of the file as a field named "path".  Use a
			// field that is indexed (i.e. searchable), but don't tokenize 
			// the field into separate words and don't index term frequency
			// or positional information:
			Field pathField = new StringField("PathSgm", article.getPath(), Field.Store.YES);
			doc.add(pathField);
			//INDEXAMOS EL HOST
			Field hostName = new StringField("Hostname",article.getHostname(),  Field.Store.YES);
			doc.add(hostName);
			//INDEXAMOS EL THREAD
			Field currentThread = new StringField("Thread", article.getThread(),  Field.Store.YES);
			doc.add(currentThread);
			doc.add(new Field("Title",article.getTitle(), type));
			doc.add(new Field("Body",article.getBody(), type));
			doc.add(new Field("Topics",article.getTopics(), type));
			doc.add(new Field("Dateline",article.getDateline(), type));
			//DATE
			doc.add(new StringField("Date", article.getDate(), Field.Store.YES));	
			//FALTAN INDEXAR OLDID Y NEWID
			doc.add(new Field("OldId",article.getOldId(), type));
			doc.add(new Field("NewId",article.getNewId(), type));
			// Add the last modified date of the file a field named "modified".
			// Use a LongPoint that is indexed (i.e. efficiently filterable with
			// PointRangeQuery).  This indexes to milli-second resolution, which
			// is often too fine.  You could instead create a number based on
			// year/month/day/hour/minutes/seconds, down the resolution you require.
			// For example the long value 2011021714 would mean
			// February 17, 2011, 2-3 PM.
			//doc.add(new LongPoint("modified", lastModified));
	      
			// Add the contents of the file to a field named "contents".  Specify a Reader,
			// so that the text of the file is tokenized and indexed, but not stored.
			// Note that FileReader expects the file to be in UTF-8 encoding.
			// If that's not the case searching for special characters will fail.
			//doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
	      
			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
				// New index, so we just add the document (no old document can be there):
				System.out.println("adding " + article.getPath());
				writer.addDocument(doc);
			} else {
				// Existing index (an old copy of this document may have been indexed) so 
				// we use updateDocument instead to replace the old one matching the exact 
				// path, if present:
				System.out.println("updating " + article.getPath());
				writer.updateDocument(new Term("path", article.getPath()), doc);
			}
	    } catch (Exception e) {
			// TODO: handle exception
		}
	}
}
