package edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation;

import java.util.Comparator;
import java.util.Date;

import net.sf.jsqlparser.statement.Statement;

public class QueryData implements Comparable<QueryData>, Comparator<QueryData>{
	
	public static int AUTO_INCREMENT = 0;

	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public Statement getQuery() {
		return query;
	}
	
	public void setQuery(Statement query) {
		this.query = query;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public int getClusterAppointment() {
		return clusterAppointment;
	}
	
	public void setClusterAppointment(int clusterAppointment) {
		this.clusterAppointment = clusterAppointment;
	}
	
	public int getQueryID() {
		return queryID;
	}
	
	/**
	 * This is the actual constructor for DataAdaptor
	 * @param timestamp
	 * @param query
	 * @param user
	 */
	public QueryData(long timestamp, Statement query, String user) {
		super();
		this.timestamp = timestamp;
		this.query = query;
		this.user = user;
		QueryData.AUTO_INCREMENT = QueryData.AUTO_INCREMENT + 1;
		this.queryID = QueryData.AUTO_INCREMENT;
	}
	
	/**
	 * This constructor converts Date to long
	 * @param timestamp
	 * @param query
	 * @param user
	 */
	public QueryData(Date timestamp, Statement query, String user) {
		super();
		this.timestamp = timestamp.getTime();
		this.query = query;
		this.user = user;
		QueryData.AUTO_INCREMENT = QueryData.AUTO_INCREMENT + 1;
		this.queryID = QueryData.AUTO_INCREMENT;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		QueryData other = (QueryData) obj;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.toString().equals(other.query.toString()))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	private long timestamp = 0;
	private Statement query = null;
	private String user = "";
	private int clusterAppointment = -1;
	private int queryID = -1;
	
	
	@Override
	public int compare(QueryData o1, QueryData o2) {
		if(o1.getTimestamp() > o2.getTimestamp()){
            return 1;
        } else if (o1.getTimestamp() == o2.getTimestamp()) {
        		return 0;
        }else {
            return -1;
        }
	}
	
	@Override
	public int compareTo(QueryData o) {
		if (this.getTimestamp() > o.getTimestamp()) {
			return 1;
		} else if (this.getTimestamp() == o.getTimestamp()) {
			return 0;
		} else {
			return -1;
		}
	}	
	
}
