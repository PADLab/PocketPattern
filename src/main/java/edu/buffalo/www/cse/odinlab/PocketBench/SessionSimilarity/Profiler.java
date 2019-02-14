package edu.buffalo.www.cse.odinlab.PocketBench.SessionSimilarity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.Util;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.DataRow;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Session;

public class Profiler {
	
	private Session session = null;
	
	public Profiler(Session session) {
		this.session = session;
	}
	
	public TreeMap<String, Integer> getProfileForFeatureBasedJaccard() {
		
		List<DataRow> queries = session.getQueries();
		TreeMap<String, Integer> profile = new TreeMap<String, Integer>();
		
		if (queries != null) {
			queries.stream().forEach( query -> {
				profile.putAll(Util.getQueryFeatureDistribution(query.getQuery()));
			});
		} else {
			return null;
		}
		
		return profile;
	}
	
	public Set<Integer> getProfileForClusterBasedJaccard() {
		
		List<DataRow> queries = session.getQueries();
		HashSet<Integer> profile = new HashSet<Integer>();
		
		if (queries != null) {
			queries.stream().forEach( query -> {
				profile.add(query.getCluster());
			});
		} else {
			return null;
		}
		
		return profile;
	}
	
	public TreeMap<String, Integer> getProfileForDivergence() {
		
		List<DataRow> queries = session.getQueries();
		TreeMap<String, Integer> profile = new TreeMap<String, Integer>();
		
		if (queries != null) {
			queries.stream().forEach( query -> {
				TreeMap<String, Integer> tempDist = Util.getQueryFeatureDistribution(query.getQuery());
				if (tempDist != null) {
					profile.putAll(tempDist);
				}
			});
		} else {
			return null;
		}
		
		return profile;
	}
	


	public List<Integer> getProfileOrderedClusters() {
		List<DataRow> queries = session.getQueries();
		List<Integer> profile = new ArrayList<Integer>();
		
		if (queries != null) {
			queries.stream().forEach( query -> {
				profile.add(query.getCluster());
			});
		} else {
			return null;
		}
		
		return profile;
	}
	
	
	

}
