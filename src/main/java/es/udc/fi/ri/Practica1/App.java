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
		//INDEXADO
		String indexPath = null; // where we store the indices
		String docsPath = null;	// where we read the documents
		String openMode = null;	// open mode
		Boolean multiThreadMode = false;
		Boolean addIndexesMode = false;
		//PROCESADO
		String indexFile = null;
		String field = null;
		int n = 0;
		String term = null;
		
		// Indexer options
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			// OPCIONES DE INDEXACION
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
			// OPCIONES DE PROCESADO DE INDICE CONTRUIDO
			case "-addindexes":
				addIndexesMode = true;
				break;
			case "-indexin":
				indexFile = args[i+1];
				break;
			case "-best_idfterms":
				field = args[i+1];
				n =  Integer.parseInt(args[i+2]);
				break;
			case "-tfpos":
				field = args[i+1];
				term = args[i+2];
				break;
			case "-termstfpos1":
				break;
			case "-termstfpos2":
				break;
			// OPCIONES DE PROCESADO Y CONSTRUCCION DE NUEVO INDICE
			default:
				break;
			}
		}
		
		//POR COMODIDAD LOS INICIALIZARÉ MANUALMENTE
		//openMode = "CREATE";
		//indexPath = "C:\\Users\\yeraymendez\\Desktop\\Pruebas";
		//docsPath = "C:\\Users\\yeraymendez\\Desktop\\Practica";
		//docsPath = "C:\\Users\\yeraymendez\\Desktop\\uNA";
		indexFile = "C:\\Users\\yeraymendez\\Desktop\\Pruebas";
		field = "modelDescription";
		n = 10;
		try {
			if (indexPath !=null && openMode != null && docsPath != null && 
					addIndexesMode == false && multiThreadMode == false) {
				//System.err.println("Usage: " + usage);
			    //System.exit(1);
				IndexFiles.Indexer(indexPath,docsPath ,openMode);
			} else if (indexFile != null && field != null && n > 0) {
				ProcessIndex.bestIdfTerms(indexFile, field, n);
			} else if(indexFile != null && field != null && term != null){
				
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	    
	    
	    
	}
}
