package es.udc.fi.ri.Practica1;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class Utils {	
	
	// Metodo para extraer el valor de OldId y NewId del tag REUTERS
	public static String getIds(String id, String text) {
		String value = null;
		StringTokenizer tokenizer = new StringTokenizer(text);
		 while (tokenizer.hasMoreTokens()) {
			 String field = tokenizer.nextToken();
			 if (field.startsWith(id)) {
		    	  value = field;
		     }
		}
		if (id.equals("NEWID"))
			value = value.substring(id.length()+2, value.length()-2);
		else
			value = value.substring(id.length()+2, value.length()-1);
		return value;
	}
	
	static Date formatDate(String date) {
		Date date1 = null;
		SimpleDateFormat dt = new SimpleDateFormat("dd-MMM-YYYY HH:mm:ss.SS", Locale.US); 
		try {
			date1 = dt.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date1;
	}
	static OpenMode getOpenMode(String mode) throws Exception {
		switch (mode) {
		case "APPEND":
			return OpenMode.APPEND;
		case "CREATE":
			return OpenMode.CREATE;	
		case "CREATE_OR_APPEND":
			return OpenMode.CREATE_OR_APPEND;	
		default:
			 throw new Exception("Select a valid mode");
		}
	}
	
	static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}
	
	static Boolean	isSgmExtension(Path path) {
		String fileName = path.toString();
		String extension = fileName.substring(fileName.lastIndexOf(".")+1);	// obtain the extension
		if (extension.equals("sgm"))
			return true;
		return false;
	}
	
	static float calculateIdf(int docFreq, int docCount){
		//log(docCount/docFreq)
		float idf = (float) Math.log10(docCount/docFreq);
		return idf;
	}
	
	static IndexReader getIndexReader(String indexFolder) {
		Directory dir;
		IndexReader reader = null;
		try {
			dir = FSDirectory.open(Paths.get(indexFolder));
			reader = DirectoryReader.open(dir);	//indexReader -> leer contenidos de los campos almacenados en el indice
		} catch (CorruptIndexException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}
		return reader;
	}
}
