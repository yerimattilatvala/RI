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
			long numDocs = reader.numDocs();
			System.out.println(numDocs);
			Terms termVectors = MultiFields.getTerms(reader, field);	// permite acceder a los campos sin recorrer las hojas
			if (termVectors ==  null){
				System.out.println("The field ** "+ field + " ** don´t exists.");
			}
			float max = 0;
			int i = 0;
			final TermsEnum iterator = termVectors.iterator();
			while(iterator.next()!=null && i<n) {
				final String t = iterator.term().utf8ToString();
				final long docFreq = iterator.docFreq();
				System.out.println(docFreq);
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
			dataTerm = MultiFields.getTermDocsEnum(indexReader, field, bytes,dataTerm.ALL);
			while (dataTerm.nextDoc() != dataTerm.NO_MORE_DOCS) {
				int docId = dataTerm.docID();
				int pos = dataTerm.nextPosition();
				doc = indexReader.document(docId);
				System.out.println("-------------------------------------");
				System.out.println("DocId = " + docId);
				System.out.println("PathSgm = " + doc.get("PathSgm"));
				System.out.println("OldId = " + doc.get("OldId"));
				System.out.println("NewId = " + doc.get("NewId"));
				System.out.println("Title = " + doc.get("Title"));
				System.out.println("Frequency of ["+term+"]" + " in this document = " + dataTerm.freq()+" .");	//frecuencia en el documento actual
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
	}
}
