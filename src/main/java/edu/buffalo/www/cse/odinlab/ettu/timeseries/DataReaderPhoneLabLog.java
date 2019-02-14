package edu.buffalo.www.cse.odinlab.ettu.timeseries;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.junit.experimental.theories.Theories;

import edu.buffalo.www.cse.odinlab.PocketBench.PairwiseDistance;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.ParsabilityStatus;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.StatementWithStatus;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.StringWithStatus;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.Util;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import querySimilarityMetrics.Makiyama;
import toolsForMetrics.Global;

public class DataReaderPhoneLabLog implements DataReader {

	private String dataFolder = "";
	private int userSize = -1;
	private List<String> userFiles = null;
	private List<Statement> uniqueStatements = null;
	private boolean uniqueQueriesFileExists = false;
	private Long startTime = null;
	private Long endTime = null;
	private HashMap<String, TreeMap<String, Integer>> queryWithFeatureList = null;
	private HashMap<String, int[]> queryWithVector = null;
	private HashSet<String> features = null;
	
	public DataReaderPhoneLabLog(String dataFolder) {
		this.dataFolder = dataFolder;
		initialize(dataFolder);
	}
	
	public DataReaderPhoneLabLog(String dataFolder, String startTime, String endTime) {
		this.dataFolder = dataFolder;
		Global.tableAlias = new HashMap<>();
		initialize(dataFolder);
		this.startTime = Util.parseFromDateStringToLong(startTime);
		this.endTime = Util.parseFromDateStringToLong(endTime);
		this.uniqueStatements = extractUniqueQueryStrings();
		this.features = new HashSet<String>();
		extractFeatures();
		createFeatureVectors();
		writeFeatureVectors();
		
	}
	
	private String getStringFromIntegerVector(int[] vector) {
		
		if (vector == null)
			return null;
		
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < vector.length - 1; i++) {
			builder.append(vector[i] + ",");
		}
		
		builder.append(vector[vector.length - 1]);
		
		return builder.toString();
	}
	
	private void createFeatureVectors() {
		
		int counter = 0;
		
		Iterator<String> iteratorFeature = features.iterator();
		String featureName = null;
		while(iteratorFeature.hasNext()) {
			featureName = iteratorFeature.next();
			
			Iterator<String> iteratorQuery = queryWithVector.keySet().iterator();
			String query = null;
			int[] tempVector = null;
			Integer tempFeatureCount = null;
			while(iteratorQuery.hasNext()) {
				query = iteratorQuery.next();
				tempVector = queryWithVector.get(query);
				
				if (queryWithFeatureList.get(query) != null)
					tempFeatureCount = queryWithFeatureList.get(query).get(featureName);
				else 
					tempFeatureCount = null;
				
				if (tempFeatureCount != null) 
					tempVector[counter] = tempFeatureCount;
				else 
					tempVector[counter] = 0;
				
				queryWithVector.put(query, tempVector);
			}
			
			counter++;
			
		}
		
	}

	private void extractFeatures() {
		
		Iterator<String> iterator1 = queryWithFeatureList.keySet().iterator();
		
		String tempString = null;
		TreeMap<String, Integer> tempFeatureList = null;
		while(iterator1.hasNext()) {
			tempString = iterator1.next();
			tempFeatureList = Makiyama.getQueryVector(Util.convertFromString(tempString));
			
			queryWithFeatureList.put(tempString, tempFeatureList);
			if (tempFeatureList != null)
				features.addAll(tempFeatureList.keySet());
		}
		
		Iterator<String> iterator2 = queryWithFeatureList.keySet().iterator();
		queryWithVector = new HashMap<String, int[]>();
		
		String tempString2 = null;
		while(iterator2.hasNext()) {
			tempString2 = iterator2.next();
			
			queryWithVector.put(tempString2, new int[features.size()]);
		}
		
	}

	/**
	 * Finds how many files in the given folder starts with 'user',
	 * records them, appoints the user size
	 */
	public void initialize(String dataFolder) {
		this.userFiles = findUserFiles(dataFolder);
		this.userSize = this.userFiles.size();
		System.out.println("There are " + this.userSize + " user information for this app.");
	}
	
	/**
	 * This method returns full paths of all user files
	 * @param appName
	 * @return
	 */
	public List<String> findUserFiles(String dataFolder) {
		List<String> results = new ArrayList<String>();
		File[] files = new File(dataFolder).listFiles();
		// If this pathname does not denote a directory, then listFiles()
		// returns null.

		for (File file : files) {
			if (file.isFile()
					&& (file.getName().startsWith("User") || file.getName().startsWith("user")
					&& !(file.getName().startsWith("user15")))) {
				results.add(file.getAbsolutePath());
			}
		}

		return results;
	}
	
	/**
	 * Returns the user files
	 */
	public List<String> getUserFiles() {
		return this.userFiles;
	}
	
	/**
	 * Reads the queries from the users files and prepares them to be clustered
	 */
	public List<Statement> extractUniqueQueries() {
		
		Set<StringWithStatus> intermediate = new HashSet<StringWithStatus>();
		
		File tmpFile = new File(this.dataFolder + File.separator + "uniqueQueries.csv");
		uniqueQueriesFileExists = tmpFile.exists();
		if (uniqueQueriesFileExists) {
			System.out.println(tmpFile.getName() + " exists");
			
			try {
				FileInputStream fis = new FileInputStream(tmpFile);
				InputStreamReader reader = new InputStreamReader(fis);
				BufferedReader in = new BufferedReader(reader);
				
				String readLine;
				String[] columns = null;
				String query = "";
				StringWithStatus queryString = null;
				
				
				while ((readLine = in.readLine()) != null) {

					queryString = Util.getTypeAndString(readLine);
					if (queryString.getStatus() == ParsabilityStatus.PARSABLE_CRUD_QUERY) {
						intermediate.add(queryString);
					}
				}
				
				System.out.println("There are " + intermediate.size() + " unique query strings for this app." );
				
				in.close();
				reader.close();
				fis.close();
				
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
			}
		
		} else {
		
			for (int i = 0; i < userFiles.size(); i++) {
				try {
					System.out.print("File being read is " + userFiles.get(i));
					FileInputStream fis = new FileInputStream(userFiles.get(i));
					GZIPInputStream gzis = new GZIPInputStream(fis);
					InputStreamReader reader = new InputStreamReader(gzis);
					BufferedReader in = new BufferedReader(reader);
					
					String readLine;
					String[] columns = null;
					String query = "";
					StringWithStatus queryString = null;
					
					
					while ((readLine = in.readLine()) != null) {
						columns = readLine.split("\t");
						
						query = columns[4];
						
						queryString = Util.getTypeAndString(query);
						if (queryString.getStatus() == ParsabilityStatus.PARSABLE_CRUD_QUERY) {
							intermediate.add(queryString);
						}
					}
					
					System.out.print("There are " + intermediate.size() + " unique query strings until this user. " + (userFiles.size() - 1 - i) + " files to go. \n" );
					
					in.close();
					reader.close();
					gzis.close();
					fis.close();
				} catch (Exception ex) {
					ex.printStackTrace();
					System.err.println(ex.getMessage());
				}
				
			}
		}
		System.gc();
		
		Statement temp = null;
		List<Statement> retVal = new ArrayList<Statement>();
		Iterator<StringWithStatus> it = intermediate.iterator();
		while(it.hasNext()) {
			temp = Util.convertFromStringWithStatus(it.next());
			if (!Util.checkIfContainsStatement(retVal, temp)) {
				retVal.add(temp);
			}
		}
		
		System.out.println("There are " + retVal.size() + " unique statements for this app.");
		
		return retVal;	
	}
	

	/**
	 * Reads the queries from the users files and prepares them to be clustered
	 */
	public List<Statement> extractUniqueQueryStrings() {
		
		queryWithFeatureList = new HashMap<String, TreeMap<String, Integer>>();
		
		File tmpFile = new File(this.dataFolder + File.separator + "uniqueQueries.csv");
		uniqueQueriesFileExists = tmpFile.exists();
		if (uniqueQueriesFileExists) {
			System.out.println(tmpFile.getName() + " already exists");
			
			try {
				FileInputStream fis = new FileInputStream(tmpFile);
				InputStreamReader reader = new InputStreamReader(fis);
				BufferedReader in = new BufferedReader(reader);
				
				String readLine;
				String[] columns = null;
				String query = "";
				StringWithStatus queryString = null;
				
				
				while ((readLine = in.readLine()) != null) {
					columns = readLine.split("\t");
					
					queryWithFeatureList.put(columns[1], null);
				}
				
				System.out.println("There are " + queryWithFeatureList.size() + " unique query strings for this app." );
				
				in.close();
				reader.close();
				fis.close();
				
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
			}
		
		} else {
		
			for (int i = 0; i < userFiles.size(); i++) {
				try {
					System.out.print("File being read is " + userFiles.get(i));
					FileInputStream fis = new FileInputStream(userFiles.get(i));
					GZIPInputStream gzis = new GZIPInputStream(fis);
					InputStreamReader reader = new InputStreamReader(gzis);
					BufferedReader in = new BufferedReader(reader);
					
					String readLine;
					String[] columns = null;
					String query = "";
					StringWithStatus queryString = null;
					Long timeValue = null;
					
					if (startTime != null && endTime != null) {
					
						while ((readLine = in.readLine()) != null) {
							columns = readLine.split("\t");
							
							timeValue = Util.parseFromDateStringToLong(columns[2]);
							
							if (timeValue > startTime && timeValue < endTime)
								queryWithFeatureList.put(columns[4], null);
							
						}
					} else {
						while ((readLine = in.readLine()) != null) {
							columns = readLine.split("\t");
							
							queryWithFeatureList.put(columns[4], null);
						}
					}
					
					System.out.println(" There are " + queryWithFeatureList.size() + " unique query strings until this user. " + (userFiles.size() - 1 - i) + " files to go. \n" );
					
					in.close();
					reader.close();
					gzis.close();
					fis.close();
				} catch (Exception ex) {
					ex.printStackTrace();
					System.err.println(ex.getMessage());
				}
				
			}
		}
		
		Statement temp = null;
		List<Statement> retVal = new ArrayList<Statement>();
		Iterator<String> it = queryWithFeatureList.keySet().iterator();
		while(it.hasNext()) {
			temp = Util.convertFromString(it.next());
			retVal.add(temp);
		}
		
		System.out.println("There are " + retVal.size() + " unique statements for this app.");
		
		return retVal;	
	}
	
	/**
	 * Writes the queries from the users files with feature vectors
	 */
	public void writeFeatureVectors() {
		
		queryWithFeatureList = new HashMap<String, TreeMap<String, Integer>>();
		
		for (int i = 0; i < userFiles.size(); i++) {
			try {
				System.out.print("File being read is " + userFiles.get(i));
				FileInputStream fis = new FileInputStream(userFiles.get(i));
				GZIPInputStream gzis = new GZIPInputStream(fis);
				InputStreamReader reader = new InputStreamReader(gzis);
				BufferedReader in = new BufferedReader(reader);
				
				String readLine;
				String[] columns = null;
				String query = "";
				StringWithStatus queryString = null;
				Long timeValue = null;
				
				if (startTime != null && endTime != null) {
					
					try  {
						BufferedWriter output_queryVectors = null;
					
						String lineToBeWritten = null;
						while ((readLine = in.readLine()) != null) {
							columns = readLine.split("\t");
							
							//timeValue = Long.parseLong(columns[2]);
							timeValue = Util.parseFromDateStringToLong(columns[3]);
							
							if (timeValue > startTime && timeValue < endTime) {
								
								if (output_queryVectors == null)
									output_queryVectors = new BufferedWriter(new FileWriter(new File(
											this.dataFolder + File.separator + i + ".csv")));
								
								if (queryWithVector.get(columns[4]) != null) {
									lineToBeWritten = timeValue + "," + getStringFromIntegerVector(queryWithVector.get(columns[4])) + "\n";
									output_queryVectors.write(lineToBeWritten); 
								}
								
								
							}
						}
						
						output_queryVectors.close();
						
					} catch (Exception ex) {
						System.err.println(ex.getMessage());
					}
				} else {
//					while ((readLine = in.readLine()) != null) {
//						columns = readLine.split("\t");
//						
//						queryWithFeatureList.put(columns[4], null);
//					}
				}
				
				
				in.close();
				reader.close();
				gzis.close();
				fis.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println(ex.getMessage());
			}
			
		}
		
		
	}
	
}
