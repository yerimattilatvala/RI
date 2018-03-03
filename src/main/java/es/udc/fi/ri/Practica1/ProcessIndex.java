package es.udc.fi.ri.Practica1;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class ProcessIndex {

	
	static void bestIdfTerms(String indexFile, String field, int n) {
		
		try {
			ArrayList<TermStats> terms = new ArrayList<>();
			Directory dir = FSDirectory.open(Paths.get(indexFile));
			IndexReader reader = DirectoryReader.open(dir);	//indexReader -> leer contenidos de los campos almacenados en el indice
			long numDocs = reader.numDocs();
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
				//log(docCount/docFreq)	hacer manualmente, preguntar si es posible con clase BM25Similarity
				float idf = Utils.calculateIdf(docFreq, numDocs);
				final TermStats term = new TermStats(t, idf);
				terms.add(term);
				i++;
			}
			
			terms.sort(Comparator.comparing(TermStats::getIdf).reversed());	//asi las muestra de mayor a menor
			
			for (int j = 0; j < terms.size(); j++) {
				System.out.println("- Idf : "+ terms.get(j).getIdf() + "  Term : "+ terms.get(j).getTerm());
			}
			
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
}
