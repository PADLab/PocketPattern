package edu.buffalo.www.cse.odinlab.PocketBench;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation.UniqueQuery;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.ParsabilityStatus;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.StatementWithStatus;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.Util;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.*;

public class SequenceMerger {
	
	/**
	 * This method recreates the log with the cluster appointments of each query.
	 * WARNING: This recreated log only includes parsable select queries
	 * @param workingObject
	 * @param appName
	 * @throws IOException
	 */
	public static void recreateTheLog(PairwiseDistance workingObject, String appName) throws IOException {
		String uniqueQueriesFile = workingObject.getDataDirectory() + appName + "/makiyamaQuery.csv";
		String clusterAppointmentsFile = workingObject.getDataDirectory() + appName
				+ "/makiyama_clustered.txt";

		BufferedWriter output_FinalResult = new BufferedWriter(new FileWriter(
				new File(workingObject.getDataDirectory() + appName + "/Clustering.csv")));

		BufferedReader inputUniqueQueries = null;
		BufferedReader inputClusterAppointments = null;

		HashMap<String, Integer> mapQueryAppointment = new HashMap<String, Integer>();

		try {
			inputUniqueQueries = new BufferedReader(new FileReader(new File(uniqueQueriesFile)));
			inputClusterAppointments = new BufferedReader(new FileReader(new File(clusterAppointmentsFile)));

			String line = inputUniqueQueries.readLine();
			String appointment = inputClusterAppointments.readLine();

			while (line != null && appointment != null) {
				
				//Throwing out first column
				line = line.substring(line.indexOf(",") + 2);
				//Throwing out last three columns
				line = line.substring(0, line.lastIndexOf(","));
				line = line.substring(0, line.lastIndexOf(","));
				line = line.substring(0, line.lastIndexOf(","));
				
				System.out.println(line);
				
				mapQueryAppointment.put(line, Integer.parseInt(appointment));

				line = inputUniqueQueries.readLine();
				appointment = inputClusterAppointments.readLine();
			}

			inputClusterAppointments.close();
			inputUniqueQueries.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		output_FinalResult.write("Query~Timestamp~Cluster~UserID~RunningTime" + "\n");

		int userSize = workingObject.getUserSize(appName);

		for (int user = 0; user < userSize; user++) {
			String fileName = workingObject.getDataDirectory() + appName + "/user" + user + ".csv";
			System.out.println(fileName);
			BufferedReader input = new BufferedReader(new FileReader(new File(fileName)));
			String line = input.readLine();

			String temp = null;
			while (line != null) {
				String[] arr = line.split("\t");

				// arr[4] -> Query
				// arr[2] -> Unix Timestamp
				// arr[7] -> ResponseTime
				try {
					if (arr[4].contains("SQLiteProgram: ")) {
						arr[4] = arr[4].replace("SQLiteProgram: ", "");
					}

					if (mapQueryAppointment.get(arr[4]) != null) {
						temp = arr[4] + "~" + arr[2] + "~" + mapQueryAppointment.get(arr[4]) + "~" + user + "~"
								+ arr[7];
						System.out.println(temp);
						output_FinalResult.write(temp + "\n");
					}
				} catch (Exception ex) {
					System.out.println(line);
				}

				line = input.readLine();
			}

			input.close();
		}

		output_FinalResult.close();

	}
	
	/**
	 * This is an adapter method to pass data to SessionSplitter class.
	 * WARNING: This collection only includes parsable select queries
	 * @param workingObject
	 * @param appName
	 * @throws IOException
	 */
	public static List<DataRow> exportForSessionSplitter(PairwiseDistance workingObject, String appName){
		String uniqueQueriesFile = workingObject.getDataDirectory() + appName + "/makiyamaQuery.csv";
		String clusterAppointmentsFile = workingObject.getDataDirectory() + appName
				+ "/makiyama_clustered.txt";
		List<DataRow> clusteringOutput = null;
		try(BufferedWriter output_FinalResult = new BufferedWriter(new FileWriter(new File(workingObject.getDataDirectory() + appName + "/Clustering.csv"))))
		{
			HashMap<String, Integer> mapQueryAppointment = new HashMap<String, Integer>();
			try(BufferedReader inputUniqueQueries = new BufferedReader(new FileReader(new File(uniqueQueriesFile))); BufferedReader inputClusterAppointments = new BufferedReader(new FileReader(new File(clusterAppointmentsFile)))) {
				String line = null, appointment = null;
				while ((line = inputUniqueQueries.readLine()) != null && (appointment = inputClusterAppointments.readLine()) != null) {										
					line = line.substring(line.indexOf(",") + 2); //Throwing out first column					
					line = line.substring(0, line.lastIndexOf(",")); //Throwing out last three columns
					line = line.substring(0, line.lastIndexOf(","));
					line = line.substring(0, line.lastIndexOf(","));					
					System.out.println(line);					
					mapQueryAppointment.put(line, Integer.parseInt(appointment));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
			clusteringOutput = new ArrayList<>();			
			int userSize = workingObject.getUserSize(appName);
			for (int user = 0; user < userSize; user++) {
				String fileName = workingObject.getDataDirectory() + appName + "/user" + user + ".csv";
				System.out.println(fileName);
				
				try(BufferedReader input = new BufferedReader(new FileReader(new File(fileName)))){
					String line = null, temp = null;
					while ((line = input.readLine()) != null) {
						String[] arr = line.split("\t"); // arr[4] -> Query, arr[2] -> Unix Timestamp, arr[7] -> ResponseTime
						try {
							if (arr[4].contains("SQLiteProgram: ")) 
								arr[4] = arr[4].replace("SQLiteProgram: ", "");							
							if (mapQueryAppointment.get(arr[4]) != null) {
//								temp = arr[4] + "~" + arr[2] + "~" + mapQueryAppointment.get(arr[4]) + "~" + user + "~"+ arr[7];
								clusteringOutput.add(new DataRow(arr[4], Long.parseLong(arr[2]), mapQueryAppointment.get(arr[4]), String.valueOf(user), Long.parseLong(arr[7])));
							}
						} catch (Exception ex) {
							System.out.println("ExportError in "+line);
						}
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return clusteringOutput;		
	}

	public static List<DataRow> exportForSessionSplitter(String dataFolder, String userFile, Map<String, UniqueQuery> uniqueQueries) {
		
		List<DataRow> retVal = new ArrayList<DataRow>();
		
		try {
			FileInputStream fis = new FileInputStream(userFile);
			GZIPInputStream gzis = new GZIPInputStream(fis);
			InputStreamReader reader = new InputStreamReader(gzis);
			BufferedReader in = new BufferedReader(reader);
			
			String readLine;
			String columns[] = null;
			DataRow tempRow = null;
			String query = null;
			
			while ((readLine = in.readLine()) != null) {
				columns = readLine.split("\t");
				
				StatementWithStatus tempObj = Util.getTypeAndStatement(columns[4]);
				
				if (tempObj.getStatus() == ParsabilityStatus.PARSABLE_CRUD_QUERY) {
					
					query = tempObj.getStatement().toString();
					UniqueQuery tempQuery = uniqueQueries.get(query);
					
					if (tempQuery != null) {
						tempRow = new DataRow(query, Long.parseLong(columns[2]), tempQuery.getClusteringAppointment(), userFile, null, null);
					} else {
						//We appoint -2 for queries that can be parsed but somehow couldn't be clustered, assuming the number of these types of
						//queries won't hold any significance to change the result
						tempRow = new DataRow(query, Long.parseLong(columns[2]), -2, userFile, null, null);
					}
				} else {
					// We appoint -1 for queries that cannot be parsed into a CRUD query
					tempRow = new DataRow(query, Long.parseLong(columns[2]), -1, userFile, null, null);
				}
				
				retVal.add(tempRow);
				
			}
			
			in.close();
			reader.close();
			gzis.close();
			fis.close();
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return retVal;
	}
	
}
