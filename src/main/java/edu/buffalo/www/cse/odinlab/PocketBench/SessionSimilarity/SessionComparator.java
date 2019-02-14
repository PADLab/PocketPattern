package edu.buffalo.www.cse.odinlab.PocketBench.SessionSimilarity;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Session;
import edu.buffalo.www.odinlab.statlib.StatLib;

public class SessionComparator {
	
	private Profiler profiler1 = null;
	private Profiler profiler2 = null;
	
	public SessionComparator(Session session1, Session session2) {
		profiler1 = new Profiler(session1);
		profiler2 = new Profiler(session2);
	}
	
	public double compareUsingKLDivergence() {
		
		TreeMap<String, Integer> distribution1 = profiler1.getProfileForDivergence();
		TreeMap<String, Integer> distribution2 = profiler2.getProfileForDivergence();
		
		TreeSet<String> union = new TreeSet<String>();
		union.addAll(distribution1.keySet());
		union.addAll(distribution2.keySet());
		
		double[] vector1 = new double[union.size()];
		double[] vector2 = new double[union.size()];
		int tempIndex = 0;
		
		for (String columnName : union) {
			if (distribution1.containsKey(columnName)) {
				vector1[tempIndex] = distribution1.get(columnName).intValue();
			}
			if (distribution2.containsKey(columnName)) {
				vector2[tempIndex] = distribution2.get(columnName).intValue();
			}
			tempIndex++;
		}
		
		//return Stat.klDivergence(vector1, vector2);
		return StatLib.klDivergenceWithLaplaceCorrection(getProbabilities(vector1), getProbabilities(vector2));
	}
	
	public double compareUsingJSDivergence(TreeMap<String, Integer> distribution1, TreeMap<String, Integer> distribution2) {
		
		if (distribution1 == null) {
			distribution1 = new TreeMap<String, Integer>();
		}
		if (distribution2 == null) {
			distribution2 = new TreeMap<String, Integer>();
		}
		
		
		TreeSet<String> union = new TreeSet<String>();
		if (!distribution1.isEmpty())
			union.addAll(distribution1.keySet());
		if (!distribution2.isEmpty())
			union.addAll(distribution2.keySet());
		
		double[] vector1 = new double[union.size()];
		double[] vector2 = new double[union.size()];
		int tempIndex = 0;
		
		for (String columnName : union) {
			
			if (distribution1.containsKey(columnName)) {
				vector1[tempIndex] = distribution1.get(columnName).intValue();
			}
			if (distribution2.containsKey(columnName)) {
				vector2[tempIndex] = distribution2.get(columnName).intValue();
			}
			tempIndex++;
		}
		
		//return Stat.klDivergence(vector1, vector2);
		return StatLib.jsDivergence(getProbabilities(vector1), getProbabilities(vector2));
	}
	
	public double compareUsingJSDivergence() {
		
		TreeMap<String, Integer> distribution1 = profiler1.getProfileForDivergence();
		TreeMap<String, Integer> distribution2 = profiler2.getProfileForDivergence();
		
		if (distribution1 == null) {
			distribution1 = new TreeMap<String, Integer>();
		}
		if (distribution2 == null) {
			distribution2 = new TreeMap<String, Integer>();
		}
		
		
		TreeSet<String> union = new TreeSet<String>();
		if (distribution1 != null && !distribution1.isEmpty())
			union.addAll(distribution1.keySet());
		if (distribution2 != null && !distribution2.isEmpty())
			union.addAll(distribution2.keySet());
		
		double[] vector1 = new double[union.size()];
		double[] vector2 = new double[union.size()];
		int tempIndex = 0;
		
		for (String columnName : union) {
			
			if (distribution1.containsKey(columnName)) {
				vector1[tempIndex] = distribution1.get(columnName).intValue();
			}
			if (distribution2.containsKey(columnName)) {
				vector2[tempIndex] = distribution2.get(columnName).intValue();
			}
			tempIndex++;
		}
		
		//return Stat.klDivergence(vector1, vector2);
		return StatLib.jsDivergence(getProbabilities(vector1), getProbabilities(vector2));
	} 
	
	public double compareUsingFeatureBasedJaccard(TreeMap<String, Integer> distribution1, TreeMap<String, Integer> distribution2) {
		
		TreeSet<String> union = new TreeSet<String>();
		if (distribution1 != null && !distribution1.isEmpty()) {
			union.addAll(distribution1.keySet());
		} else {
			distribution1 = new TreeMap<String, Integer>();
		}
		if (distribution2 != null && !distribution2.isEmpty()) {
			union.addAll(distribution2.keySet());
		} else {
			distribution2 = new TreeMap<String, Integer>();
		}
		
		int intersectionSize = distribution1.keySet().size() + distribution2.keySet().size()
                - union.size();
		
		double distance = 1.0 - (1.0 * intersectionSize / union.size());
		
		return distance;
	} 
	
	public double compareUsingFeatureBasedJaccard() {
		
		TreeMap<String, Integer> distribution1 = profiler1.getProfileForFeatureBasedJaccard();
		TreeMap<String, Integer> distribution2 = profiler2.getProfileForFeatureBasedJaccard();
		
		TreeSet<String> union = new TreeSet<String>();
		if (distribution1 != null && !distribution1.isEmpty()) {
			union.addAll(distribution1.keySet());
		} else {
			distribution1 = new TreeMap<String, Integer>();
		}
		if (distribution2 != null && !distribution2.isEmpty()) {
			union.addAll(distribution2.keySet());
		} else {
			distribution2 = new TreeMap<String, Integer>();
		}
		
		int intersectionSize = distribution1.keySet().size() + distribution2.keySet().size()
                - union.size();
		
		double distance = 1.0 - (1.0 * intersectionSize / union.size());
		
		return distance;
	} 
	
	public double compareUsingClusterBasedJaccard() {
		Set<Integer> distribution1 = profiler1.getProfileForClusterBasedJaccard();
		Set<Integer> distribution2 = profiler2.getProfileForClusterBasedJaccard();
		
		TreeSet<Integer> union = new TreeSet<Integer>();
		union.addAll(distribution1);
		union.addAll(distribution2);
		
		int intersectionSize = distribution1.size() + distribution2.size()
                - union.size();
		
		double distance = 1.0 - (1.0 * intersectionSize / union.size());
		
		return distance;
	} 
	
	public double compareUsingClusterBasedJaccard(Set<Integer> distribution1, Set<Integer> distribution2) {
		
		TreeSet<Integer> union = new TreeSet<Integer>();
		if (distribution1 != null && !distribution1.isEmpty()) {
			union.addAll(distribution1);
		} else {
			distribution1 = new HashSet<Integer>();
		}
		if (distribution2 != null && !distribution2.isEmpty()) {
			union.addAll(distribution2);
		} else {
			distribution2 = new HashSet<Integer>();
		}
		
		int intersectionSize = distribution1.size() + distribution2.size()
                - union.size();
		
		double distance = 1.0 - (1.0 * intersectionSize / union.size());
		
		return distance;
	} 

	public double compareUsingKLDivergence(LabeledSession sessionA, LabeledSession sessionB) {
		
		return 0;
	} 
	
	public double compareUsingJSDivergence(LabeledSession sessionA, LabeledSession sessionB) {
		
		return 0;
	} 
	
	public double compareUsingFeatureBasedJaccard(LabeledSession sessionA, LabeledSession sessionB) {
		
		return 0;
	} 
	
	public double compareUsingClusterBasedJaccard(LabeledSession sessionA, LabeledSession sessionB) {
		
		return 0;
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



}
