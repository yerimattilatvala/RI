package es.udc.fi.ri.Practica1;

public class Article {
	private String title,topics,body,date,dateline,oldId,newId,path,hostname,thread;

	public Article(String title, String topics, String body, String date, String dateline, String oldId, String newId,
			String path, String hostname, String thread) {
		super();
		this.title = title;
		this.topics = topics;
		this.body = body;
		this.date = date;
		this.dateline = dateline;
		this.oldId = oldId;
		this.newId = newId;
		this.path = path;
		this.hostname = hostname;
		this.thread = thread;
	}


	public String getTitle() {
		return title;
	}

	public String getTopics() {
		return topics;
	}

	public String getBody() {
		return body;
	}

	public String getDate() {
		return date;
	}

	public String getDateline() {
		return dateline;
	}

	public String getOldId() {
		return oldId;
	}

	public String getNewId() {
		return newId;
	}

	public String getPath() {
		return path;
	}

	public String getHostname() {
		return hostname;
	}

	public String getThread() {
		return thread;
	}
}
