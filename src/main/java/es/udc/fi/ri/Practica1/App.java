package es.udc.fi.ri.Practica1;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
	public static void main(String[] args) {
		String usage = "Indexer use "
                + " [-index INDEX_PATH] [-coll DOCS_PATH] \n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index "
                + "in INDEX_PATH that can be searched with SearchFiles\n"
				+ "You must select one of the following modes : \n"
				+ "	- APPEND : Opens an existing index. \n"
				+ "	- CREATE : Creates a new index or overwrites an existing one. \n"
				+ "	- CREATE_OR_APPEND : Creates a new index if one does not" 
				+ " exist, otherwise it opens the index and documents will be" 
				+ " appended.";
		
		String indexPath = null; // where we store the indices
		String docsPath = null;	// where we read the documents
		String openMode = null;	// open mode
		Boolean multiThreadMode = false;
		Boolean addIndexesMode = false;
		/*FALTAN LA OPCION DE MULTITHREAD Y ADDINDEXES*/
		
		// Indexer options
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-openmode":
				openMode = args[i+1];
				break;
			case "-index":
				indexPath = args[i+1];
				break;
			case "-coll":
				docsPath = args[i+1];
				break;
			case "-multithread":
				multiThreadMode = true;
				break;
			case "-addindexes":
				addIndexesMode = true;
				break;
			default:
				break;
			}
		}
		
		//POR COMODIDAD LOS INICIALIZARÉ MANUALMENTE
		openMode = "CREATE";
		indexPath = "C:\\Users\\yeraymendez\\Desktop\\Pruebas";
		//docsPath = "C:\\Users\\yeraymendez\\Desktop\\Practica";
		docsPath = "C:\\Users\\yeraymendez\\Desktop\\uNA";
		
		if (indexPath ==null || openMode == null || docsPath == null ) {
			System.err.println("Usage: " + usage);
		    System.exit(1);
		}
		
	    final Path docDir = Paths.get(docsPath);
	    if (!Files.isReadable(docDir)) {
	      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }
	    
	    IndexFiles.Indexer(indexPath,docDir ,openMode);
	}
}
