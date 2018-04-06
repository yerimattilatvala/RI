package es.udc.fi.ri.Practica1;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;

public class ProcessedThread implements Runnable{

	private IndexReader indexReader;
	private IndexWriter indexWriter;
	private int init,end;
	
	public ProcessedThread(IndexReader indexReader, IndexWriter indexWriter, int init, int end) {
		super();
		this.indexReader = indexReader;
		this.indexWriter = indexWriter;
		this.init = init;
		this.end = end;
	}

	@Override
	public void run() {
		String summary = "";
		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		type.setStored(true);
		type.setTokenized(true);
		type.setStoreTermVectors(true);
		type.setStoreTermVectorPositions(true);
		for (int i = init; i < end; i++) {
			try {
				Document doc = this.indexReader.document(i);
				String body = doc.get("Body");
				String[] phraseBody = body.split("\\.\n");
				String title = doc.get("Title");
				//System.out.println(Thread.currentThread().getId());
				if (body.equals("")) {
					summary = "";
				} else if (title.equals("")) {
					summary = phraseBody[0]+phraseBody[1];
				} else {
					summary = ConstructIndexFromIndex.extractMostSimilarPhrases(doc, title,body);
				}
				ConstructIndexFromIndex.addSummary(this.indexWriter, doc, summary);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
