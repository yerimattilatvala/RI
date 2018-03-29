package es.udc.fi.ri.Practica1;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.plaf.synth.SynthSeparatorUI;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;


public class ConstructIndexFromIndex {

	// funcion que realiza la logica de deldocsterm y deldocsquery
	private static long delDocs(String indexFolder, String indexOut,String field,String term, String query) {
		IndexWriter indexWriter = null;
		Analyzer analyzer = null;
		IndexWriterConfig iwc = null;
		Directory dir = null;
		long numDocsDelete = 0;
		ArrayList<String> fields = null;
		QueryParser parser = null;
		Query queryAux = null;
		
		analyzer = new StandardAnalyzer();
		iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.APPEND);
		try {
			dir = FSDirectory.open(Paths.get(indexFolder)); //abre ruta donde esta el indice
			indexWriter = new IndexWriter(dir,iwc);
			if (field != null && term != null) {
				numDocsDelete = indexWriter.deleteDocuments(new Term(field,term));
			} else if (query != null) {
				fields = getFields(dir);
				for (String f : fields) {
					parser = new QueryParser(f, analyzer);
					queryAux = parser.parse(query);
					numDocsDelete = indexWriter.deleteDocuments(queryAux);
					System.out.println("---------------");
					System.out.println("-Field = "+f);
					System.out.println("-Deleted Documents = "+ numDocsDelete);
					System.out.println("---------------");
				}
			}
			indexWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		return numDocsDelete;
	}

	private static ArrayList<String> getFields(Directory indexFolder) {
		IndexReader indexReader = null;
		Fields allFields = null;
		Iterator<String> iterator = null;
		String field = null;
		ArrayList<String> fieldList = new ArrayList<>();
		
		try {
			indexReader = DirectoryReader.open(indexFolder);
			allFields = MultiFields.getFields(indexReader);
			iterator = allFields.iterator();
			while (iterator.hasNext()){
				field = iterator.next();
				fieldList.add(field);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fieldList;		
	}
	
	public static void delDocsTerm(String indexFolder,String indexOut, String field, String term) {
		long numDocsDelete = delDocs(indexFolder, indexOut,field, term, null);
		System.out.println("-Field = "+field);
		System.out.println("-Term = "+term);
		System.out.println("-Deleted Documents = "+ numDocsDelete);
	}
	
	public static void delDocsQuery(String indexFolder,String indexOut,String query) {
		System.out.println("");
		System.out.println("-Query = "+query);
		delDocs(indexFolder,indexOut,null, null, query);
	}

	public static void summaries(String indexFolder, String indexOut) {
		IndexReader indexReader = null;
		IndexWriter indexWriter = null;
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		Directory dir = null;
		Document doc = null;
		String body = null;
		String title = null;
		String[] phraseBody = null;
		String summary = null;
		
		iwc.setOpenMode(OpenMode.CREATE);
		
		try {
			Directory dir2 = FSDirectory.open(Paths.get(indexOut)); //abre ruta para almacenar los indices
			indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexFolder)));
			indexWriter = new IndexWriter(dir2, iwc);	
			for (int i = 0; i < indexReader.numDocs(); i++) {
				doc = indexReader.document(i);
				body = doc.get("Body");
				phraseBody = body.split("\\.\n");
				title = doc.get("Title");
				if (body.equals("")) {
					summary = "";
				} else if (title.equals("")) {
					summary = phraseBody[0]+phraseBody[1];
				} else {
					summary = extractMostSimilarPhrases(doc, title,body);
				}
				addSummary(indexWriter, doc, summary);
			}
			indexReader.close();
			indexWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Directory createIndex(String body) {
		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		type.setStored(true);
		type.setTokenized(true);
		type.setStoreTermVectors(true);
		type.setStoreTermVectorPositions(true);
		Directory directory = new RAMDirectory();
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		IndexWriter writer;
		try {
			writer = new IndexWriter(directory, iwc);
			String[] f = body.split("\\.\n");
			for (int i = 0; i < f.length; i++) {
				Document doc = new Document();
				doc.add(new Field("Body",f[i],type));
				writer.addDocument(doc);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return directory;
	}
	

	private static void addSummary(IndexWriter indexWriter, Document doc, String summary) {
		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		type.setStored(true);
		type.setTokenized(true);
		type.setStoreTermVectors(true);
		type.setStoreTermVectorPositions(true);
		Field summaries = new Field("Summary",summary,type);
		doc.add(summaries);
		try {
			indexWriter.addDocument(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
	
	private static String extractMostSimilarPhrases(Document document,String title,String body) {
		Directory dir = createIndex(body);
		TopDocs topDocs = null;
		ArrayList<String> fields = new ArrayList<>();
		QueryParser parser = new QueryParser("Body",new StandardAnalyzer());
		String summary = "";
		try {
			IndexReader indexReader = DirectoryReader.open(dir);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			//Query query = parser.parse(title);
			Query query = parser.parse(QueryParser.escape(title));
			topDocs = indexSearcher.search(query, 2);
			if (topDocs.scoreDocs.length>0) {
				for (int i = 0; i < topDocs.scoreDocs.length; i++) {
					summary += indexSearcher.doc(topDocs.scoreDocs[i].doc).get("Body")+"\n";
				}
			}
			else {
				summary = "";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return summary;
	}
	
}
