package edu.buffalo.www.cse.odinlab.PocketBench.SessionSimilarity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.buffalo.www.cse.odinlab.PocketBench.SequenceMerger;
import edu.buffalo.www.cse.odinlab.PocketBench.ActivityDetection.ActivityMatcher;
import edu.buffalo.www.cse.odinlab.PocketBench.ControlledData.Label;
import edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation.DataReader;
import edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation.DataReaderPhoneLabLog;
import edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation.UniqueQuery;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.Util;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.DataRow;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Session;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.SessionSplitter;
import querySimilarityMetrics.Makiyama;

public class WorkloadAnalyzer {
	
	private static int PARTITION_COUNT = 3;
	private DataReader workloadReader = null;
	private String dataFolder = "";
	private String workloadOwner = "";
	
	public WorkloadAnalyzer(String dataFolder, String workloadOwner) {
		this.dataFolder = dataFolder;
		this.workloadOwner = workloadOwner;
	}
	
	/**
	 * This method takes a session list as input and creates distance
	 * matrices for different types of feature extraction methods
	 * @param sessions
	 * @param method
	 */
	public List<Session> performSessionClustering(List<Session> sessions, String method) {
		
		double[][] distanceMatrix = null;
		
		if (sessions != null && sessions.size() > 0) {
			distanceMatrix = new double[sessions.size()][sessions.size()];
			
			SessionComparator comparator = null;
			
			// symmetric matrices
			if (method.equals("JSDivergence")
					|| method.equals("FeatureBasedJaccard")
					|| method.equals("ClusterBasedJaccard")
					|| method.equals("EditBasedApproach")
					|| method.equals("SubsequenceBasedApproach")
					|| method.equals("LogBasedApproach")
					|| method.equals("AlignmentBasedApproach")
					
					) {		
				
				if (method.equals("EditBasedApproach")) {
					// use Levenstein distance comparing the cluster assignments
					
					List<List<Integer>> sessionProfiles = new ArrayList<List<Integer>>();
					for (int i = 0; i < sessions.size(); i++) {
						sessionProfiles.add(sessions.get(i).getQueries().stream().map(DataRow::getCluster).collect(Collectors.toList()));
					}
					
					LevenshteinDistance levenshtein = null;
					for (int i = 0; i < sessions.size() - 1; ++i) {
						for (int j = i + 1; j < sessions.size(); ++j) {
							
							levenshtein = new LevenshteinDistance();
							
							distanceMatrix[i][j] = levenshtein.Similarity(sessionProfiles.get(i), sessionProfiles.get(j));
							distanceMatrix[j][i] = distanceMatrix[i][j];
						}
		
					}
				}
				
				if (method.equals("SubsequenceBasedApproach")) {
					
					//TODO It computes the dice coefficient of n-grams of cluster assignments.
					
					
				}
				
				if (method.equals("LogBasedApproach")) {
					
					//TODO It computes the tf-idf of two sequences of cluster assignments.
					
					
				}
				
				if (method.equals("AlignmentBasedApproach")) {
					//TODO It considers the ordering of the queries along while comparing n-grams of resulting sequences. It finds the best alignments of n-grams to maxi- mize the similarity.
				}
				
				
				if (method.equals("JSDivergence")) {
					TreeMap<Long, TreeMap<String, Integer>> sessionProfiles = new TreeMap<>();
					System.out.println("JSDivergence profile creation for " + sessions.size() + " sessions started.");
					sessions.stream().forEach((session) -> {
						System.out.println(session.getSessionID());
						Profiler profiler = new Profiler(session);
						sessionProfiles.put(session.getSessionID(), profiler.getProfileForDivergence());
					});
					System.out.println("JSDivergence profiles created.");
					for (int i = 0; i < sessions.size() - 1; ++i) {
						for (int j = i + 1; j < sessions.size(); ++j) {
							
							comparator = new SessionComparator(sessions.get(i), sessions.get(j));
							
							distanceMatrix[i][j] = comparator.compareUsingJSDivergence(sessionProfiles.get(sessions.get(i).getSessionID()), sessionProfiles.get(sessions.get(j).getSessionID()));
							distanceMatrix[j][i] = distanceMatrix[i][j];
						}
		
					}
					System.out.println("JSDivergence distance matrix created.");
				} else if (method.equals("FeatureBasedJaccard") ) {
					TreeMap<Long, TreeMap<String, Integer>> sessionProfiles = new TreeMap<>();
					
					sessions.stream().forEach((session) -> {
						Profiler profiler = new Profiler(session);
						sessionProfiles.put(session.getSessionID(), profiler.getProfileForFeatureBasedJaccard());
					});
					
					for (int i = 0; i < sessions.size() - 1; ++i) {
						for (int j = i + 1; j < sessions.size(); ++j) {
							
							comparator = new SessionComparator(sessions.get(i), sessions.get(j));
							
							distanceMatrix[i][j] = comparator.compareUsingFeatureBasedJaccard(sessionProfiles.get(sessions.get(i).getSessionID()), sessionProfiles.get(sessions.get(j).getSessionID()));
							distanceMatrix[j][i] = distanceMatrix[i][j];
						}
		
					}
					System.out.println("FeatureBasedJaccard distance matrix created.");
				} else if (method.equals("ClusterBasedJaccard")) {
					TreeMap<Long, Set<Integer>> sessionProfiles = new TreeMap<>();
					
					for (int i = 0; i < sessions.size(); i++) {
						Profiler profiler = new Profiler(sessions.get(i));
						sessionProfiles.put(sessions.get(i).getSessionID(), profiler.getProfileForClusterBasedJaccard());
					}
					
					for (int i = 0; i < sessions.size() - 1; ++i) {
						for (int j = i + 1; j < sessions.size(); ++j) {
							
							comparator = new SessionComparator(sessions.get(i), sessions.get(j));
							
							distanceMatrix[i][j] = comparator.compareUsingClusterBasedJaccard(sessionProfiles.get(sessions.get(i).getSessionID()), sessionProfiles.get(sessions.get(j).getSessionID()));
							distanceMatrix[j][i] = distanceMatrix[i][j];
						}
		
					}
				}
				System.out.println("ClusterBasedJaccard distance matrix created.");
			} else if (method.equals("KLDivergence")) {
			
				// asymmetric matrices
				for (int i = 0; i < sessions.size() - 1; ++i) {
					for (int j = 0; j < sessions.size(); ++j) {
						
						comparator = new SessionComparator(sessions.get(i), sessions.get(j));
						
						if (i != j) {
							distanceMatrix[i][j] = comparator.compareUsingKLDivergence();
						} else {
							distanceMatrix[i][j] = 0;
						}
					}
				}
			}
			
			try {
				BufferedWriter output_DM = new BufferedWriter(new FileWriter(new File(
						this.dataFolder + File.separator + "DM.csv")));
				
				for (int i = 0; i < sessions.size(); ++i) {
					if (i == sessions.size() - 1) {
						output_DM.write(i + "\n");
					} else {
						output_DM.write(i + ",");
					}
				}
	
				for (int i = 0; i < sessions.size(); ++i) {
					for (int j = 0; j < sessions.size(); ++j) {
						if (j == sessions.size() - 1) {
							output_DM.write(distanceMatrix[i][j] + "\n");
						} else {
							output_DM.write(distanceMatrix[i][j] + ",");
						}
					}
				}
				output_DM.close();
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
			}
			
			performSessionClustering(sessions.size());
			sessions = getUniqueSessionClusters(sessions);
		}
		
		return sessions;
		
	}
	
	/**
	 * Clusters the unique queries using R
	 */
	public void performSessionClustering(int size) {
		
		long lStartTime = System.nanoTime();
		
		String[] cmdArray = new String[4];
		
		cmdArray[0] = "Rscript";
		cmdArray[1] = "--vanilla";
		cmdArray[2] = this.dataFolder + File.separator + "clusterSessions.R";
		cmdArray[3] = this.dataFolder;
		
		Process child;
		try {
			child = Runtime.getRuntime().exec(cmdArray);
			
			int code = child.waitFor();

	        switch (code) {
	            case 0:
	                //normal termination, everything is fine
	                break;
	            case 1:
	                //Read the error stream
	                InputStream inputStream = child.getErrorStream();
	                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	                StringBuilder out = new StringBuilder();
	                String line;
	                while ((line = reader.readLine()) != null) {
	                    out.append(line);
	                }
	                System.out.println(out.toString());   //Prints the string content read from input stream
	                reader.close();
	        }
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		
		long lEndTime = System.nanoTime();
		long output = lEndTime - lStartTime;
		System.out.println("Session clustering time for " + size + " sessions - Elapsed time in seconds: " + (1.0 * output / 1000000000));
		
	}
	
	
	/**
	 * Matches the cluster appointments with the queries
	 */
	public List<Session> getUniqueSessionClusters(List<Session> sessions) {
		String clusterAppointmentsFile = this.dataFolder + File.separator + "sessionClustersOrdered.txt";

		BufferedReader inputClusterAppointments = null;

		try {
			inputClusterAppointments = new BufferedReader(new FileReader(new File(clusterAppointmentsFile)));

			String appointment = inputClusterAppointments.readLine();
			int counter = 0;

			while (appointment != null) {
				sessions.get(counter).setCluster(Integer.parseInt(appointment));
				
				counter++;
				appointment = inputClusterAppointments.readLine();
			}

			inputClusterAppointments.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		
		return sessions;

	}

	
	public void initLabeled(String dataFolder) {
		this.dataFolder = dataFolder;
		this.workloadReader = new DataReaderPhoneLabLog(dataFolder);
		
		List<String> userFiles = workloadReader.getUserFiles();
		
		SessionSplitter s = null;
		List<DataRow> inputToSequenceSplitter = null;
		HashMap<String, List<LabeledSession>> userSessions = new HashMap<String, List<LabeledSession>>();
		
		for (int i = 0; i < userFiles.size(); i++) {
			s = new SessionSplitter();
			
			s.setWorkingDirectory(dataFolder);
			
			inputToSequenceSplitter = SequenceMerger.exportForSessionSplitter(this.dataFolder, userFiles.get(i), this.workloadReader.getUniqueQueries());
			
			s.importClusteringData(inputToSequenceSplitter);
			
			//Creating a list of time intervals
	        //100 -> 0.1sn, 1000 -> 1sn, 5000 -> 5sn, 10000 -> 10sn, 30000 -> 30sn, 60000 -> 1min, 300000 -> 5min, 600000 -> 10min, 1800000 -> 30min
	        List<Integer> idleTimes = new ArrayList<>(Arrays.asList(100, 1000, 5000, 10000, 30000, 60000, 300000, 600000, 1800000));
	        
			//As a side effect, this creates a file that keeps the session counts per time interval for each user
			s.findIdealIdleTimesForGivenUserMap(idleTimes);
			
			inputToSequenceSplitter = s.getSessions();
			List<LabeledSession> sessions = null;
			
			if (inputToSequenceSplitter != null && !inputToSequenceSplitter.isEmpty()) {
				
				sessions = new ArrayList<LabeledSession>();
				LabeledSession session = null;
				for (int j = 0; j < inputToSequenceSplitter.size(); j++) {
					if (!foundSessionInTheList(sessions, inputToSequenceSplitter.get(j))) {
						session = new LabeledSession(inputToSequenceSplitter.get(j).getUser(), inputToSequenceSplitter.get(j).getWindowId());
						session.addRow(Util.convertDataRowToActivityData(inputToSequenceSplitter.get(j)));
						
						sessions.add(session);
					}
				}
				
				
				//TODO test for each one and measure time
				
				//test for each one
				//TODO measure the time
				sessions = testForFeatureBasedJaccardActivityLabels(sessions);
				
				sessions = testForClusteringBasedActivityLabels(sessions);
				
				sessions = testForFeatureBasedJensenShannonActivityLabels(sessions);
				
			}
			
		}
		
	}

	private List<LabeledSession> testForFeatureBasedJensenShannonActivityLabels(List<LabeledSession> sessions) {
		
		for (int i = 0; i < sessions.size(); i++) {
			sessions.get(i).setLabels(ActivityMatcher.appointLabelsThroughFeatureBasedJensenShannon(sessions.get(i)));
		}
		
		List<LabeledSession>[] partitionedData = partitionData(sessions, PARTITION_COUNT);
		
		//Inner hashmap holds the accumulated label string and its count
		//Outer hashmap holds the partition number and the inner hashmap
		Map<Integer, HashMap<String, Integer>> labelCountsPerPartition = null;
		
		if (partitionedData != null && partitionedData.length > 0) {
			labelCountsPerPartition = new HashMap<Integer, HashMap<String, Integer>>();
			
			for (int j = 0; j < partitionedData.length; j++) {
				labelCountsPerPartition.put(j, getLabelCounts(partitionedData[j]));
			}
			
			
		}
		
		//TODO returns will change
		return sessions;
	}

	private List<LabeledSession> testForClusteringBasedActivityLabels(List<LabeledSession> sessions) {
		
		for (int i = 0; i < sessions.size(); i++) {
			sessions.get(i).setLabels(ActivityMatcher.appointLabelsThroughClusters(sessions.get(i)));
		}
		
		List<LabeledSession>[] partitionedData = partitionData(sessions, PARTITION_COUNT);
		
		//Inner hashmap holds the accumulated label string and its count
		//Outer hashmap holds the partition number and the inner hashmap
		Map<Integer, HashMap<String, Integer>> labelCountsPerPartition = null;
		
		if (partitionedData != null && partitionedData.length > 0) {
			labelCountsPerPartition = new HashMap<Integer, HashMap<String, Integer>>();
			
			for (int j = 0; j < partitionedData.length; j++) {
				labelCountsPerPartition.put(j, getLabelCounts(partitionedData[j]));
			}
			
			
		}
		
		//TODO returns will change
		return sessions;
	}

	private List<LabeledSession> testForFeatureBasedJaccardActivityLabels(List<LabeledSession> sessions) {
		
		for (int i = 0; i < sessions.size(); i++) {
			sessions.get(i).setLabels(ActivityMatcher.appointLabelsThroughFeatureBasedJaccards(sessions.get(i)));
		}
		
		List<LabeledSession>[] partitionedData = partitionData(sessions, PARTITION_COUNT);
		
		//Inner hashmap holds the accumulated label string and its count
		//Outer hashmap holds the partition number and the inner hashmap
		Map<Integer, HashMap<String, Integer>> labelCountsPerPartition = null;
		
		if (partitionedData != null && partitionedData.length > 0) {
			labelCountsPerPartition = new HashMap<Integer, HashMap<String, Integer>>();
			
			for (int j = 0; j < partitionedData.length; j++) {
				labelCountsPerPartition.put(j, getLabelCounts(partitionedData[j]));
			}
			
			
		}
		
		
		//TODO returns will change
		return sessions;
	}

	private boolean foundSessionInTheList(List<LabeledSession> sessions, DataRow dataRow) {
		
		for (int i = 0; i < sessions.size(); i++) {
			if (sessions.get(i).getSessionID() == dataRow.getWindowId()) {
				sessions.get(i).addRow(Util.convertDataRowToActivityData(dataRow));
				return true;
			}
		}
		
		return false;
	}
	
	private List<LabeledSession>[] partitionData(List<LabeledSession> sessions, int partitionCount) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private HashMap<String, Integer> getLabelCounts(List<LabeledSession> sessionList) {
		
		HashMap<String, Integer> retVal = null;
		
		if (sessionList != null && !sessionList.isEmpty()) {
			
			retVal = new HashMap<String, Integer>();
			String tempKey = null;
			for (int i = 0; i < sessionList.size(); i++) {
				tempKey = getLabelsString(sessionList.get(i).getLabels());
				
				if (retVal.containsKey(tempKey)) {
					retVal.put(tempKey, retVal.get(tempKey) + 1);
				} else {
					retVal.put(tempKey, 1);
				}
				
			}
			
			
		}
		
		return retVal;
	}
	
	private String getLabelsString(List<Label> labels) {
		String retVal = "";
		if (labels != null && !labels.isEmpty()) {
			
			for (int i = 0; i < labels.size(); i++) {
				retVal = retVal + "," + labels.get(i);
			}
		}
		
		return retVal;
	}

}
