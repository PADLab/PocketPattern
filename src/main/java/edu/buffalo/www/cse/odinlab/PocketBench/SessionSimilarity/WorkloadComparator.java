package edu.buffalo.www.cse.odinlab.PocketBench.SessionSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.Lists;

import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Session;
import edu.buffalo.www.odinlab.statlib.StatLib;

public class WorkloadComparator {
	
	private List<Session> allSessions = null;
	private int partitionCount = 0;
	private String message = "";
	private TreeMap<Integer, Integer> fullProfile = null;
	
	public WorkloadComparator(List<Session> allSessions, int partitionCount, String message) {
		this.allSessions = allSessions;
		this.partitionCount = partitionCount;
		this.message = message;
		fullProfile = createProfile(this.allSessions);
	}
	
	
	private TreeMap<Integer, Integer> createProfile(List<Session> sessionList) {
		
		TreeMap<Integer, Integer> profile = new TreeMap<Integer, Integer>();
		HashMap<Integer, Integer> tempProfile = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < sessionList.size(); i++) {
			if (tempProfile.containsKey(sessionList.get(i).getCluster())) {
				tempProfile.put(sessionList.get(i).getCluster(), tempProfile.get(sessionList.get(i).getCluster()) + 1);
			} else {
				tempProfile.put(sessionList.get(i).getCluster(), 1);
			}
		}
		
		profile.putAll(tempProfile);
		
		return profile;
	}
	
	public static List<List<Session>> partitionTheWorkload(List<Session> allSessions, int partitionCount) {
	    return Lists.partition(allSessions, partitionCount);
	}
	
	private double[] getProbabilities(double[] vector) {
		
		double sum = 0;
		
		for (int i = 0; i < vector.length; i++) {
			sum += vector[i];
		}
		
		double[] probabilityVector = new double[vector.length];
		
		for (int i = 0; i < vector.length; i++) {
			probabilityVector[i] = vector[i] / sum;
			//System.out.println(probabilityVector[i] + " = " + vector[i] + " / " + sum);
		}
		
		return probabilityVector;
	}
	
	public PartitionSimilarityStruct getComparisonOfFrequencies(String userID) {
		
		PartitionSimilarityStruct retVal = new PartitionSimilarityStruct(userID);
		
		List<List<Session>> partitions = Lists.partition(this.allSessions, (this.allSessions.size() / this.partitionCount) + 1);
		int count = 0;
		System.out.println(message);
		
		
	    for(List<Session> partition : partitions) {
	    		TreeMap<Integer, Integer> partitionProfile = createProfile(partition);
	    		TreeMap<Integer, Integer> restOfTheProfile = new TreeMap<>();
	    		
	    		
	    		Iterator<Integer> it = this.fullProfile.keySet().iterator();
	    		
	    		while(it.hasNext()) {
	    			int key = it.next();
	    			
	    			if (partitionProfile.containsKey(key)) {
	    				restOfTheProfile.put(key, fullProfile.get(key) - partitionProfile.get(key));
	    			} else {
	    				restOfTheProfile.put(key, fullProfile.get(key));
	    			}
	    		}
	    		
	    		TreeSet<Integer> union = new TreeSet<Integer>();
	    		union.addAll(partitionProfile.keySet());
	    		union.addAll(restOfTheProfile.keySet());
	    		
	    		double[] vector1 = new double[union.size()];
	    		double[] vector2 = new double[union.size()];
	    		int tempIndex = 0;
	    		
	    		for (int key : union) {
	    			if (partitionProfile.containsKey(key)) {
	    				vector1[tempIndex] = partitionProfile.get(key).intValue();
	    			}
	    			if (restOfTheProfile.containsKey(key)) {
	    				vector2[tempIndex] = restOfTheProfile.get(key).intValue();
	    			}
	    			tempIndex++;
	    		}
	    		
	    		double similarity = StatLib.Bhattacharyya(getProbabilities(vector1), getProbabilities(vector2));
	    		
	    		retVal.addPartitionSimilarity(similarity);
	    		
	    		System.out.println("Similarity between the partition " + count + " and the rest of the partitions is " + similarity);
	    		//System.out.println("Correlation distance between the partition " + count + " and the rest of the partitions is " + StatLib.correlation(getProbabilities(vector1), getProbabilities(vector2)));
	    		
	    		count++;
	    		
	    }
	    
	    return retVal;
		
	}

}
