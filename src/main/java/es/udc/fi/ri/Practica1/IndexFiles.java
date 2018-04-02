package es.udc.fi.ri.Practica1;

import java.io.File;
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
import java.util.regex.Pattern;

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
	
	static void Indexer(String indexPath, String path , String mode, boolean addIndexes, boolean multiThread) {
		if (addIndexes && multiThread) {
			//opcion add y multi
		} else if (addIndexes) {
			addIndexesIndexing(indexPath, path, mode);
		} else if (multiThread) {
			// multi
		} else {
			simpleIndexing(indexPath, path, mode);
		}
	}
	
	private static void simpleIndexing(String indexPath, String path , String mode){
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
	        iwc.setOpenMode(Utils.getOpenMode(mode));	//modo de crear el indice
	        IndexWriter writer = new IndexWriter(dir, iwc);	//inicializar indexwriter
	        indexDocs(writer, docDir);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}
	
	private static void addIndexesIndexing(String indexPath, String path , String mode) {
		final Path docDir = Paths.get(path);
	    if (!Files.isReadable(docDir)) {
	      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }
	    try {
	    	int j = indexBySubFolders(docDir, mode, indexPath);
	    	System.out.println(j);
			String folderIndex = indexPath+"\\FinalIndex";
			//abre ruta para almacenar los indices
	        Analyzer analyzer = new StandardAnalyzer(); //analizador
	        IndexWriterConfig iwc = new IndexWriterConfig(analyzer); //establece configuracion para determinar modelo de indexacion
	        iwc.setOpenMode(Utils.getOpenMode(mode));
	        Directory[] directories = obtainPartialIndex(Paths.get(indexPath),j);
	        new File(folderIndex).mkdir();
	        Directory dir = FSDirectory.open(Paths.get(folderIndex)); 
	        IndexWriter indexWriter = new IndexWriter(dir, iwc);
	        indexWriter.addIndexes(directories);
	        indexWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Directory[] obtainPartialIndex(Path path, int nFolders) throws IOException{
		Directory [] directories = new Directory[nFolders];
		int i = 0;
		if (Files.isDirectory(path)) {
			File[] files = new File(path.toString()).listFiles();
			for (File file : files) {
				String[] parts = file.toString().split(Pattern.quote(File.separator));
        		String folder= parts[parts.length-1]; 
				if (file.isDirectory() && !folder.equals("FinalIndex")) {
					System.out.println(file.toString());
	        		directories[i] = FSDirectory.open(file.toPath());
	        		i++;
	        	}
			}
		}
		return directories;
	}
	
	private static int indexBySubFolders(Path path, String mode, String indexPath) throws IOException {
		int i = 0;
		if (Files.isDirectory(path)) {
			File[] files = new File(path.toString()).listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					i++;
	        		String[] parts = file.toString().split(Pattern.quote(File.separator));
	        		String folder= parts[parts.length-1]; 
	        		String newFolder = indexPath+"\\"+folder;
	        		boolean newFile = new File(newFolder).mkdir();
	        		if (newFile) {
	        			simpleIndexing(newFolder, file.toString(), mode);
	        		}
				}
			}
		}
		return i;
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
		// THREAD
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
		article = new Article(tags.get(0), tags.get(2), tags.get(1), 
			DateTools.dateToString(Utils.formatDate(tags.get(4)), Resolution.MILLISECOND), 
			tags.get(3), tags.get(5).toString(), tags.get(6).toString(), path, hostname, thread);
		return article; 
	}
	
	/** Indexes a single document */
	private static void indexDoc(IndexWriter writer, Article article) {
		try {
			Document doc = new Document();
			FieldType type = new FieldType();
			type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
			type.setStored(true);
			type.setTokenized(true);
			type.setStoreTermVectors(true);
			type.setStoreTermVectorPositions(true);
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
			//INDEXAR OLDID Y NEWID
			doc.add(new Field("OldId",article.getOldId(), type));
			doc.add(new Field("NewId",article.getNewId(), type));
	      
			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
				//System.out.println("adding " + article.getPath());
				writer.addDocument(doc);
			} else {
				//System.out.println("updating " + article.getPath());
				writer.updateDocument(new Term("path", article.getPath()), doc);
			}
	    } catch (Exception e) {
			// TODO: handle exception
		}
	}
}
