package edu.buffalo.www.cse.odinlab.PocketBench;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation.DataReader;
import edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation.DataReaderPhoneLabLog;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.ParsabilityStatus;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.StringWithStatus;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.Util;
import edu.buffalo.www.cse.odinlab.PocketBench.SessionSimilarity.PartitionSimilarityStruct;
import edu.buffalo.www.cse.odinlab.PocketBench.SessionSimilarity.WorkloadAnalyzer;
import edu.buffalo.www.cse.odinlab.PocketBench.SessionSimilarity.WorkloadComparator;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.DataRow;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.OutputDataRow;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Session;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.SessionSplitter;

public class ExperimentSetup {
	
	private String appName = null;
	private String workingDirectory = null;
	
	private static final int PARTITION_COUNT = 3;
	
	public ExperimentSetup(String appName, String workingDirectory) {
		this.appName = appName;
		this.workingDirectory = workingDirectory;
	}
	
	public void runBaseExperiment(String startTime, String endTime) {
		
		//This line performs the preprocessing (clustering and preparing unique queries)
		
		DataReader workloadReader = new DataReaderPhoneLabLog(workingDirectory, startTime, endTime);
		
		List<String> userFiles = workloadReader.getUserFiles();
		
		//Preparing to find out the sessions
		SessionSplitter s = null;
		List<DataRow> inputToSequenceSplitter = null;
		
		HashMap<String, List<Session>> userSessions = new HashMap<String, List<Session>>();
		List<PartitionSimilarityStruct> partitionSimilarities = new ArrayList<PartitionSimilarityStruct>();
		
		for (int i = 0; i < userFiles.size(); i++) {
			s = new SessionSplitter();
			
			s.setWorkingDirectory(workingDirectory);
			
			//Matching cluster numbers appointed to the queries to actual query log
			inputToSequenceSplitter = SequenceMerger.exportForSessionSplitter(this.workingDirectory, userFiles.get(i), workloadReader.getUniqueQueries());
			
			s.importClusteringData(inputToSequenceSplitter);
			
			//Actual session identification takes place here
			long lStartTime = System.nanoTime();
			
			//Creating a list of time intervals
	        //100 -> 0.1sn, 1000 -> 1sn, 5000 -> 5sn, 10000 -> 10sn, 20000-> 20sn, 30000 -> 30sn, 60000 -> 1min, 300000 -> 5min, 600000 -> 10min, 120000->20min, 1800000 -> 30min
	        List<Integer> idleTimes = null; //ew ArrayList<>(Arrays.asList(100, 1000, 5000, 10000, 20000, 30000, 60000, 300000, 600000, 120000, 1800000));
			
	        
	        idleTimes.add(10000);
	        idleTimes.add(20000);
	        idleTimes.add(30000);
	        
	        int idleTime = 60000;
	        
			while (idleTime < 60000 * 60 * 60) //Between 10 seconds to 1 hour 
			{
				idleTimes.add(idleTime);
				idleTime = idleTime + 60000;
			}
	        
	        //As a side effect, this creates a file that keeps the session counts per time interval for each user
			s.findIdealIdleTimesForGivenUserMap(idleTimes);
			
			inputToSequenceSplitter = s.getSessions();
			
			long lEndTime = System.nanoTime();
			long output = lEndTime - lStartTime;
			System.out.println("Session Identification for " + userFiles.get(i) + " - Elapsed time in seconds: " + (1.0 * output / 1000000000));
			
			
			//Now that we found out sessions...
			Map<Long, Session> sessionsMap = null;
			
			if (inputToSequenceSplitter != null && !inputToSequenceSplitter.isEmpty()) {
				// Boring data type conversion from a huge list of data rows to session data type
				// For convenience and performance, we created a map just in case DataRows might now be sorted by the session number
				// It is possible specifically in splitting methods that does not solely depend on time
				sessionsMap = new HashMap<Long, Session>();
				Session session = null;
				long windowID = -1;
				for (int j = 0; j < inputToSequenceSplitter.size(); j++) {
					windowID = inputToSequenceSplitter.get(j).getWindowId();
					
					if (sessionsMap.containsKey(windowID)) {
						sessionsMap.get(windowID).addQuery(inputToSequenceSplitter.get(j));
					} else {
						
						session = new Session(windowID);
						session.addQuery(inputToSequenceSplitter.get(j));
						
						sessionsMap.put(windowID, session);
					}
					
				}
				
				
				// Put the sessions into a list
				
				List<Session> sessions = new ArrayList<Session>();
				
				Iterator<Long> it = sessionsMap.keySet().iterator();
				long tempValue = -1;
				
				while(it.hasNext()) {
					tempValue = it.next();
					sessions.add(sessionsMap.get(tempValue));
				}
				
				//This sorts sessions based on their start time in ascending order
				Collections.sort(sessions);
				
				userSessions.put(userFiles.get(i), sessions);
				
				//This gives us the clustering appointments of the sessions
				WorkloadAnalyzer workloadAnalyzer = new WorkloadAnalyzer(workingDirectory, userFiles.get(i));
				
				WorkloadComparator workloadComparator = null;
				
				//This part finally gives us the experimental results
				lStartTime = System.nanoTime();
				sessions = workloadAnalyzer.performSessionClustering(sessions, "ClusterBasedJaccard");
				workloadComparator = new WorkloadComparator(sessions, PARTITION_COUNT, "This data belongs to ClusterBasedJaccard method");
				PartitionSimilarityStruct tempStruct = workloadComparator.getComparisonOfFrequencies(userFiles.get(i));
				lEndTime = System.nanoTime();
				output = lEndTime - lStartTime;
				tempStruct.setTimeElapsed((1.0 * output / 1000000000) + "");
				partitionSimilarities.add(tempStruct);
				System.out.println("Analysis with ClusterBasedJaccard for " + userFiles.get(i) + " - Elapsed time in seconds: " + (1.0 * output / 1000000000));
				
				lStartTime = System.nanoTime();
				sessions = workloadAnalyzer.performSessionClustering(sessions, "JSDivergence");
				workloadComparator = new WorkloadComparator(sessions, PARTITION_COUNT, "This data belongs to JSDivergence method");
				lEndTime = System.nanoTime();
				output = lEndTime - lStartTime;
				System.out.println("Analysis with JSDivergence for " + userFiles.get(i) + " - Elapsed time in seconds: " + (1.0 * output / 1000000000));
				
				lStartTime = System.nanoTime();
				sessions = workloadAnalyzer.performSessionClustering(sessions, "FeatureBasedJaccard");
				workloadComparator = new WorkloadComparator(sessions, PARTITION_COUNT, "This data belongs to FeatureBasedJaccard method");
				lEndTime = System.nanoTime();
				output = lEndTime - lStartTime;
				System.out.println("Analysis with FeatureBasedJaccard for " + userFiles.get(i) + " - Elapsed time in seconds: " + (1.0 * output / 1000000000));
				
				
			}
			
			
		}
		
		
		int partitions = partitionSimilarities.size();
		if (partitions > 0)
			partitions = partitionSimilarities.get(0).getSimilarities().size();
		double average = 0;
		for (int i = 0; i < partitions; i++) {
			for (int j = 0; j < partitionSimilarities.size(); j++) {
				average += partitionSimilarities.get(j).getSimilarities().get(i);
				if (j == partitionSimilarities.size() -1) {
					average = average / partitionSimilarities.size();
					System.out.println("For all users, average of the comparison of partition " + i + " and others is " + average );
				}
			}
		}
		
		writePartitionSimilaritiesToFile(partitionSimilarities, "ClusterBasedJaccard");
		
	}
	
	
	public void runPredictionExperiment(String startTime, String endTime) {
		
		//This line performs the preprocessing (clustering and preparing unique queries)
		
		DataReader workloadReader = new DataReaderPhoneLabLog(workingDirectory, startTime, endTime);
		
		List<String> userFiles = workloadReader.getUserFiles();
		
		//Preparing to find out the sessions
		SessionSplitter s = null;
		List<DataRow> inputToSequenceSplitter = null;
		
		HashMap<String, List<Session>> userSessions = new HashMap<String, List<Session>>();
		List<PartitionSimilarityStruct> partitionSimilarities = new ArrayList<PartitionSimilarityStruct>();
		
		for (int i = 0; i < userFiles.size(); i++) {
			s = new SessionSplitter();
			
			s.setWorkingDirectory(workingDirectory);
			
			//Matching cluster numbers appointed to the queries to actual query log
			inputToSequenceSplitter = SequenceMerger.exportForSessionSplitter(this.workingDirectory, userFiles.get(i), workloadReader.getUniqueQueries());
			
			s.importClusteringData(inputToSequenceSplitter);
		}
		
		
		int partitions = partitionSimilarities.size();
		if (partitions > 0)
			partitions = partitionSimilarities.get(0).getSimilarities().size();
		double average = 0;
		for (int i = 0; i < partitions; i++) {
			for (int j = 0; j < partitionSimilarities.size(); j++) {
				average += partitionSimilarities.get(j).getSimilarities().get(i);
				if (j == partitionSimilarities.size() -1) {
					average = average / partitionSimilarities.size();
					System.out.println("For all users, average of the comparison of partition " + i + " and others is " + average );
				}
			}
		}
		
		writePartitionSimilaritiesToFile(partitionSimilarities, "ClusterBasedJaccard");
		
	}
	
	
	private void writePartitionSimilaritiesToFile(List<PartitionSimilarityStruct> partitionSimilarities, String method) {
		
		try {
			BufferedWriter output_partitionSimilarities = new BufferedWriter(new FileWriter(new File(
					this.workingDirectory + File.separator + method + ".csv")));
			
			for(int i = 0; i< partitionSimilarities.size(); i++) {
				output_partitionSimilarities.write(partitionSimilarities.get(i).toString() + "\n");
			}
			output_partitionSimilarities.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex.getMessage());
		}
	}
	
	
	
	
	
	
	private TreeMap<LocalDateTime, Integer> fillTreeMap(LocalDateTime startTime, LocalDateTime endTime) {
		TreeMap<LocalDateTime, Integer> retVal = null;
		
		if (startTime.isBefore(endTime)) {
			retVal = new TreeMap<LocalDateTime, Integer>();
			
			while (startTime.isBefore(endTime)) {
				
				retVal.put(startTime, 0);
				
				startTime = startTime.plusSeconds(1);
			}
			
		}
		
		return retVal;
	}
	
	public void runQueryCountPerUnit(String startTime, String endTime) {
		
		
		String[] listOfFiles = new File(workingDirectory).list();
		
		LocalDateTime startLocalTime = Util.parseFromDateStringToLocalTime(startTime);
		LocalDateTime endLocalTime = Util.parseFromDateStringToLocalTime(endTime);
		
		TreeMap<LocalDateTime, Integer> mapToBePlotted = new TreeMap<LocalDateTime, Integer>(); 
		
		for (int i = 0; i < listOfFiles.length; i++) {
		
			String file = listOfFiles[i];
			
			try {
				FileInputStream fis = new FileInputStream(file);
				GZIPInputStream gzis = new GZIPInputStream(fis);
				InputStreamReader reader = new InputStreamReader(gzis);
				BufferedReader in = new BufferedReader(reader);
				
				String readLine;
				String columns[] = null;
				String timestamp;
				String query;
				LocalDateTime timeValue = null;
				
				mapToBePlotted = fillTreeMap(startLocalTime, endLocalTime);
				
				while ((readLine = in.readLine()) != null) {
					columns = readLine.split("\t");
					
					timestamp = columns[0]; //TODO this is wrong! Find the correct column that has human readable date info!
					query = columns[4];
					
					timeValue = Util.parseFromDateStringToLocalTime(timestamp);
					
					//mapToBePlotted.put(timeValue, value);
					
				}
				
				
				in.close();
				reader.close();
				gzis.close();
				fis.close();
				
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		
	}
	
	
	public void runQueryCountPerUnit(String file) {
		
		
		//String[] listOfFiles = new File(workingDirectory).list();
		
		LocalDateTime startLocalTime = null;
		LocalDateTime endLocalTime = null;
		
		TreeMap<LocalDateTime, Integer> mapToBePlotted = new TreeMap<LocalDateTime, Integer>();
		List<String> inMemoryList = new ArrayList<String>();
		
		
			
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(fis);
			BufferedReader in = new BufferedReader(reader);
			
			boolean firstLine = true;
			String readLine = "";
			String previousLine = "";
			
			while ((readLine = in.readLine()) != null) {
				
				if (firstLine == true) {
					startLocalTime = Util.parseFromDateStringToLocalTimeSecond(readLine.substring(0, 19));
					firstLine = false;
				}
				
				if (readLine.contains("com.facebook") && readLine.contains("stmt_text")) {
					inMemoryList.add(readLine);
				}
				
				previousLine = readLine.toString();
				
			}
			
			endLocalTime = Util.parseFromDateStringToLocalTimeSecond(previousLine.substring(0, 19));
			
			mapToBePlotted = fillTreeMap(startLocalTime, endLocalTime);
			
			in.close();
			reader.close();
			fis.close();
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
		LocalDateTime tempTime = null;
		
		for (int i = 0; i < inMemoryList.size(); i++) {
			
			tempTime = Util.parseFromDateStringToLocalTimeSecond(inMemoryList.get(i).substring(0, 19));
			
			if(mapToBePlotted.containsKey(tempTime)) {
				mapToBePlotted.put(tempTime, mapToBePlotted.get(tempTime) + 1);
			}
			
		}
		
		
		Iterator<LocalDateTime> it = mapToBePlotted.keySet().iterator();
		
		try {
			FileOutputStream fos = new FileOutputStream(file + "counts.csv");
			OutputStreamWriter writer = new OutputStreamWriter(fos);
			BufferedWriter in = new BufferedWriter(writer);
			
			int counter = 0;
			
			while(it.hasNext()) {
				
				LocalDateTime itTime = it.next();
				
				in.write(itTime + "\t" + counter + "\t" + mapToBePlotted.get(itTime) + "\n");
				
				counter++;
			}
			
			in.close();
			writer.close();
			fos.close();
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
	}
	
	
	public void runQueryCountPerApp(String directory, String startDate, String endDate) {
		
		String[] listOfFilesArray = new File(directory).list();
		List<String> listOfFiles = Util.getFileList(listOfFilesArray);
		
		
		int totalCount = 0;
		int insertCount = 0;
		int deleteCount = 0;
		int updateCount = 0;
		int selectCount = 0;
		int unionCount = 0;
		int otherCount = 0;
		
		
		LocalDateTime startLocalDateTime = Util.parseFromDateStringToLocalTime(startDate);
		LocalDateTime endLocalDateTime = Util.parseFromDateStringToLocalTime(endDate);
		
		Set<String> userList = new HashSet<String>();
		Set<String> uniqueQueries = new HashSet<String>();
		
		for (int i = 0; i < listOfFiles.size(); i++) {
		
			String file = directory + listOfFiles.get(i);
			
			try {
				FileInputStream fis = new FileInputStream(file);
				GZIPInputStream gzis = new GZIPInputStream(fis);
				InputStreamReader reader = new InputStreamReader(gzis);
				BufferedReader in = new BufferedReader(reader);
				
				String readLine;
				String columns[] = null;
				String query;
				LocalDateTime queryTime = null;
				
				while ((readLine = in.readLine()) != null) {
					columns = readLine.split("\t");
					
					queryTime = Util.parseFromLongToLocalTime(Long.parseLong(columns[2]));
					
					if (queryTime.isAfter(startLocalDateTime) && queryTime.isBefore(endLocalDateTime)) {
						
						userList.add(file);
						
						query = columns[4].toLowerCase();
						
						if (query.startsWith("insert")) {
							insertCount++;
						} else if (query.startsWith("update")) {
							updateCount++;
						} else if (query.startsWith("delete")) {
							deleteCount++;
						} else if (query.startsWith("select")) {
							selectCount++;
						} else if (query.startsWith("union")) {
							unionCount++;
						} else {
							//System.out.println(query);
							otherCount++;
						}
						
						uniqueQueries.add(columns[4]);
						totalCount++;
					}
				}
				
				in.close();
				reader.close();
				gzis.close();
				fis.close();
				
				System.out.println("There are " + listOfFiles.size() + " users.");
				System.out.println(file + " completed.");
				
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		
		System.out.println("Searched over " + directory);
		
		System.out.println("There are " + listOfFiles.size() + " users in total.");
		System.out.println("There are " + userList.size() + " users between the given dates.");
		System.out.println("There are " + insertCount + " insert queries.");
		System.out.println("There are " + updateCount + " update queries.");
		System.out.println("There are " + deleteCount + " delete queries.");
		System.out.println("There are " + selectCount + " select queries.");
		System.out.println("There are " + unionCount + " union queries.");
		System.out.println("There are " + otherCount + " other types of queries.");
		System.out.println("There are " + totalCount + " queries in total.");
		
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(directory + "uniqueQueries.csv.gz");
			Writer writer = new OutputStreamWriter(new GZIPOutputStream(output));
			try {
				for (String text : uniqueQueries) {
					writer.write(text + "\n");
				}
				
				writer.close();
			} catch(Exception ex) {
				System.out.println(ex.getMessage());
			} finally {
				output.close();
			}
		 } catch(Exception ex) {
			 System.out.println(ex.getMessage());
		 }
	}
	
	public void runQueryCountPerAppWithoutTime(String directory) {
		
		String[] listOfFilesArray = new File(directory).list();
		
		List<String> listOfFiles = Util.getFileList(listOfFilesArray);
		
		
		int totalCount = 0;
		int insertCount = 0;
		int deleteCount = 0;
		int updateCount = 0;
		int selectCount = 0;
		int unionCount = 0;
		int otherCount = 0;
		
		Set<String> userList = new HashSet<String>();
		Set<String> uniqueQueries = new HashSet<String>();
		
		for (int i = 0; i < listOfFiles.size(); i++) {
		
			String file = directory + listOfFiles.get(i);
			
			try {
				FileInputStream fis = new FileInputStream(file);
				GZIPInputStream gzis = new GZIPInputStream(fis);
				InputStreamReader reader = new InputStreamReader(gzis);
				BufferedReader in = new BufferedReader(reader);
				
				String readLine;
				String columns[] = null;
				String query;
				
				while ((readLine = in.readLine()) != null) {
					columns = readLine.split("\t");
					userList.add(file);
					
					query = columns[4].toLowerCase();
					
					if (query.startsWith("insert")) {
						insertCount++;
					} else if (query.startsWith("update")) {
						updateCount++;
					} else if (query.startsWith("delete")) {
						deleteCount++;
					} else if (query.startsWith("select")) {
						selectCount++;
					} else if (query.startsWith("union")) {
						unionCount++;
					} else {
						//System.out.println(query);
						otherCount++;
					}
					
					uniqueQueries.add(columns[4]);
					totalCount++;
				}
				
				in.close();
				reader.close();
				gzis.close();
				fis.close();
				
				System.out.println("There are " + listOfFiles.size() + " users.");
				System.out.println(file + " completed.");
				
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		
		System.out.println("Searched over " + directory);
		
		System.out.println("There are " + listOfFiles.size() + " users in total.");
		System.out.println("There are " + userList.size() + " users between the given dates.");
		System.out.println("There are " + insertCount + " insert queries.");
		System.out.println("There are " + updateCount + " update queries.");
		System.out.println("There are " + deleteCount + " delete queries.");
		System.out.println("There are " + selectCount + " select queries.");
		System.out.println("There are " + unionCount + " union queries.");
		System.out.println("There are " + otherCount + " other types of queries.");
		System.out.println("There are " + totalCount + " queries in total.");
		
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(directory + "uniqueQueriesWithoutTime.csv.gz");
			Writer writer = new OutputStreamWriter(new GZIPOutputStream(output));
			try {
				for (String text : uniqueQueries) {
					writer.write(text + "\n");
				}
				
				writer.close();
			} catch(Exception ex) {
				System.out.println(ex.getMessage());
			} finally {
				output.close();
			}
		 } catch(Exception ex) {
			 System.out.println(ex.getMessage());
		 }
	}
	
	
	public void runExperiment1() {
		
		PairwiseDistance workingObject = new PairwiseDistance(workingDirectory);
		
		//Determining how many users' data we have for this specific app
        int userSize = workingObject.getUserSize(appName);
        
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        
        //Creating distance matrices for all users
        System.out.println("Calculating distance matrix for all users");
        System.out.println("Start time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        
        //TODO run the script that creates the unique query list on the given folder/working directory here.
        
        //TODO perform clustering on the prepared unique query file
        
        workingObject.createDistanceMatrixForAll(appName);
                
        System.out.println("End time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
		
        
        
        //Creating a list of time intervals
        //100 -> 0.1sn, 1000 -> 1sn, 5000 -> 5sn, 10000 -> 10sn, 30000 -> 30sn, 60000 -> 1min, 300000 -> 5min, 600000 -> 10min, 1800000 -> 30min
        List<Integer> idleTimes = new ArrayList<>(Arrays.asList(100, 1000, 5000, 10000, 30000, 60000, 300000, 600000, 1800000));
        
		SessionSplitter s = new SessionSplitter();
		s.setWorkingDirectory(this.workingDirectory, appName);
		
		List<DataRow> inputToSequenceSplitter = SequenceMerger.exportForSessionSplitter(workingObject, appName);
		
		//TODO convert input time info to Long
		s.importClusteringData(inputToSequenceSplitter);
		
		//As a side effect, this creates a file that keeps the session counts per time interval for each user
		s.findIdealIdleTimesForAllUsersMap(idleTimes);
		
		//TODO feed the sessions into
		
		
        
	}
	
	
	
	public void runExperimentX() {
        
        PairwiseDistance workingObject = new PairwiseDistance(workingDirectory);
        
        //Determining how many users' data we have for this specific app
        int userSize = workingObject.getUserSize(appName);
        
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        
        /*
        //Creating distance matrices for each user separately
        for (int i = 0; i < userSize; i++) {
        	System.out.println("Calculating distance matrix for user " + i);
            //System.out.println("Start time: " + sdf.format(cal.getTime()) );
            System.out.println("Start time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            workingObject.createDistanceMatrixForUser(appName, i);
            
            //System.out.println("End time: " + sdf.format(cal.getTime()) );
            System.out.println("End time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        }
         */
        
        //Creating distance matrices for all users
        System.out.println("Calculating distance matrix for all users");
        //System.out.println("Start time: " + sdf.format(cal.getTime()) );
        System.out.println("Start time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        
        workingObject.createDistanceMatrixForAll(appName);
                
        //System.out.println("End time: " + sdf.format(cal.getTime()) );
        System.out.println("End time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        
        //We perform clustering using all the queries

        String[] clusteringParameters = new String[2];
        clusteringParameters[0] = workingObject.getDataDirectory() + appName + File.separator +"clusterAll.R";
        clusteringParameters[1] = workingObject.getDataDirectory() + appName;
        	
        ScriptExecuter.runCommand("Rscript --vanilla", clusteringParameters);
        
        //String[] querySimilarityParameters = new String[1];
        //querySimilarityParameters[0] = workingObject.getDataDirectory() + appName + "/clusterAll.R";
        
        //ScriptExecuter.runCommand("python", args);
		
        //Open the pdf file to determine how many clusters there should be
        if (Desktop.isDesktopSupported()) {
        	try {
        		File theDendrogram = new File(workingObject.getDataDirectory() + appName + File.separator +"dendrogram.pdf");
        		Desktop.getDesktop().open(theDendrogram);
        	} catch (FileNotFoundException fnf){
        			
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        } else {
        	System.out.println("Environment is not suitable to open Pdf file visually.");
        }        
        //			SequenceMerger.recreateTheLog(workingObject, appName);
        int idleTimeTolerance = 1000;
        
        //TODO automatic idle time tolerance identification
        
		List<DataRow> inputToSequenceSplitter = SequenceMerger.exportForSessionSplitter(workingObject, appName);
		SessionSplitter s = new SessionSplitter();
		s.importClusteringData(inputToSequenceSplitter);
		List<OutputDataRow> avgSimWindows = s._calculateAverageSimilarityForWindows(idleTimeTolerance);
		String outFilePath = workingObject.getDataDirectory() + appName + File.separator + "sessions.csv";
		Boolean resultsExportStatus = s.exporttoCSV(avgSimWindows, outFilePath );

		System.out.println("Size: "+inputToSequenceSplitter.size()+" Fileout :: "+resultsExportStatus);
		
		LinkedHashMap<String, LinkedHashMap<Long, List<DataRow>>> sessions = s.exportResults();
		
		Iterator<String> it = sessions.keySet().iterator();
		String temp = null;
		while(it.hasNext()) {
			temp = it.next();
			System.out.println(temp + " : " + sessions.get(temp).size());
		}
	}

}
