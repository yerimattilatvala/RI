package es.udc.fi.ri.Practica1;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import Parser.Reuters21578Parser;

public class IndexFiles {
	
	static void Indexer(String indexPath, Path docPath, String mode) {
		//INDEXADOR
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
	        indexDocs(writer, docPath);
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
	
	static void indexDocs(final IndexWriter writer, Path path) throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	        	try {
	        		if (Utils.isSgmExtension(file))
	        			indexDoc(writer, file);
	              } catch (IOException ignore) {
	            	  	// don't index files that can't be read.
	            	  	System.out.println("Error al intentar indexar el documento : "+ file.toString()+ " \n");
	              }
	              return FileVisitResult.CONTINUE;
	        }
			});
		} else {
			if (Utils.isSgmExtension(path))
				indexDoc(writer, path);
	    }
	  }
	
	/** Indexes a single document */
	static void indexDoc(IndexWriter writer, Path file) throws IOException {
		try {
			// make a new, empty document
			Document doc = new Document();
			StringBuffer strBuffer = new StringBuffer(Utils.readFile(file.toString()));
			List<List<String>> data = Reuters21578Parser.parseString(strBuffer);
			FieldType type = new FieldType();
			type.setStored(true);
			type.setTokenized(true);
			type.setStoreTermVectors(true);
			// Add the path of the file as a field named "path".  Use a
			// field that is indexed (i.e. searchable), but don't tokenize 
			// the field into separate words and don't index term frequency
			// or positional information:
			Field pathField = new StringField("PathSgm", file.toString(), Field.Store.YES);
			doc.add(pathField);
			//INDEXAMOS EL HOST
			Field hostName = new StringField("Hostname",InetAddress.getLocalHost().getHostName(),  Field.Store.YES);
			doc.add(hostName);
			//INDEXAMOS EL THREAD
			Thread thread = Thread.currentThread();
			Field currentThread = new StringField("Thread",thread.toString(),  Field.Store.YES);
			doc.add(currentThread);
			//CREAMOS ITERADOR PARA LOS CAMPOS DEL ARTICULO
			Iterator<List<String>> it1 = data.iterator();
			try {
				while(it1.hasNext()) {
					List<String> fields = it1.next();
					doc.add(new Field("Title",fields.get(0), type));
					doc.add(new Field("Body",fields.get(1), type));
					doc.add(new Field("Topics",fields.get(2), type));
					doc.add(new Field("Dateline",fields.get(3), type));
					//DATE
					doc.add(new StringField("DATE", DateTools.dateToString(Utils.formatDate(fields.get(4)), Resolution.MILLISECOND), Field.Store.YES));	
					//FALTAN INDEXAR OLDID Y NEWID
					doc.add(new Field("OldId",fields.get(5), type));
					doc.add(new Field("NewId",fields.get(6), type));
				}
			} catch (Exception e) {
				System.out.println(" caught a " + e.getClass() +
					       "\n with message: " + e.getMessage());
			}
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
				System.out.println("adding " + file);
				writer.addDocument(doc);
			} else {
				// Existing index (an old copy of this document may have been indexed) so 
				// we use updateDocument instead to replace the old one matching the exact 
				// path, if present:
				System.out.println("updating " + file);
				writer.updateDocument(new Term("path", file.toString()), doc);
			}
	    } catch (Exception e) {
			// TODO: handle exception
		}
	}
}
