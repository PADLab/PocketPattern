package edu.buffalo.www.cse.odinlab.PocketBench.SessionSimilarity;

import java.util.ArrayList;
import java.util.List;

public class PartitionSimilarityStruct {
	
	String userID = null;
	List<Double> partitionSimilarities = null;
	String timeElapsed = null;
	
	public PartitionSimilarityStruct(String userID) {
		this.userID = userID;
	}
	
	public void addPartitionSimilarity(double result) {
		if (partitionSimilarities == null) {
			partitionSimilarities = new ArrayList<Double>();
		}
		partitionSimilarities.add(result);
	}
	
	public List<Double> getSimilarities() {
		return partitionSimilarities;
	}
	
	public String getUser() {
		return userID;
	}
	
	public void setTimeElapsed(String timeElapsed) {
		this.timeElapsed = timeElapsed;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(userID + ",");
		
		for (int i = 0; i < partitionSimilarities.size(); i++) {
			builder.append(partitionSimilarities.get(i) + ",");
		}
		
		builder.append(timeElapsed);
		
		return builder.toString();
	}

}
