package edu.buffalo.www.cse.odinlab.ettu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation.Analyze;
import edu.buffalo.www.odinlab.statlib.Stat;
import edu.buffalo.www.odinlab.statlib.StatLib;
import edu.illinois.dais.ttr.Statistics;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import querySimilarityMetrics.Makiyama;

public class BankExperimentSetup {
	private String workingDirectory = "";
	private String appName = "";
	private Set<String> users = null;
	private int userSize = 0;
	private ArrayList<TreeMap<String, Integer>> experimentData = null;
	private ArrayList<TreeMap<String, Integer>> trainingQueries = null;
	private ArrayList<TreeMap<String, Integer>> testQueries = null;
	private int traningDatasetSize;
	private int testDatasetSize;
	
	public BankExperimentSetup(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	public void setupUserInfo() {
		this.users = this.getUsers();
		this.userSize = this.users.size();
		System.out.println("There are " + this.userSize + " users for the bank");
	}
	
	public void setupExperiment1ver2() {
		//For each user, take the last day for testing and calculate KL-Divergence
		
		System.out.println("Experiment 1 Hourly started for bank app.........................");
		
		try {
			String fileName = workingDirectory + "/ettuExperiment1ver2.csv";
			
			BufferedWriter output_FinalResult = new BufferedWriter(new FileWriter(new File(fileName)));
			
			Iterator iterator = this.users.iterator();
			
			while (iterator.hasNext()) {
				
				String userId = (String) iterator.next();
				
				System.out.println("Results for User " + userId + ":");
				
				HashMap<String, ArrayList<TreeMap<String, Integer>>> experimentData = getWorkloadForDatasetDaily(userId);
				
				System.out.println("User" + "," + "Day" + "," + "TrainingSize" + "," + "TestSize" + "," + "Result" + "," + "Date");
				
				NumberFormat formatterScore = new DecimalFormat("#0.0000");
				
				
					this.trainingQueries = null;
					this.testQueries = null;
					int counter = 1;
					
					//SortedSet<String> asd = new TreeSet<String>();
					Iterator<String> it = new TreeSet<String>(experimentData.keySet()).iterator();
					
					while (it.hasNext()) {
						String date = it.next();
						ArrayList<TreeMap<String,Integer>> tempData = experimentData.get(date);
						
						if (this.testQueries == null || this.testQueries.size() == 0) {
							this.testQueries = new ArrayList<TreeMap<String, Integer>>();
							this.testQueries.addAll(tempData);
							continue;
						}
						
						if (this.trainingQueries == null || this.trainingQueries.size() == 0) {
							this.trainingQueries = new ArrayList<TreeMap<String, Integer>>();
						}
						
						this.trainingQueries.addAll(this.testQueries);
						this.testQueries.clear();
						this.testQueries.addAll(tempData);
						
						
						
						if (!(this.testQueries == null || this.testQueries.size() == 0)) {
							TreeMap<String, Integer> trainingDist = createDistribution(this.trainingQueries);
							TreeMap<String, Integer> testDist = createDistribution(this.testQueries);
							
							double result = compareWithKLDivergence(testDist, trainingDist);
							
							output_FinalResult.write(userId + "," + Integer.parseInt(date.substring(date.length() - 2)) + "," + this.trainingQueries.size() + "," + this.testQueries.size() + "," + formatterScore.format(result) + "," + date + "\n");
							
							System.out.println(userId + "," + Integer.parseInt(date.substring(date.length() - 2)) + "," + this.trainingQueries.size() + "," + this.testQueries.size() + "," + formatterScore.format(result) + "," + date);
						}
						
						counter++;
						
					}
					System.out.println("Experiment 1 for " + appName + " is successfully completed.");
				
			}
			output_FinalResult.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			
			System.out.println("Something went wrong while running Experiment 1 for " + appName + ". Operation FAILED.");
		}
	}
	
	
	public void setupExperiment2() {
		
		System.out.println("Experiment 2 started.........................");
		
		//Take a each user's data for testing and
		//calculate KL-Divergence value comparing it with other users
		
		List<TreeMap<String, Integer>> distributions = new ArrayList<TreeMap<String, Integer>>();
		
		Iterator it  = users.iterator();
		
		while(it.hasNext()) {
			
			String userId = (String) it.next();
			
			distributions.add(createDistribution(getWorkloadForUser(userId)));
		}
		
		double[][] userDifference = new double[userSize][userSize];
		NumberFormat formatterScore = new DecimalFormat("#0.0000");
		
		for (int i = 0; i < userSize; i++) {
			for (int j = 0; j < userSize; j++) {
				userDifference[i][j] = compareWithKLDivergence(distributions.get(i), distributions.get(j));
				
				System.out.println("User" + i + " vs User" + j + ": " + formatterScore.format(userDifference[i][j]));
			}
		}
		
		if (writeToFileExperiment2(userDifference)) {
			System.out.println("Experiment 2 for " + appName + " is successfully completed.");
		} else {
			System.out.println("Something went wrong while running Experiment 2 for " + appName + ". Operation FAILED.");
		}
	}
	
	
	private boolean writeToFileExperiment2(double[][] userDifference) {
		String fileName = workingDirectory + "/ettuExperiment2ver1.csv";
		
		try {
			BufferedWriter output_FinalResult = new BufferedWriter(new FileWriter(new File(fileName)));
			
			for (int i = 0; i < userDifference.length; ++i) {
				for (int j = 0; j < userDifference.length; ++j) {
					if (j == userDifference.length - 1) {
						output_FinalResult.write(userDifference[i][j] + "\n");
					} else {
						output_FinalResult.write(userDifference[i][j] + ",");
					}
				}
			}
			
			output_FinalResult.close();
			
			//return true;
		}
		catch (Exception ex) {
			return false;
		}
		
		fileName = workingDirectory + "/ettuExperiment2ver2.csv";
		
		try {
			BufferedWriter output_FinalResult = new BufferedWriter(new FileWriter(new File(fileName)));
			
			for (int i = 0; i < userDifference.length; ++i) {
				for (int j = 0; j < userDifference.length; ++j) {
					output_FinalResult.write(i + "," + j + "," + userDifference[i][j] + "\n");
				}
			}
			
			output_FinalResult.close();
			
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}
	
	public void setupExperiment3() {
		
		System.out.println("Experiment 3 started for " + appName + ".........................");
		
		//Take a each user's data for testing and
		//calculate KL-Divergence value comparing it with other users
		
		List<TreeMap<String, Integer>> distributions = new ArrayList<TreeMap<String, Integer>>();
		
		Iterator it = users.iterator();
		
		while(it.hasNext()) {
			String userId = (String) it.next();
			distributions.add(createDistribution(getWorkloadForUser(userId)));
		}
		
		double[][] userDifference = new double[userSize][userSize];
		NumberFormat formatterScore = new DecimalFormat("#0.0000");
		
		for (int i = 0; i < this.users.size(); i++) {
			for (int j = 0; j < this.users.size(); j++) {
				userDifference[i][j] = compareWithKLDivergence(distributions.get(i), distributions.get(j));
				
				System.out.println("User" + i + " vs User" + j + ": " + formatterScore.format(userDifference[i][j]));
			}
		}
		
		if (writeToFileExperiment2(userDifference)) {
			System.out.println("Experiment 2 for " + appName + " is successfully completed.");
		} else {
			System.out.println("Something went wrong while running Experiment 2 for " + appName + ". Operation FAILED.");
		}
	}
	
	private boolean writeToFileExperiment3(double[][] userDifference) {
		String fileName = workingDirectory + "/ettuExperiment3.csv";
		
		try {
			BufferedWriter output_FinalResult = new BufferedWriter(new FileWriter(new File(fileName)));
			
			for (int i = 0; i < userDifference.length; ++i) {
				for (int j = 0; j < userDifference.length; ++j) {
					output_FinalResult.write(i + "," + j + "," + userDifference[i][j] + "\n");
				}
			}
			
			output_FinalResult.close();
			
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}
	
	private TreeMap<String, Integer> createDistribution(List<TreeMap<String, Integer>> workloadForUser) {
		
		TreeMap<String, Integer> finalDistribution = new TreeMap<String, Integer>();
		TreeMap<String, Integer> tempDistribution = null;
		
		for (int i = 0; i < workloadForUser.size(); i++) {
			TreeSet<String> union = new TreeSet<String>();
			union.addAll(finalDistribution.keySet());
			union.addAll(workloadForUser.get(i).keySet());
			
			tempDistribution = new TreeMap<String, Integer>();
			
			for (String columnName : union) {
				
				int counter = 0;
				
				if (finalDistribution.containsKey(columnName)) {
					counter += finalDistribution.get(columnName);
				}
				if (workloadForUser.get(i).containsKey(columnName)) {
					counter += workloadForUser.get(i).get(columnName);
				}
				
				tempDistribution.put(columnName, counter);
			}
			
			finalDistribution.putAll(tempDistribution);
		}
		
		return finalDistribution;
		
	}

	private double compareWithKLDivergence(TreeMap<String, Integer> distribution1, TreeMap<String, Integer> distribution2) {
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

	/**
	 * This method creates distance matrices for hierarchical clustering
	 * 
	 * @param appName
	 *            (MediaStorage, Facebook, GooglePlus, Hangouts)
	 * @param userID
	 */
	public ArrayList<TreeMap<String, Integer>> getWorkloadForUser(String userID) {
		toolsForMetrics.Global.tableAlias = new HashMap<String, String>();
		ArrayList<TreeMap<String, Integer>> vectorList = new ArrayList<TreeMap<String, Integer>>();
		try {
			
			HashMap<String, ArrayList<Integer>> uniqueStrings = new HashMap<String, ArrayList<Integer>>();

				int count = 0;
				int countParsable = 0;

				String fileName = workingDirectory + "/user" + userID
						+ ".csv";
				System.out.println(fileName);
				BufferedReader input = new BufferedReader(new FileReader(new File(fileName)));
				String line = input.readLine();
				Statement stmt;
				TreeMap<String, Integer> vector;
				String query = null;

				try {
					while (line != null) {
						stmt = null;
						count++;
						String[] arr = line.split("\t");
						//if (arr[4].contains("SQLiteProgram: ")) {
						query = arr[arr.length-1].replace("SQLiteProgram: ", "");
						query = query.replace("[", "");
						query = query.replace("]", "");
						query = query.replace("\u202a", "");
						query = query.replace("\u202c", "");
						query = query.replace("\ub808", "");
						query = query.replace("\uc774", "");
						query = query.replace("\ube10", "");
						query = query.replace("\u001a", "");
						//}
						// System.out.println(arr[1]);
						InputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
							try {
								CCJSqlParser parser = new CCJSqlParser(stream);
								//System.out.println(count + " " + query);
								stmt = parser.Statement();
								// queryList.add(stmt);
								if (stmt instanceof Select) {
									ArrayList<Integer> responseTime = null;
									//if (uniqueStrings.containsKey(arr[4])) {
									//	responseTime = uniqueStrings.get(arr[4]);
									//} else {
									//	responseTime = new ArrayList<Integer>();
									//}
									//responseTime.add(Integer.parseInt(arr[7]));
									uniqueStrings.put(query, responseTime);
								}
								countParsable++;
							} catch (Exception ex) {
								// System.out.println(ex);
								// System.out.println(arr[4] + " is not a valid SQL
								// query.");
							}

						line = input.readLine();
					}
				} catch (Exception ex) {
					System.out.println(line);
				}

				input.close();
				//System.out.println("Total lines: " + count);
				System.out.println("Total parsable select queries: " + countParsable);
				System.out.println("Unique strings: " + uniqueStrings.size());

				Iterator<String> it = uniqueStrings.keySet().iterator();
				//int counter = 1;
				while (it.hasNext()) {
					String temp = it.next();
					InputStream stream = new ByteArrayInputStream(temp.getBytes(StandardCharsets.UTF_8));
					CCJSqlParser parser = new CCJSqlParser(stream);
					stmt = parser.Statement();
					// output_MakiyamaQuery.write(counter + ", " + temp + "\n");
					//counter++;
					vector = Makiyama.getQueryVector(stmt);
					vectorList.add(vector);
				}
				// output_MakiyamaQuery.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return vectorList;
	}
	
	/**
	 * This method creates distance matrices for hierarchical clustering
	 * 
	 * @param appName
	 *            (MediaStorage, Facebook, GooglePlus, Hangouts)
	 * @param userID
	 */
	public ArrayList<TreeMap<String, Integer>> getWorkloadForDataset(String appName, int userID) {
		toolsForMetrics.Global.tableAlias = new HashMap<String, String>();
		ArrayList<TreeMap<String, Integer>> vectorList = new ArrayList<TreeMap<String, Integer>>();
		try {
			int count = 0;
			int countParsable = 0;
			
			HashMap<String, TreeMap<String, Integer>> uniqueStrings = new HashMap<String, TreeMap<String, Integer>>();
			for (int user = userID; user <= userID; user++) {

				String fileName = workingDirectory + "/user" + userID
						+ ".csv";
				//System.out.println(fileName);
				BufferedReader input = new BufferedReader(new FileReader(new File(fileName)));
				String line = input.readLine();
				Statement stmt;
				TreeMap<String, Integer> vector;
				String query = null;

				try {
					while (line != null) {
						stmt = null;
						count++;
						String[] arr = line.split("\t");
						query = arr[4].replace("SQLiteProgram: ", "");
						query = query.replace("[", "");
						query = query.replace("]", "");
						query = query.replace("\u202a", "");
						query = query.replace("\u202c", "");
						query = query.replace("\ub808", "");
						query = query.replace("\uc774", "");
						query = query.replace("\ube10", "");
						query = query.replace("\u001a", "");
						// System.out.println(arr[1]);
						if (uniqueStrings.containsKey(query) && uniqueStrings.get(query) != null) {
							vectorList.add(uniqueStrings.get(query));
						}
						else {
							InputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
							try {
								CCJSqlParser parser = new CCJSqlParser(stream);
								stmt = parser.Statement();
								// queryList.add(stmt);
								if (stmt instanceof Select) {
									vector = Makiyama.getQueryVector(stmt);
									uniqueStrings.put(query, vector);
									vectorList.add(vector);
								} else {
									uniqueStrings.put(query, null);
								}
								countParsable++;
							} catch (Exception ex) {
								// System.out.println(ex);
								// System.out.println(arr[4] + " is not a valid SQL
								// query.");
							}
						}

						line = input.readLine();
					}
				} catch (Exception ex) {
					System.out.println(line);
				}

				input.close();
				//System.out.println("Total lines: " + count);
				System.out.println("Total parsable select queries: " + countParsable);
				//System.out.println("Unique strings: " + uniqueStrings.size());
				// output_MakiyamaQuery.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return vectorList;
	}



	/**
	 * This method creates distance matrices for hierarchical clustering
	 * 
	 * @param appName
	 *            (MediaStorage, Facebook, GooglePlus, Hangouts)
	 * @param userID
	 */
	public HashMap<String, ArrayList<TreeMap<String, Integer>>> getWorkloadForDatasetDaily(String userID) {
		toolsForMetrics.Global.tableAlias = new HashMap<String, String>();
		HashMap<String, ArrayList<TreeMap<String, Integer>>> vectorList = new HashMap<String, ArrayList<TreeMap<String, Integer>>>();
		try {
			int count = 0;
			int countParsable = 0;
			
			HashMap<String, TreeMap<String, Integer>> uniqueStrings = new HashMap<String, TreeMap<String, Integer>>();


				String fileName = workingDirectory + "/user" + userID + ".csv";
				//System.out.println(fileName);
				BufferedReader input = new BufferedReader(new FileReader(new File(fileName)));
				String line = input.readLine();
				Statement stmt;
				TreeMap<String, Integer> vector;
				String query = null;

				try {
					while (line != null) {
						stmt = null;
						count++;
						String[] arr = line.split("\t");
						query = arr[1].replace("SQLiteProgram: ", "");
						query = query.replace("[", "");
						query = query.replace("]", "");
						query = query.replace("\u202a", "");
						query = query.replace("\u202c", "");
						query = query.replace("\ub808", "");
						query = query.replace("\uc774", "");
						query = query.replace("\ube10", "");
						query = query.replace("\u001a", "");
						
						String dateString = arr[0];
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
						Date myDate = sdf.parse(dateString);
						String date = sdf.format(myDate);
						date = date.substring(0, 13); //Taking only the hour
						
						// System.out.println(arr[1]);
						InputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
						try {
							CCJSqlParser parser = new CCJSqlParser(stream);
							stmt = parser.Statement();
							// queryList.add(stmt);
							if (stmt instanceof Select) {
								if (uniqueStrings.containsKey(query) && uniqueStrings.get(query) != null) {
									vector = uniqueStrings.get(query);
								} else {
									uniqueStrings.put(query, Makiyama.getQueryVector(stmt));
									vector = uniqueStrings.get(query);
								}
								
								if (vectorList.containsKey(date)) {
									ArrayList<TreeMap<String, Integer>> tempList = vectorList.get(date);
									tempList.add(vector);
									vectorList.put(date, tempList);
								} else {
									ArrayList<TreeMap<String, Integer>> tempList = new ArrayList<TreeMap<String, Integer>>();
									tempList.add(vector);
									vectorList.put(date, tempList);
								}
							}
							countParsable++;
						} catch (Exception ex) {
							// System.out.println(ex);
							// System.out.println(arr[4] + " is not a valid SQL
							// query.");
						}

						line = input.readLine();
					}
				} catch (Exception ex) {
					System.out.println(line);
				}

				input.close();
				//System.out.println("Total lines: " + count);
				System.out.println("Total parsable select queries: " + countParsable);
				//System.out.println("Unique strings: " + uniqueStrings.size());
				// output_MakiyamaQuery.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return vectorList;
	}

	
	private void splitData(double testDataRatio) {
		
		if (testDataRatio > 1.0 || testDataRatio < 0 || this.experimentData == null) {
			System.out.println("The ratio entered should be between zero and one.");
			return;
		}
		
		//Find the total session number
		int totalSize = this.experimentData.size();
				
		int testSize = (int)(totalSize * testDataRatio);
		int trainingSize = totalSize - testSize;
		
		this.traningDatasetSize = trainingSize;
		this.testDatasetSize = testSize;
		
		initializeTheDatasetObjects(trainingSize, testSize);
		
		for(int i = 0; i < this.experimentData.size(); i++) {
			if (i < trainingSize) {
				this.trainingQueries.add(experimentData.get(i));
			}
			else {
				this.testQueries.add(experimentData.get(i));
			}
		}
	}
	
	private void initializeTheDatasetObjects(int trainingSize, int testSize) {
		if (trainingSize > 0) { 
			this.trainingQueries = new ArrayList<TreeMap<String, Integer>>();
		}
		else {
			System.out.println("Not enough data for the training set.");
		}
		
		if (testSize > 0) {
			this.testQueries = new ArrayList<TreeMap<String, Integer>>();
		}
		else {
			System.out.println("Not enough data for the test set.");
		}
	}
	
	/**
	 * This method returns how many user files there are in the data file of the given app
	 * @param appName
	 * @return
	 */
	public Set<String> getUsers() {
		File[] files = new File(workingDirectory).listFiles();
		
		Set<String> servers = new HashSet<String>();
		
		int fileCounter = 0;

		for (File file : files) {
			if (file.isFile() && file.getName().startsWith("output_") && file.getName().endsWith(".csv")) {
				 servers.addAll(goThroughTheFileToFindUsers(file));
			} else if (file.isFile() && file.getName().startsWith("user") && file.getName().endsWith(".csv")) {
				servers.add(file.getName().substring(4, file.getName().lastIndexOf(".")));
			}
			
			fileCounter++;
			
			System.out.println("There are " + files.length + " and only " + (files.length - fileCounter) + " left to process.");
			System.out.println("There are " + servers.size() + " servers in total.");
		}
		
		return servers;
	}
	
	/**
	 * This method returns how many user files there are in the data file of the given app
	 * @param appName
	 * @return
	 */
	public Set<String> getUsersWithoutLoading() {
		File[] files = new File(workingDirectory + "/data").listFiles();
		
		Set<String> servers = new HashSet<String>();
		
		int fileCounter = 0;

		for (File file : files) {
			if (file.isFile() && file.getName().startsWith("output_") && file.getName().endsWith(".csv")) {
				 servers.addAll(goThroughTheFileToFindUsers(file));
			}
			
			fileCounter++;
			
			System.out.println("There are " + files.length + " and only " + (files.length - fileCounter) + " left to process.");
			System.out.println("There are " + servers.size() + " servers in total.");
		}
		
		return servers;
	}
	
	private Set<String> goThroughTheFileToFindUsers(File file) {
		Set<String> servers = new HashSet<String>();
		
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(fis);
			BufferedReader in = new BufferedReader(reader);

			String readLine = "";
			String[] columns = null;
			String serverHashedAddress = "";
			
			int counter = 0;

			while ((readLine = in.readLine()) != null) {
				columns = readLine.split(";");
				//System.out.println(readLine);
				//System.out.println(columns.length);
				
				if (!columns[4].equals("")) {
					serverHashedAddress = columns[4];
					servers.add(serverHashedAddress);
					
					if (writeQueryToFile(serverHashedAddress, columns[0], columns[columns.length - 1])) {
						counter++;
					}
					
					if (counter % 10000 == 0) {
						System.out.println(counter);
					}
				}
				readLine = in.readLine();
			}

			in.close();
			reader.close();
			fis.close();


		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		System.out.println("Read completed for file: " + file.getName());
		System.out.println("There are " + servers.size() + " addresses in this file.");
		
		return servers;
		
	}

	private boolean writeQueryToFile(String serverHashedAddress, String dateTime, String query) {
		
		String data = "";
		
		if (checkIfParsable(query)) {
		
			try {
				
				File tempFile = new File(
						this.workingDirectory + File.separator + "user" + serverHashedAddress + ".csv");
				
				FileOutputStream fos = new FileOutputStream(tempFile,
						true);
				
				
				
				BufferedWriter outputAppData = new BufferedWriter(new OutputStreamWriter(fos));
				
				//System.out.println("Trying to output to: " + targetFolder + File.separator + app + File.separator + fileName);
	
				data = dateTime + "\t" + query;
				
	
				outputAppData.append(data + System.lineSeparator());
				outputAppData.close();
				
				return true;
				
			} catch (Exception ex){
				
			}
			
			return false;
		}
		
		return false;
	}

	private boolean checkIfParsable(String query) {
		
		query = Analyze.fixQuery(query);
		
		String lowerCaseQuery = query.toLowerCase();
		
		if (lowerCaseQuery.startsWith("not a query") || lowerCaseQuery.startsWith("parsing error")) {
			
		} else if (lowerCaseQuery.startsWith("pragma")) {
			
		} else if (lowerCaseQuery.startsWith("begin") || lowerCaseQuery.startsWith("commit")
				|| lowerCaseQuery.startsWith("abort") || lowerCaseQuery.startsWith("rollback")
				|| lowerCaseQuery.startsWith(";")) {
			
		} else if (lowerCaseQuery.startsWith("create trigger")
				|| lowerCaseQuery.startsWith("create index")
				|| lowerCaseQuery.startsWith("attach database") || lowerCaseQuery.startsWith("analyze")
				|| lowerCaseQuery.startsWith("reindex")) {
			
		} else if (lowerCaseQuery.startsWith("alter table")) {
			
		} else {
			ByteArrayInputStream stream = null;
			CCJSqlParser parser = null;
			Statement statement = null;
			
			try {
				stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
				parser = new CCJSqlParser(stream);
				statement = parser.Statement();
				
				return true;
		
			} catch (ParseException e) {
				// System.out.println("An example of unparsable query: " + query + " : " +
				// e.getMessage());
				// System.out.println("An example of unparsable query: " + query);
			} catch (Exception e) {
				//System.out.println("Some other problem: " + query);
				//e.printStackTrace();
			} catch (Error e) {
				System.out.println("The reason JSqlParser crashed is: " + query);
			}
		
		}
		
		return false;
	}

	public ArrayList<TreeMap<String, Integer>> getTrainingDataset() {
		return trainingQueries;
	}
	
	public ArrayList<TreeMap<String, Integer>> getTestDataset() {
		return testQueries;
	}
	
	public void setExperimentData(ArrayList<TreeMap<String, Integer>> queries) {
		this.experimentData = queries;
	}
	
	/**
	 * This method returns the data folder address
	 * @return
	 */
	public String getDataDirectory() {
		return workingDirectory + "/data/";
	}
	
	/**
	 * This method returns the working directory of the program
	 * @return
	 */
	public String getWorkingDirectory() {
		return workingDirectory;
	}
}
