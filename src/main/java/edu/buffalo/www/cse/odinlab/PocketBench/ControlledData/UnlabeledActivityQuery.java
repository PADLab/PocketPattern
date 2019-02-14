package edu.buffalo.www.cse.odinlab.PocketBench.ControlledData;

public class UnlabeledActivityQuery {

	private String timestamp = null;
	private String query = null;
	private String database = null;
	
	public UnlabeledActivityQuery(String timestamp, String query, String database) {
		this.timestamp = timestamp;
		this.query = query;
		this.database = database;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getDatabase() {
		return database;
	}
	
	public void setDatabase(String database) {
		this.database = database;
	}
}
