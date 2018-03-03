package es.udc.fi.ri.Practica1;

public class TermStats {
	private String term;
	private float idf;
	
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
