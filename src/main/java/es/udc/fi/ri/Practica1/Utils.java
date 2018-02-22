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
import org.apache.lucene.index.IndexWriterConfig.OpenMode;


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
		value = value.substring(id.length()+1, value.length());
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
			 throw new Exception("Introduzca un modo válido");
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
}
