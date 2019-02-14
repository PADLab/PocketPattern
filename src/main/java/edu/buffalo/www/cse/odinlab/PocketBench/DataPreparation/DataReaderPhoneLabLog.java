package edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation;

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
	private Map<String, UniqueQuery> uniqueQueries = null;
	private boolean uniqueQueriesFileExists = false;
	private Long startTime = null;
	private Long endTime = null;
	
	public DataReaderPhoneLabLog(String dataFolder) {
		this.dataFolder = dataFolder;
		initialize(dataFolder);
		createDistanceMatrix();
		performQueryClustering();
		this.uniqueQueries = getUniqueQueryClusters();
	}
	
	public DataReaderPhoneLabLog(String dataFolder, String startTime, String endTime) {
		this.dataFolder = dataFolder;
		initialize(dataFolder);
		createDistanceMatrix();
		performQueryClustering();
		this.uniqueQueries = getUniqueQueryClusters();
		this.startTime = Util.parseFromDateStringToLong(startTime);
		this.endTime = Util.parseFromDateStringToLong(endTime);
	}
	
	/**
	 * Gets all the unique parsable queries encountered
	 */
	public Map<String, UniqueQuery> getUniqueQueries() {
		return this.uniqueQueries;
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
		
		Set<String> intermediate = new HashSet<String>();
		
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
					
					intermediate.add(columns[1]);
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
					Long timeValue = null;
					
					if (startTime != null && endTime != null) {
					
						while ((readLine = in.readLine()) != null) {
							columns = readLine.split("\t");
							
							timeValue = Util.parseFromDateStringToLong(columns[2]);
							
							if (timeValue > startTime && timeValue < endTime)
								intermediate.add(columns[4]);
							
						}
					} else {
						while ((readLine = in.readLine()) != null) {
							columns = readLine.split("\t");
							
							intermediate.add(columns[4]);
						}
					}
					
					System.out.print(" There are " + intermediate.size() + " unique query strings until this user. " + (userFiles.size() - 1 - i) + " files to go. \n" );
					
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
		Iterator<String> it = intermediate.iterator();
		while(it.hasNext()) {
			temp = Util.convertFromString(it.next());
			retVal.add(temp);
		}
		
		System.out.println("There are " + retVal.size() + " unique statements for this app.");
		
		return retVal;	
	}

	/**
	 * Creates the distance metric between unique queries
	 * Writes the unique queries to a file (uniqueQueries.csv)
	 * Writes the distance matrix to a comma separated file (distanceMatrix.csv)
	 */
	public double[][] createDistanceMatrix() {
		
		Global.tableAlias = new HashMap<String, String>();
		
		this.uniqueStatements = extractUniqueQueryStrings();
		double[][] matrix_Makiyama = new double[uniqueStatements.size()][uniqueStatements.size()];
		
		ArrayList<TreeMap<String, Integer>> treeList = new ArrayList<TreeMap<String, Integer>>();
		
		try {
			if (!uniqueQueriesFileExists) {
				BufferedWriter output_uniqueQueries = new BufferedWriter(new FileWriter(new File(
						this.dataFolder + File.separator + "uniqueQueries.csv")));
				
				long lStartTime = System.nanoTime();
				
				for (int i = 0; i < uniqueStatements.size(); ++i) {
					matrix_Makiyama[i][i] = 0;
					treeList.add(Makiyama.getQueryVector(uniqueStatements.get(i)));
					output_uniqueQueries.write(i + "\t" + uniqueStatements.get(i).toString() + "\n");
				}
				
				long lEndTime = System.nanoTime();
				long output = lEndTime - lStartTime;
				System.out.println("Feature extraction for " + uniqueStatements.size() + " queries including writing to file- Elapsed time in seconds: " + (1.0 * output / 1000000000));
				
				output_uniqueQueries.close();
			} else {
				long lStartTime = System.nanoTime();
				
				for (int i = 0; i < uniqueStatements.size(); ++i) {
					matrix_Makiyama[i][i] = 0;
					treeList.add(Makiyama.getQueryVector(uniqueStatements.get(i)));
				}
				
				long lEndTime = System.nanoTime();
				long output = lEndTime - lStartTime;
				System.out.println("Feature extraction for " + uniqueStatements.size() + " queries - Elapsed time in seconds: " + (1.0 * output / 1000000000));
				
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println(e1.getMessage());
		}
		
		System.gc();
		
		long lStartTime = System.nanoTime();
		
		for (int i = 0; i < treeList.size() - 1; ++i) {
			for (int j = i + 1; j < treeList.size(); ++j) {
				// System.out.println(i + "," + j);
				matrix_Makiyama[i][j] = 1.0 - Makiyama.getDistanceAsRatio(treeList.get(i), treeList.get(j));
				matrix_Makiyama[j][i] = matrix_Makiyama[i][j];
			}

		}
		
		long lEndTime = System.nanoTime();
		long output = lEndTime - lStartTime;
		System.out.println("Distance matrix preparation for " + uniqueStatements.size() + " queries - Elapsed time in seconds: " + (1.0 * output / 1000000000));
		
		try {
			File tmpFile = new File(this.dataFolder + File.separator + "distanceMatrix.csv");
			boolean distanceMatrixExists = tmpFile.exists();
			
			if (!distanceMatrixExists) {
				BufferedWriter output_Makiyama = new BufferedWriter(new FileWriter(new File(
						this.dataFolder + File.separator + "distanceMatrix.csv")));
				
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
			} else {
				System.err.println("Distance matrix already created. Not writing to file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		
		return matrix_Makiyama;
	}
	
	
	/**
	 * Clusters the unique queries using R
	 */
	public void performQueryClustering() {
		
		long lStartTime = System.nanoTime();
		
		String[] cmdArray = new String[4];
		
		cmdArray[0] = "Rscript";
		cmdArray[1] = "--vanilla";
		cmdArray[2] = this.dataFolder + File.separator + "clusterAll.R";
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
		System.out.println("Clustering time for " + uniqueStatements.size() + " queries - Elapsed time in seconds: " + (1.0 * output / 1000000000));
		
	}
	
	
	/**
	 * Matches the cluster appointments with the queries
	 */
	public Map<String, UniqueQuery> getUniqueQueryClusters() {
		String clusterAppointmentsFile = this.dataFolder + File.separator + "clustersOrdered.txt";

		BufferedReader inputClusterAppointments = null;
		
		Map<String, UniqueQuery> uniqueQueries = new HashMap<String, UniqueQuery>();

		try {
			inputClusterAppointments = new BufferedReader(new FileReader(new File(clusterAppointmentsFile)));

			String appointment = inputClusterAppointments.readLine();
			int counter = 0;

			while (appointment != null) {
				uniqueQueries.put(this.uniqueStatements.get(counter).toString(), new UniqueQuery(counter, this.uniqueStatements.get(counter), Integer.parseInt(appointment)));
				
				counter++;
				appointment = inputClusterAppointments.readLine();
			}

			inputClusterAppointments.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		
		return uniqueQueries;

	}

}
