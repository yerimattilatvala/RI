package es.udc.fi.ri.Practica1;

public class TermStats {
	private String term;
	private float idf;
	private int tf;
	private int df;
	private int pos;
	private int docId;
	
	public TermStats(String term, int tf, int df, int pos,int docId) {
		super();
		this.term = term;
		this.tf = tf;
		this.df = df;
		this.pos = pos;
		this.docId = docId;
	}
	
	public int getDocId() {
		return docId;
	}

	public int getPos() {
		return pos;
	}

	public int getTf() {
		return tf;
	}

	public int getDf() {
		return df;
	}

	public TermStats(String term, float idf) {
		super();
		this.term = term;
		this.idf = idf;
	}

	public String getTerm() {
		return term;
	}

	public float getIdf() {
		return idf;
	}
	
}
