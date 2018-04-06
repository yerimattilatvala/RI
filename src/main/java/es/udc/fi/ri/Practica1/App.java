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
		Boolean indexando = false;
		Boolean tfpos = false;
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
		Boolean borrando = false;
		String indexOut = null;
		String query = null;
		Boolean summariesMode = false;
		// Indexer options
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			// OPCIONES DE INDEXACION
			case "-openmode":
				indexando = true;
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
			case "-multithreadS":
				multiThreadMode = true;
				n = Integer.parseInt(args[i+1]);
				break;
			// OPCIONES DE PROCESADO DE INDICE CONTRUIDO
			case "-addindexes":
				addIndexesMode = true;
				break;
			case "-indexin":
				indexFile = args[i+1];
				break;
			case "-best_idfterms":
				indexando = false;
				field = args[i+1];
				n =  Integer.parseInt(args[i+2]);
				break;
			case "-tfpos":
				tfpos = true;
				indexando = false;
				field = args[i+1];
				term = args[i+2].toLowerCase();
				break;
			case "-termstfpos1":		//termstfpos1 docid field ord
				indexando = false;
				docId = Integer.parseInt(args[i+1]);
				field = args[i+2];
				ord = Integer.parseInt(args[i+3]);
				break;
			case "-termstfpos2":
				indexando = false;
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
				borrando = true;
				indexando = false;
				field = args[i+1];
				term = args[i+2].toLowerCase();
				break;
			case "-deldocsquery":
				borrando = true;
				indexando = false;
				query = args[i+1];
				break;
			case "-summaries":
				borrando = false;
				indexando = false;
				summariesMode = true;
				break;
			default:
				break;
			}
		}
		try {
			System.out.println();
			if (indexando) {
				if (indexPath !=null && openMode != null && docsPath != null) {
					IndexFiles.Indexer(indexPath,docsPath ,openMode,addIndexesMode,multiThreadMode);
				}
			} else {
				if (borrando) {
					if (indexFile != null && field !=null && term != null ) {
						ConstructIndexFromIndex.delDocsTerm(indexFile,field, term);
					}else if (indexFile !=null && query != null) {
						ConstructIndexFromIndex.delDocsQuery(indexFile, query);
					}
				} else {
					if (summariesMode == true) {
						if (indexFile != null && indexOut != null) {
							ConstructIndexFromIndex.makeSummaries(indexFile, indexOut, multiThreadMode, n);
						}else {
							throw new Exception("One of the routes has not been specified for summaries");
						}
					}
				}
				if(indexFile !=null && docId>-1 && field != null && ord>=0) {
					ProcessIndex.termsTfTerms1(indexFile, docId, field, ord);											
				}
				if(indexFile !=null && pathSgm != null  && newId != null && ord>=0) {
					ProcessIndex.termsTfTerms2(indexFile, pathSgm, newId,field,ord);											
				}
				if (tfpos) {
					if(indexFile != null && field != null && term != null){
						ProcessIndex.tfPos(indexFile, field, term);	
					}
				}
				if (indexFile != null && field != null && n > 0) {
					ProcessIndex.bestIdfTerms(indexFile, field, n);	//Fields -> Terms
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	    
	}
}
