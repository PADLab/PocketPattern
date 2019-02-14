package edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation;

import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.UniqueQueryExtractor;
import net.sf.jsqlparser.statement.Statement;

public class UniqueQuery {

	private Statement query = null;
	private int clusteringAppointment = -1;
	private int queryID = -1;
	private static int queryIDCounter = -1;
	
	public UniqueQuery(int queryID, Statement query, int clusteringAppointment) {
		super();
		this.query = query;
		this.clusteringAppointment = clusteringAppointment;
		
		if (queryID >= queryIDCounter) {
			this.queryID = queryID;
			queryIDCounter = queryID;
		}
	}
	
	public UniqueQuery(Statement query, int clusteringAppointment) {
		super();
		this.query = query;
		this.clusteringAppointment = clusteringAppointment;
		this.queryID = ++queryIDCounter;
	}

	public Statement getQuery() {
		return query;
	}

	public void setQuery(Statement query) {
		this.query = query;
	}	

	public int getClusteringAppointment() {
		return clusteringAppointment;
	}

	public void setClusteringAppointment(int clusteringAppointment) {
		this.clusteringAppointment = clusteringAppointment;
	}

	public int getQueryID() {
		return queryID;
	}

	public void setQueryID(int queryID) {
		this.queryID = queryID;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + clusteringAppointment;
		result = prime * result + queryID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		UniqueQuery uniqueQuery = null;
		
		if (obj == null) {
			return false;
		} else if (obj instanceof UniqueQuery) {
			uniqueQuery = (UniqueQuery) obj;
			return UniqueQueryExtractor.compareStatement(this.query, uniqueQuery.getQuery());
		} else if (obj instanceof Statement) {
			return UniqueQueryExtractor.compareStatement(this.query, (Statement) obj);
		} else {
			return false;
		}
	}
	
	
	
	
}
