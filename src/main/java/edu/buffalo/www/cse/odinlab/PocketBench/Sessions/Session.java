package edu.buffalo.www.cse.odinlab.PocketBench.Sessions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Session implements Comparable<Session>{
	
	private long windowID = -1;
	private int cluster = -1;
	private String user = "";
	private List<DataRow> queries = null;
	private long startTime = -1;
	private long endTime = -1;
	
	public Session() {
		
	}
	
	public Session(long sessionID) {
		super();
		this.windowID = sessionID;

	}
	
	public int getCluster() {
		return cluster;
	}
	
	public String getUser() {
		return user;
	}
	
	public List<DataRow> getQueries() {
		return queries;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getEndTime() {
		return endTime;
	}
	
	public long getSessionID() {
		return windowID;
	}
	
	public void setCluster(int cluster) {
		this.cluster = cluster;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public void setQueries(ArrayList<DataRow> queries) {
		this.queries = queries;
	}
	
	public void addQuery(DataRow query) {
		if (this.queries == null) {
			this.queries = new ArrayList<DataRow>();
		}
		this.queries.add(query);
		
		Collections.sort(this.queries);
		
		this.startTime = this.queries.get(0).getTimestamp();
		this.endTime = this.queries.get(this.queries.size() - 1).getTimestamp();
	}
	
	public void addQueries(List<DataRow> queries) {
		if (this.queries == null) {
			this.queries = new ArrayList<DataRow>();
		}
		this.queries.addAll(queries);
		
		Collections.sort(this.queries);
		
		this.startTime = this.queries.get(0).getTimestamp();
		this.endTime = this.queries.get(this.queries.size() - 1).getTimestamp();
	}
	
	public void setSessionID(long windowID) {
		this.windowID = windowID;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cluster;
		result = prime * result + (int) (endTime ^ (endTime >>> 32));
		result = prime * result + ((queries == null) ? 0 : queries.hashCode());
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + (int) (windowID ^ (windowID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Session other = (Session) obj;
		if (cluster != other.cluster)
			return false;
		if (endTime != other.endTime)
			return false;
		if (queries == null) {
			if (other.queries != null)
				return false;
		} else if (!queries.equals(other.queries))
			return false;
		if (startTime != other.startTime)
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (windowID != other.windowID)
			return false;
		return true;
	}

	public int compareTo(Session session) {

		long currentObjTime = this.startTime;
		long otherObjTime = session.getStartTime();
		
		//ascending order 
		if (currentObjTime - otherObjTime > 0) {
			return 1;
		} else if (currentObjTime - otherObjTime == 0) {
			return 0;
		} else {
			return -1;
		}
	}
	
}
