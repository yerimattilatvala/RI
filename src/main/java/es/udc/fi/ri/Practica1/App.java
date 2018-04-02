package es.udc.fi.ri.Practica1;

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
		String term = null;
		String pathSgm = null;
		String newId = null;
		int docId = -1;
		int ord = -1;
		int n = 0;
		//CONSTRUCCION/BORRADO/RESUME
		String indexOut = null;
		String query = null;
		Boolean summariesMode = false;
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
			case "-termstfpos1":		//termstfpos1 docid field ord
				docId = Integer.parseInt(args[i+1]);
				field = args[i+2];
				ord = Integer.parseInt(args[i+3]);
				break;
			case "-termstfpos2":
				pathSgm = args[i+1];
				newId = args[i+2];
				field = args[i+3];
				ord = Integer.parseInt(args[i+4]);
				break;
			// OPCIONES DE PROCESADO Y CONSTRUCCION DE NUEVO INDICE A PARTIR DE OTRO INDICE
			case "-indexout":
				indexOut = args[i+1];
				break;
			case "-deldocsterm":
				field = args[i+1];
				term = args[i+2];
				break;
			case "-deldocsquery":
				query = args[i+1];
				break;
			case "-summaries":
				summariesMode = true;
				break;
			default:
				break;
			}
		}
		//POR COMODIDAD LOS INICIALIZARÉ MANUALMENTE
		openMode = "CREATE";
		indexPath = "C:\\Users\\yeraymendez\\Desktop\\Pruebass2";
		//docsPath = "C:\\Users\\yeraymendez\\Desktop\\Practica";
		docsPath = "C:\\Users\\yeraymendez\\Desktop\\dos";
		//indexFile = "C:\\Users\\yeraymendez\\Desktop\\Pruebass";
		addIndexesMode = true;
		//field = "Title";	//Topics, Body, Dateline, Date, Title
		//term = "CANADA";
		//n = 5;
		//docId = 1;
		//field = "Title";
		//pathSgm = "C:\\Users\\yeraymendez\\Desktop\\uNA\\reut2-021.sgm";
		//pathSgm.replaceAll("\\\\", "\\\\\\\\");
		//newId = "21001";
		//ord = 0;
		//term = "abe";
		//summariesMode = true;
		//indexOut = "C:\\Users\\yeraymendez\\Desktop\\Pruebass3";
		try {
			if (indexPath !=null && openMode != null && docsPath != null) {
				//System.err.println("Usage: " + usage);
			    //System.exit(1);
				IndexFiles.Indexer(indexPath,docsPath ,openMode,addIndexesMode,multiThreadMode);
			} else if (indexFile != null && field != null && n > 0) {
				ProcessIndex.bestIdfTerms(indexFile, field, n);	//Fields -> Terms
			} //else if(indexFile != null && field != null && term != null){
				//term = term.toLowerCase();
				//.tfPos(indexFile, field, term);		// Fields -> Terms -> Posting Lists
			/*}*/ else if(indexFile !=null && docId>-1 && field != null && ord>=0) {
				ProcessIndex.termsTfTerms1(indexFile, docId, field, ord);												//termstfpos1
			}else if(indexFile !=null && pathSgm != null  && newId != null && ord>=0) {
				ProcessIndex.termsTfTerms2(indexFile, pathSgm, newId,field,ord);												//termstfpos1
			}else if (indexFile != null && field !=null && term != null ) {
				ConstructIndexFromIndex.delDocsTerm(indexFile, indexOut,field, term);
			}else if (indexFile !=null && query != null) {
				ConstructIndexFromIndex.delDocsQuery(indexFile, indexOut, query);
			}else if (summariesMode == true) {
				if (indexFile != null && indexOut != null) {
					ConstructIndexFromIndex.summaries(indexFile, indexOut);
				}else {
					throw new Exception("0ne of the routes has not been specified for summaries");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	    
	}
}
