package edu.buffalo.www.cse.odinlab.PocketBench;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import querySimilarityMetrics.Makiyama;
import toolsForMetrics.Global;

public class PairwiseDistance {
	
	private String workingDirectory = "";
	private double[][] matrix_Makiyama = null;
	
	/**
	 * This method is the constructor object of the class PairwiseDistance
	 * @param workingDirectory
	 */
	public PairwiseDistance(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	/**
	 * This method returns the working directory of the program
	 * @return
	 */
	public String getWorkingDirectory() {
		return workingDirectory;
	}

	/**
	 * This method returns the data folder address
	 * @return
	 */
	public String getDataDirectory() {
		return workingDirectory + "/data/";
	}
	
	public double[][] getDissimilarityMatrix() {
		if (matrix_Makiyama == null) {
			System.out.println("Dissimilarity matrix is not ready yet. Try again later.");
			return null;
		}
		else {
			return matrix_Makiyama;
		}
	}

	/**
	 * This method returns how many user files there are in the data file of the given app
	 * @param appName
	 * @return
	 */
	public int getUserSize(String appName) {
		List<String> results = new ArrayList<String>();
		File[] files = new File(workingDirectory + "/data/" + appName).listFiles();
		// If this pathname does not denote a directory, then listFiles()
		// returns null.

		// int userSize = userID;

		// if (allUsers) {
		for (File file : files) {
			if (file.isFile() && (file.getName().startsWith("User") || file.getName().startsWith("user"))) {
				results.add(file.getName());
			}
		}

		// userID = 0;
		// userSize = results.size() - 1;
		// }
		return results.size();
	}
	
	
	//TODO distance matrix should be created for all unique queries
	

	/**
	 * This method creates distance matrices for hierarchical clustering
	 * 
	 * @param appName
	 *            (MediaStorage, Facebook, GooglePlus, Hangouts)
	 * @param userID
	 */
	public void createDistanceMatrixForUser(String appName, int userID) {
		// TODO Auto-generated method stub
		Global.tableAlias = new HashMap<String, String>();
		try {

			// TODO all users and separate users in the output
			// ArrayList<Statement> queryList = new ArrayList<>();
			BufferedWriter output_MakiyamaFeature = new BufferedWriter(
					new FileWriter(new File(workingDirectory + "/data/" + appName
							+ "/makiyamaFeature" + userID + ".csv")));
			BufferedWriter output_MakiyamaQuery = new BufferedWriter(new FileWriter(new File(
					workingDirectory + "/data/" + appName + "/makiyamaQuery" + userID + ".csv")));
			// BufferedWriter output_MakiyamaQueryDist = new BufferedWriter(new
			// FileWriter(new
			// File("/Users/gokhanku/Documents/Projects/PocketData/" + appName +
			// "/makiyamaQueryDist.csv")));
			BufferedWriter output_Makiyama = new BufferedWriter(new FileWriter(new File(
					workingDirectory + "/data/" + appName + "/dist_Makiyama" + userID + ".csv")));
			int count = 0;
			int countParsable = 0;

			HashSet<String> featureList = new HashSet<String>();
			HashMap<String, ArrayList<Integer>> uniqueStrings = new HashMap<String, ArrayList<Integer>>();
			ArrayList<AbstractMap.SimpleEntry<String, TreeMap<String, Integer>>> vectorList = new ArrayList<AbstractMap.SimpleEntry<String, TreeMap<String, Integer>>>();
			for (int user = userID; user <= userID; user++) {

				String fileName = workingDirectory + "/data/" +  appName + "/user" + userID
						+ ".csv";
				System.out.println(fileName);
				BufferedReader input = new BufferedReader(new FileReader(new File(fileName)));
				String line = input.readLine();
				Statement stmt;
				TreeMap<String, Integer> vector;

				try {
					while (line != null) {
						stmt = null;
						count++;
						String[] arr = line.split("\t");
						if (arr[4].contains("SQLiteProgram: ")) {
							arr[4] = arr[4].replace("SQLiteProgram: ", "");
						}
						// System.out.println(arr[1]);
						InputStream stream = new ByteArrayInputStream(arr[4].getBytes(StandardCharsets.UTF_8));
						try {
							CCJSqlParser parser = new CCJSqlParser(stream);
							stmt = parser.Statement();
							// queryList.add(stmt);
							if (stmt instanceof Select) {
								ArrayList<Integer> responseTime = null;
								if (uniqueStrings.containsKey(arr[4])) {
									responseTime = uniqueStrings.get(arr[4]);
								} else {
									responseTime = new ArrayList<Integer>();
								}
								responseTime.add(Integer.parseInt(arr[7]));
								uniqueStrings.put(arr[4], responseTime);
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
				System.out.println("Total lines: " + count);
				System.out.println("Total parsable select queries: " + countParsable);
				System.out.println("Unique strings: " + uniqueStrings.size());

				Iterator<String> it = uniqueStrings.keySet().iterator();
				int counter = 1;
				while (it.hasNext()) {
					String temp = it.next();
					InputStream stream = new ByteArrayInputStream(temp.getBytes(StandardCharsets.UTF_8));
					CCJSqlParser parser = new CCJSqlParser(stream);
					stmt = parser.Statement();
					// output_MakiyamaQuery.write(counter + ", " + temp + "\n");
					counter++;
					vector = Makiyama.getQueryVector(stmt);
					featureList.addAll(vector.keySet());
					vectorList
							.add(new AbstractMap.SimpleEntry<String, TreeMap<String, Integer>>("user" + user, vector));
				}
				// output_MakiyamaQuery.close();
			}

			Iterator<String> it = uniqueStrings.keySet().iterator();
			int counter = 1;
			ArrayList<Integer> responseTime = null;
			double averageResponseTime = 0;
			double standardDeviationOfResponseTime = 0;
			while (it.hasNext()) {
				String temp = it.next();
				responseTime = uniqueStrings.get(temp);
				averageResponseTime = computeAverageTime(responseTime);
				standardDeviationOfResponseTime = standardDeviation(responseTime);
				output_MakiyamaQuery.write(counter + ", " + temp + ", " + responseTime.size() + ", "
						+ averageResponseTime + ", " + standardDeviationOfResponseTime + "\n");
				counter++;
			}
			output_MakiyamaQuery.close();

			ArrayList<String> features = new ArrayList<String>();
			features.addAll(featureList);
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < features.size(); i++) {
				builder.append(features.get(i) + ",");
			}
			builder.append("Class");
			output_MakiyamaFeature.write(builder.toString() + "\n");

			// System.out.println("Headers are printed.");

			AbstractMap.SimpleEntry<String, TreeMap<String, Integer>> entry = null;

			for (int j = 0; j < vectorList.size(); j++) {
				builder = new StringBuilder();
				entry = vectorList.get(j);
				for (int i = 0; i < features.size(); i++) {
					if (entry.getValue().containsKey(features.get(i))) {
						builder.append(entry.getValue().get(features.get(i)) + ",");
					} else {
						builder.append("0,");
					}
				}
				builder.append(entry.getKey());

				output_MakiyamaFeature.write(builder.toString() + "\n");
			}

			// System.out.println("Vectors are printed.");

			output_MakiyamaFeature.close();

			double[][] matrix_Makiyama = new double[uniqueStrings.size()][uniqueStrings.size()];

			ArrayList<String> stringList = new ArrayList<String>();
			stringList.addAll(uniqueStrings.keySet());
			ArrayList<TreeMap<String, Integer>> treeList = new ArrayList<TreeMap<String, Integer>>();
			Statement stmt = null;
			for (int i = 0; i < stringList.size(); ++i) {
				matrix_Makiyama[i][i] = 0;
				// output_MakiyamaQueryDist.write(i + "," + stringList.get(i) +
				// "\n");
				InputStream stream = new ByteArrayInputStream(stringList.get(i).getBytes(StandardCharsets.UTF_8));
				CCJSqlParser parser = new CCJSqlParser(stream);
				stmt = parser.Statement();
				treeList.add(Makiyama.getQueryVector(stmt));
			}
			// output_MakiyamaQueryDist.close();
			for (int i = 0; i < treeList.size() - 1; ++i) {
				for (int j = i + 1; j < treeList.size(); ++j) {
					// System.out.println(i + "," + j);
					matrix_Makiyama[i][j] = 1.0 - Makiyama.getDistanceAsRatio(treeList.get(i), treeList.get(j));
					matrix_Makiyama[j][i] = matrix_Makiyama[i][j];
				}

			}

			for (int i = 0; i < treeList.size(); ++i) {
				if (i == treeList.size() - 1) {
					output_Makiyama.write(i + "\n");
				} else {
					output_Makiyama.write(i + ",");
				}
			}

			for (int i = 0; i < treeList.size(); ++i) {
				for (int j = 0; j < treeList.size(); ++j) {
					if (j == treeList.size() - 1) {
						output_Makiyama.write(matrix_Makiyama[i][j] + "\n");
					} else {
						output_Makiyama.write(matrix_Makiyama[i][j] + ",");
					}
				}
			}
			output_Makiyama.close();
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method creates distance matrices for hierarchical clustering
	 * 
	 * @param appName
	 *            (MediaStorage, Facebook, GooglePlus, Hangouts)
	 */
	public void createDistanceMatrixForAll(String appName) {
		// TODO Auto-generated method stub
		Global.tableAlias = new HashMap<String, String>();
		try {

			// ArrayList<Statement> queryList = new ArrayList<>();
			BufferedWriter output_MakiyamaFeature = new BufferedWriter(new FileWriter(
					new File(workingDirectory + "/data/" +  appName + "/makiyamaFeature.csv")));
			BufferedWriter output_MakiyamaQuery = new BufferedWriter(new FileWriter(
					new File(workingDirectory + "/data/" +  appName + "/makiyamaQuery.csv")));
			// BufferedWriter output_MakiyamaQueryDist = new BufferedWriter(new
			// FileWriter(new
			// File("/Users/gokhanku/Documents/Projects/PocketData/" + appName +
			// "/makiyamaQueryDist.csv")));
			BufferedWriter output_Makiyama = new BufferedWriter(new FileWriter(
					new File(workingDirectory + "/data/" +  appName + "/dist_Makiyama.csv")));
			int count = 0;
			int countParsable = 0;

			int userSize = getUserSize(appName);

			HashSet<String> featureList = new HashSet<String>();
			HashMap<String, ArrayList<Integer>> uniqueStrings = new HashMap<String, ArrayList<Integer>>();
			HashMap<String, TreeMap<String, Integer>> vectorList = new HashMap<String, TreeMap<String, Integer>>();
			for (int user = 0; user < userSize; user++) {

				String fileName = workingDirectory + "/data/" +  appName + "/user" + user + ".csv";
				System.out.println(fileName);
				BufferedReader input = new BufferedReader(new FileReader(new File(fileName)));
				String line = input.readLine();
				Statement stmt;
				TreeMap<String, Integer> vector;

				try {
					while (line != null) {
						stmt = null;
						count++;
						String[] arr = line.split("\t");
						if (arr[4].contains("SQLiteProgram: ")) {
							arr[4] = arr[4].replace("SQLiteProgram: ", "");
						}
						// System.out.println(arr[1]);
						InputStream stream = new ByteArrayInputStream(arr[4].getBytes(StandardCharsets.UTF_8));
						try {
							CCJSqlParser parser = new CCJSqlParser(stream);
							stmt = parser.Statement();
							// queryList.add(stmt);
							if (stmt instanceof Select) {
								ArrayList<Integer> responseTime = null;
								if (uniqueStrings.containsKey(arr[4])) {
									responseTime = uniqueStrings.get(arr[4]);
								} else {
									responseTime = new ArrayList<Integer>();
								}
								responseTime.add(Integer.parseInt(arr[7]));
								uniqueStrings.put(arr[4], responseTime);
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
				// System.out.println("Total lines: " + count);
				System.out.println("Total parsable select queries: " +
				  countParsable);
				System.out.println("Unique strings: " +
				 uniqueStrings.size());

				Iterator<String> it = uniqueStrings.keySet().iterator();
				int counter = 1;
				while (it.hasNext()) {
					String temp = it.next();
					InputStream stream = new ByteArrayInputStream(temp.getBytes(StandardCharsets.UTF_8));
					CCJSqlParser parser = new CCJSqlParser(stream);
					stmt = parser.Statement();
					// output_MakiyamaQuery.write(counter + ", " + temp + "\n");
					counter++;
					vector = Makiyama.getQueryVector(stmt);
					featureList.addAll(vector.keySet());
					vectorList.put(temp, vector);
				}
				// output_MakiyamaQuery.close();
			}

			Iterator<String> it = uniqueStrings.keySet().iterator();
			int counter = 1;
			ArrayList<Integer> responseTime = null;
			double averageResponseTime = 0;
			double standardDeviationOfResponseTime = 0;
			while (it.hasNext()) {
				String temp = it.next();
				responseTime = uniqueStrings.get(temp);
				averageResponseTime = computeAverageTime(responseTime);
				standardDeviationOfResponseTime = standardDeviation(responseTime);
				output_MakiyamaQuery.write(counter + ", " + temp + ", " + responseTime.size() + ", "
						+ averageResponseTime + ", " + standardDeviationOfResponseTime + "\n");
				counter++;
			}
			output_MakiyamaQuery.close();

			ArrayList<String> features = new ArrayList<String>();
			features.addAll(featureList);
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < features.size(); i++) {
				builder.append(features.get(i) + ",");
			}
			builder.append("Class");
			output_MakiyamaFeature.write(builder.toString() + "\n");

			// System.out.println("Headers are printed.");

			Iterator<String> it2 = uniqueStrings.keySet().iterator();
			
			while (it2.hasNext()) {
				String temp = it2.next();
				
				builder = new StringBuilder();
				TreeMap<String, Integer> entry = vectorList.get(temp);
				for (int i = 0; i < features.size(); i++) {
					if (entry.containsKey(features.get(i))) {
						builder.append(entry.get(features.get(i)) + ",");
					} else {
						builder.append("0,");
					}
				}
				builder.deleteCharAt(builder.length() - 1);

				output_MakiyamaFeature.write(builder.toString() + "\n");
			}

			// System.out.println("Vectors are printed.");

			output_MakiyamaFeature.close();

			matrix_Makiyama = new double[uniqueStrings.size()][uniqueStrings.size()];

			ArrayList<String> stringList = new ArrayList<String>();
			stringList.addAll(uniqueStrings.keySet());
			ArrayList<TreeMap<String, Integer>> treeList = new ArrayList<TreeMap<String, Integer>>();
			Statement stmt = null;
			for (int i = 0; i < stringList.size(); ++i) {
				matrix_Makiyama[i][i] = 0;
				// output_MakiyamaQueryDist.write(i + "," + stringList.get(i) +
				// "\n");
				InputStream stream = new ByteArrayInputStream(stringList.get(i).getBytes(StandardCharsets.UTF_8));
				CCJSqlParser parser = new CCJSqlParser(stream);
				stmt = parser.Statement();
				treeList.add(Makiyama.getQueryVector(stmt));
			}
			// output_MakiyamaQueryDist.close();
			for (int i = 0; i < treeList.size() - 1; ++i) {
				for (int j = i + 1; j < treeList.size(); ++j) {
					// System.out.println(i + "," + j);
					matrix_Makiyama[i][j] = 1.0 - Makiyama.getDistanceAsRatio(treeList.get(i), treeList.get(j));
					matrix_Makiyama[j][i] = matrix_Makiyama[i][j];
				}

			}

			for (int i = 0; i < treeList.size(); ++i) {
				if (i == treeList.size() - 1) {
					output_Makiyama.write(i + "\n");
				} else {
					output_Makiyama.write(i + ",");
				}
			}

			for (int i = 0; i < treeList.size(); ++i) {
				for (int j = 0; j < treeList.size(); ++j) {
					if (j == treeList.size() - 1) {
						output_Makiyama.write(matrix_Makiyama[i][j] + "\n");
					} else {
						output_Makiyama.write(matrix_Makiyama[i][j] + ",");
					}
				}
			}
			output_Makiyama.close();
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method computes the average response time of a given query list
	 * 
	 * @param responseTime
	 * @return average response time
	 */
	private double computeAverageTime(ArrayList<Integer> responseTime) {
		if (responseTime != null && responseTime.size() > 0) {
			double sum = 0;
			for (int i = 0; i < responseTime.size(); i++) {
				sum += responseTime.get(i);
			}
			return sum / responseTime.size();
		} else {
			return Double.MAX_VALUE;
		}
	}

	/**
	 * This method returns the standard deviation of the given list
	 * 
	 * @param responseTime
	 * @return standard deviation of response time
	 */
	private double standardDeviation(ArrayList<Integer> responseTime) {
		// Step 1:
		double mean = computeAverageTime(responseTime);
		double temp = 0;

		for (int i = 0; i < responseTime.size(); i++) {
			int val = responseTime.get(i);

			// Step 2:
			double squrDiffToMean = Math.pow(val - mean, 2);

			// Step 3:
			temp += squrDiffToMean;
		}

		// Step 4:
		double meanOfDiffs = (double) temp / (double) (responseTime.size());

		// Step 5:
		return Math.sqrt(meanOfDiffs);
	}

}
