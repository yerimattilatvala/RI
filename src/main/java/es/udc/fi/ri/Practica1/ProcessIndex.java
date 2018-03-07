package es.udc.fi.ri.Practica1;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class ProcessIndex {

	static private IndexReader getIndexReader(String indexFolder) {
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
	
	static void bestIdfTerms(String indexFolder, String field, int n) {
		
		try {
			ArrayList<TermStats> terms = new ArrayList<>();
			IndexReader reader = getIndexReader(indexFolder);
			int numDocs = reader.numDocs();
			Terms termVectors = MultiFields.getTerms(reader, field);	// permite acceder a los campos sin recorrer las hojas
			if (termVectors ==  null){
				System.out.println("The field ** "+ field + " ** don´t exists.");
			}
			int i = 0;
			final TermsEnum iterator = termVectors.iterator();
			while(iterator.next()!=null && i<n) {
				final String t = iterator.term().utf8ToString();
				final int docFreq = iterator.docFreq();
				//log(docCount/docFreq)	
				float idf = Utils.calculateIdf(docFreq, numDocs);
				final TermStats term = new TermStats(t, idf);
				terms.add(term);
				i++;
			}
			
			terms.sort(Comparator.comparing(TermStats::getIdf).reversed());	//asi las muestra de mayor a menor
			
			for (int j = 0; j < terms.size(); j++)
				System.out.println("- Idf : "+ terms.get(j).getIdf() + "  Term : "+ terms.get(j).getTerm());
			
			reader.close();
			
		} catch (CorruptIndexException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		} catch (Exception e1) {
			System.out.println("Graceful message: exception " + e1);
			e1.printStackTrace();
		}
	}
	
	static void tfPos(String indexFolder, String field, String term) {
		IndexReader indexReader = null;
		PostingsEnum dataTerm = null;
		BytesRef bytes = null;
		Document doc = null;
		
		bytes = new BytesRef(term);
		indexReader = getIndexReader(indexFolder);
		Term termAux = new Term(field,bytes);
		int df = 0;
		try {
			df = indexReader.docFreq(termAux);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			dataTerm = MultiFields.getTermDocsEnum(indexReader, field, bytes,PostingsEnum.ALL);
			while (dataTerm.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
				int docId = dataTerm.docID();
				int pos = dataTerm.nextPosition();
				doc = indexReader.document(docId);
				System.out.println("-------------------------------------");
				System.out.println("DocId = " + docId);
				System.out.println("PathSgm = " + doc.get("PathSgm"));
				System.out.println("OldId = " + doc.get("OldId"));
				System.out.println("NewId = " + doc.get("NewId"));
				System.out.println("Title = " + doc.get("Title"));
				System.out.println("["+term+"] frequency in this document = " + dataTerm.freq()+" .");	//frecuencia en el documento actual
				System.out.println("Number the documents that contain the ["+term+"] = "+df+" .");	//frecuencia del termino
				System.out.println("Position of ["+term+"] = " + pos+" .");	//posicion
				System.out.println("-------------------------------------");
			}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			indexReader.close();
		} catch (IOException e) {
			System.out.println("Graceful message: exception " + e);
			e.printStackTrace();
		}
	}

	static void termsTfTerms1(String indexFolder,int docId,String field,int ord) {
		IndexReader indexReader = null;
		Document doc = null;
		Terms termField = null;
		TermsEnum iterator = null;
		PostingsEnum posting= null;
		ArrayList<TermStats> termList = new ArrayList<>();
		
		indexReader = getIndexReader(indexFolder);
		try {
			termField = indexReader.getTermVector(docId, field);
			doc = indexReader.document(docId);
			iterator = termField.iterator();
			while(iterator.next()!=null) {
				final String t = iterator.term().utf8ToString();
				final int tf = iterator.docFreq();	// frecuencia del termino en el documento actual
				BytesRef bytes = new BytesRef(t);
				Term taux = new Term(field,bytes);
				int df = indexReader.docFreq(taux);		// numero de documentos en los que aparece el termino dentro del campo especificado	
				posting = MultiFields.getTermDocsEnum(indexReader, field, bytes,PostingsEnum.ALL);
				//final int tf = posting.freq();	// frecuencia del termino en el documento actual(me devuelve 0??)
				final int pos = posting.nextPosition();	//posicion del termino
				TermStats term = new TermStats(t, tf, df, pos,docId);
				termList.add(term);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch (ord) {
		case 0:
			System.out.println("Alphabetical sort...");
			printListOrd(doc, termList, true, false, false);
			break;
		case 1:
			System.out.println("Tf sort...");
			printListOrd(doc, termList, false, true, false);
			break;
		case 2:
			System.out.println("Df sort...");
			printListOrd(doc, termList, false, false, true);
			break;
		default:
			System.out.println("Correct values : 0, 1 y 2.");
			System.out.println("Predefined sort: Alphabetical");
			printListOrd(doc, termList, true, false, false);
			break;
		}
		try {
			indexReader.close();
		} catch (IOException e) {
			System.out.println("Graceful message: exception " + e);
			e.printStackTrace();
		}
	}
	
	private static void printListOrd(Document doc,ArrayList<TermStats> terms,Boolean alpha,Boolean tf,Boolean df) {
		
		if (alpha == true) {
			terms.sort(Comparator.comparing(TermStats::getTerm));
		} else if (tf == true) {
			terms.sort(Comparator.comparing(TermStats::getTf).reversed());
		} else if (df == true) {
			terms.sort(Comparator.comparing(TermStats::getDf).reversed());
		}
		
		for (int i = 0; i < terms.size(); i++) {
			System.out.println("-------------------------------------");
			System.out.println("Term = "+ terms.get(i).getTerm()+" .");
			System.out.println("DocId = "+ terms.get(i).getDocId()+" .");
			System.out.println("PathSgm = "+ doc.get("PathSgm")+" .");
			System.out.println("OldId = " + doc.get("OldId")+" .");
			System.out.println("NewId = " + doc.get("NewId")+" .");
			System.out.println("["+terms.get(i).getTerm()+"] frequency in this document = " + terms.get(i).getTf()+" .");	//frecuencia en el documento actual
			System.out.println("Position of ["+terms.get(i).getTerm()+"] = " + terms.get(i).getPos()+" .");	//posicion
			System.out.println("Number the documents that contain the ["+terms.get(i).getTerm()+"] = "+terms.get(i).getDf()+" .");	//frecuencia del termino
			System.out.println("-------------------------------------");
		}
	}
}
