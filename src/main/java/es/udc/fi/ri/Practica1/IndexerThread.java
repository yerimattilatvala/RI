package es.udc.fi.ri.Practica1;

import org.apache.lucene.index.IndexWriter;

public class IndexerThread implements Runnable{
	private String path;
	private IndexWriter indexWriter;
	
	public IndexerThread(String path, IndexWriter indexWriter) {
		super();
		this.path = path;
		this.indexWriter = indexWriter;
	}

	@Override
	public void run() {
		IndexFiles.simpleIndexing(this.path, this.indexWriter);	
	}
}
