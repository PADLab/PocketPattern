package edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.jayway.jsonpath.JsonPath;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.JSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

public class Analyze {

	public static void main(String[] args) {
		
		findDatabaseAccessCounts();
	}
	
	private static void findDatabaseAccessCounts() {
		System.out.println("Enter the target folder (where you need to write the data)");
		String targetFile = "/home/gokhanku/eclipse-workspace/data/user0.csv.gz";
		
		Map<String, Integer> dbCount = null;
		
		try {
			FileInputStream fis = new FileInputStream(targetFile);
			GZIPInputStream gzis = new GZIPInputStream(fis);
			InputStreamReader reader = new InputStreamReader(gzis);
			BufferedReader in = new BufferedReader(reader);
			
			String readLine;
			int recordCounter = 0;
			int queryCounter = 0;
			int noDBCounter = 0;
			int unparsableQueryCounter = 0;
			int someOtherProblem = 0;
			int pragmaCounter = 0;
			int beginAndCommitCounter = 0;
			int maintenanceQueryCounter = 0;
			int schemaAlteration = 0;
			int parsedQueryCount = 0;
			String[] columns = null;
			String dbName = "";
			String query = "";
			String[] dbQueryPair = null;
			dbCount = new TreeMap<String, Integer>();
			InputStream stream = null;
			CCJSqlParser parser = null;
			Statement statement = null;
			String lowerCaseQuery = "";
			
			//facebook -> com.facebook.orca and com.facebook.katana
			//hangouts -> com.google.android.talk
			
			while ((readLine = in.readLine()) != null) {
				columns = readLine.split("\t");
				
				dbQueryPair = processJSON(columns[columns.length - 1]);
				dbName = dbQueryPair[0];
				query = dbQueryPair[1];
				
				if (dbName.equals("")) {
					noDBCounter++;
				} else {
					if (dbCount.containsKey(dbName)) {
						dbCount.put(dbName, dbCount.get(dbName) + 1);
					} else {
						dbCount.put(dbName, 1);
					}
				}
				
				if (!query.equals("")) {
					query = fixQuery(query);
					
					lowerCaseQuery = query.toLowerCase();
					
					if (lowerCaseQuery.startsWith("pragma")) {
						pragmaCounter++;
					} else if (lowerCaseQuery.startsWith("begin")
							|| lowerCaseQuery.startsWith("commit")
							|| lowerCaseQuery.startsWith("abort")
							|| lowerCaseQuery.startsWith("rollback")
							|| lowerCaseQuery.startsWith(";")) {
						beginAndCommitCounter++;
					} else if (lowerCaseQuery.startsWith("create trigger")
							|| lowerCaseQuery.startsWith("create index")
							|| lowerCaseQuery.startsWith("attach database")
							|| lowerCaseQuery.startsWith("analyze")
							|| lowerCaseQuery.startsWith("reindex")) {
						maintenanceQueryCounter++;
					} else if (lowerCaseQuery.startsWith("alter table")) {
						schemaAlteration++;
					} else {
						try {
							stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
							parser = new CCJSqlParser(stream);
							statement = parser.Statement();
							
							parsedQueryCount++;
							
							//System.out.println("An example of accepted query: " + query);
						} catch (ParseException e) {			    
							unparsableQueryCounter++;
							//System.out.println("An example of unparsable query: " + query + " : " + e.getMessage());
							//System.out.println("An example of unparsable query: " + query);
						} catch(Exception e){
							someOtherProblem++;
							System.out.println("Some other problem: " + query);
						} catch(Error e){
							someOtherProblem++;
							System.out.println("The reason JSqlParser crashed is: " + query);
						}
					}
					
					queryCounter++;
					
				}
				
				//readLine = in.readLine();
				
				recordCounter++;
			}
			
			in.close();
			reader.close();
			gzis.close();
			fis.close();
			
			System.out.println("There are " + noDBCounter + " lines where the JSON is unparsable in " + recordCounter + " records.");
			System.out.println("There are " + queryCounter + " queries in " + (recordCounter - noDBCounter) + " parsable JSON records.");
			System.out.println(unparsableQueryCounter + " queries couldn't be parsed in " + queryCounter + " queries.");
			System.out.println("There are " + parsedQueryCount + " parsed queries in " + queryCounter + " queries.");
			System.out.println("There are also " + someOtherProblem + " other problems in " + queryCounter + " queries.");
			System.out.println("There are also " + pragmaCounter + " pragma commands in " + queryCounter + " queries.");
			System.out.println("There are also " + beginAndCommitCounter + " transaction commands in " + queryCounter + " queries.");
			System.out.println("There are also " + maintenanceQueryCounter + " maintenance commands in " + queryCounter + " queries.");
			System.out.println("There are also " + schemaAlteration + " schema alteration queries in " + queryCounter + " queries.");
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
		if (dbCount != null) {
			
			dbCount = MapUtil.sortByValue(dbCount);
			
			Iterator dbCountIt = dbCount.entrySet().iterator();
			
			while(dbCountIt.hasNext()) {
				
				Map.Entry pair = (Map.Entry)dbCountIt.next();
		        System.out.println(pair.getKey() + " => " + pair.getValue());
			}
			
			
		}
		
		System.out.println("Read completed.");
	}
	
	public static String fixQuery(String query) {
		query = query.replace("[", "");
		query = query.replace("]", "");
		
		query = query.replace("==", "=");
		
		query = query.replace("OR REPLACE", ""); //JSqlParser doesn't parse Insert OR Replace
		
		return query;
	}

	public static String[] processJSON(String jsonString) {
		//TODO: process JSON
		
		//When DeviceID, bootID, and "db_ptr" is the same, we can be sure that they are from the same app
		//When "bootID" changes, look for new "db_ptr" and "process"
		//It is the "process" where we get the application name
		
		//String databasePointer = JsonPath.read(jsonString, "$.db_ptr");
		//String appName = JsonPath.read(jsonString, "$.process");
		String dbName = "";
		String query = "";
		try {
			dbName = JsonPath.read(jsonString, "$.db_name");
		} catch (Exception ex) {
			//System.err.println(ex.getMessage());
		}
		
		try {
			query = JsonPath.read(jsonString, "$.stmt_text");
		} catch (Exception ex) {
			//System.err.println(ex.getMessage());
		}
		
		return new String[] {dbName, query};
	}
	

	
	
	private static void findAppsOfInterest() {
		System.out.println("Enter the target folder (where you need to write the data)");
		String targetFile = "/Users/gokhanku/Downloads/user0.csv.gz";
		
		try {
			FileInputStream fis = new FileInputStream(targetFile);
			GZIPInputStream gzis = new GZIPInputStream(fis);
			InputStreamReader reader = new InputStreamReader(gzis);
			BufferedReader in = new BufferedReader(reader);
			
			String readLine;
			int recordCounter = 0;
			int hitCounter = 0;
			
			//facebook -> com.facebook.orca and com.facebook.katana
			//hangouts -> com.google.android.talk
			
			while ((readLine = in.readLine()) != null) {
				if (readLine.toLowerCase().contains("facebook")) {
					System.out.println(readLine);
					hitCounter++;
				}
				readLine = in.readLine();
				
				recordCounter++;
			}
			
			in.close();
			reader.close();
			gzis.close();
			fis.close();
			
			System.out.println("There are " + hitCounter + " hits in " + recordCounter + " records.");
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
		System.out.println("Read completed.");
		
	}
	
	
	private static void extractToFile() {
		
		//Scanner keyboard = new Scanner(System.in);
		
		System.out.println("Enter the source file (where the phonelab data file is)");
		String sourceFile = "/Users/gokhanku/Downloads/user0.csv.gz";
		
		System.out.println("Enter the target folder (where you need to write the data)");
		String targetFile = "/Users/gokhanku/Downloads/user0.csv";
		
		int FILE_SIZE_RECORD_LIMIT = 1000000;
		
		ArrayList<String> records = null;
		
		try {
			FileInputStream fis = new FileInputStream(sourceFile);
			GZIPInputStream gzis = new GZIPInputStream(fis);
			InputStreamReader reader = new InputStreamReader(gzis);
			BufferedReader in = new BufferedReader(reader);
			
			records = new ArrayList<String>();
			
			String readLine;
			int recordCounter = 0;
			while ((readLine = in.readLine()) != null) {
				if (readLine.contains("SQLite-Query-PhoneLab")) {
					records.add(readLine);
				}
				readLine = in.readLine();
				
				recordCounter++;
				
				if (recordCounter == FILE_SIZE_RECORD_LIMIT) {
					break;
				}
				
				
			}
			
			in.close();
			reader.close();
			gzis.close();
			fis.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
		System.out.println("Read completed.");
		
		//Write the information extracted to new file
		
		BufferedWriter writer = null;
	    try {
	    		FileOutputStream zip = new FileOutputStream(new File(targetFile), true);

	        writer = new BufferedWriter(
	            new OutputStreamWriter(zip, "UTF-8"));

	        
	        for (int j = 0; j < records.size(); j++) {
	        		writer.append(records.get(j) + System.lineSeparator());
			}
	    } catch (Exception ex) {
			System.out.println(ex.getMessage());
		} finally {         
	        if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    }
	    
	    int problematicLineCounter = 0;
	    
	    for(int i = 0; i < records.size(); i++) {
	    		if (!records.get(i).contains("db_name") && !records.get(i).contains("taint")) {
	    			System.out.println(records.get(i));
	    			problematicLineCounter++;
	    		}
	    }
	    
	    System.out.println("There are " + problematicLineCounter + " problematic lines in " + FILE_SIZE_RECORD_LIMIT + " records.");

	    System.out.println("Write completed.");
	    
	    
	    
	}
	
	private static class MapUtil {
	    public static <K, V extends Comparable<? super V>> Map<K, V> 
	        sortByValue(Map<K, V> map) {
	        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
	        Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
	            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
	                return (o1.getValue()).compareTo( o2.getValue() );
	            }
	        });

	        Map<K, V> result = new LinkedHashMap<K, V>();
	        for (Map.Entry<K, V> entry : list) {
	            result.put(entry.getKey(), entry.getValue());
	        }
	        return result;
	    }
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
	    return map.entrySet()
	              .stream()
	              .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
	              .collect(Collectors.toMap(
	                Map.Entry::getKey, 
	                Map.Entry::getValue, 
	                (e1, e2) -> e1, 
	                LinkedHashMap::new
	              ));
	}

	public static int findQueryCluster(String queryString) {
		// TODO Auto-generated method stub
		return -1;
	}

}
