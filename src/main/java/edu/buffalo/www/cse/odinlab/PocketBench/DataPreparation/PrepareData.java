package edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.*;

import com.jayway.jsonpath.JsonPath;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

public class PrepareData {
	
	public static void main(String[] args) {
		String sourceFolder = "";
		String targetFolder = "";
		FileType fileType = null;

		String[] apps = {
				"com.google.android.gms", 
				"com.android.providers.media",
				"com.google.android.apps.photos",
				"com.facebook",
				"com.whatsapp",
				"com.google.android.gm/",
				"com.android.providers.contacts",
				"com.android.providers.calendar",
				"com.google.android.apps.plus",
				"com.android.messaging",
				"com.google.android.talk",
				"com.twitter.android",
				"com.instagram.android"
		};

		Scanner keyboard = new Scanner(System.in);

		if (args == null || args.length == 0) {
			System.out.println("Enter the source folder (where the phonelab data files are)");
			// sourceFolder = keyboard.nextLine();
			//sourceFolder = "/home/gokhanku/eclipse-workspace/data";
			sourceFolder = "/media/gokhanku/1834be66-2b44-4022-926a-c298e4f4eaa4/logfiles";
			
			System.out.println("Enter the target folder (where you need to write the data)");
			// targetFolder = keyboard.nextLine();
			//targetFolder = "/home/gokhanku/eclipse-workspace/AppData";
			targetFolder = "/home/gokhanku/eclipse-workspace/data";
			
			System.out.println("Enter file type: (type \\'zipped\\' or \\'extracted\\')");
			// String fileTypeString = keyboard.nextLine();
			String fileTypeString = "zippedFile";
			if (fileTypeString.equals("extracted")) {
				fileType = FileType.extractedFile;
			} else {
				fileType = FileType.zippedFile;
			}
		} else {
			if (args[0].equals("--help")) {
				System.out.print("\nRequired arguments are\n" + "\t-source (data file location) \n"
						+ "\t-target (where to store the output) \n" + "\t-fileType (\'zipped\' or \'extracted\' \n");
			} else {
				sourceFolder = args[1];
				targetFolder = args[3];
				if (args[5].equals("extracted")) {
					fileType = FileType.extractedFile;
				} else {
					fileType = FileType.zippedFile;
				}
			}
		}

		if (fileType == FileType.zippedFile) {

			// Get the device IDs in the source folder
			String[] devices = new File(sourceFolder).list();

			Arrays.sort(devices);

			int counter = 0;

			// Iterate on all the devices
			for (String deviceID : devices) {
				// extracting here

				List<File> filesToBeProcessed = new ArrayList<File>();
				// Making sure it's a device ID
				if (new File(sourceFolder + File.separator + deviceID).isDirectory()) {
					filesToBeProcessed.addAll(traverseFolder(new File(sourceFolder + File.separator + deviceID)));
				}

				Collections.sort(filesToBeProcessed, new Comparator<File>() {
					@Override
					public int compare(File f1, File f2) {
						return ((File) f1).getAbsolutePath().compareTo(((File) f2).getAbsolutePath());
					}
				});

				extractFiles(counter, filesToBeProcessed, targetFolder);
				counter++;
			}
		} else if (fileType == FileType.extractedFile) {

			List<File> filesToBeProcessed = new ArrayList<File>();
			// Making sure it's a device ID
			if (new File(sourceFolder).isDirectory()) {
				filesToBeProcessed.addAll(traverseFolder(new File(sourceFolder)));
			}

			extractAppQueries(filesToBeProcessed, targetFolder, apps);

		}

	}
	
	private static void unzipFile(File zippedFile) {
		byte[] buffer = new byte[1024];

		try{

			GZIPInputStream gzis =
					new GZIPInputStream(new FileInputStream(zippedFile));

			FileOutputStream out =
					new FileOutputStream(
							zippedFile.getAbsolutePath()
							.substring(0, 
									zippedFile.getAbsolutePath().toString().lastIndexOf(".")));

			int len;
			
			while ((len = gzis.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}

			gzis.close();
			out.close();

			System.out.println("Done extracting " + zippedFile.getAbsoluteFile().getName());

		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	private static void extractAppQueries(List<File> filesToBeProcessed, String targetFolder,
			String[] appNames) {

		for (File file : filesToBeProcessed) {

			System.out.println("Starting to read the file: " + file.getAbsolutePath());
			
			Map<String, Integer> dbCount = null;

			try {
				FileInputStream fis = new FileInputStream(file);
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
				int fileWriteCount = 0;
				String[] columns = null;
				String dbName = "";
				String query = "";
				String[] dbQueryPair = null;
				dbCount = new TreeMap<String, Integer>();
				InputStream stream = null;
				CCJSqlParser parser = null;
				Statement statement = null;
				String lowerCaseQuery = "";
				String data = "";
				String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));

				// facebook -> com.facebook.orca and com.facebook.katana
				// hangouts -> com.google.android.talk

				while ((readLine = in.readLine()) != null) {
					columns = readLine.split("\t");
					//System.out.println(readLine);
					//System.out.println(columns.length);

					dbQueryPair = Analyze.processJSON(columns[columns.length - 1]);
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
						query = Analyze.fixQuery(query);

						lowerCaseQuery = query.toLowerCase();

						if (query.toLowerCase().startsWith("pragma")) {
							pragmaCounter++;
						} else if (lowerCaseQuery.startsWith("begin") || lowerCaseQuery.startsWith("commit")
								|| lowerCaseQuery.startsWith("abort") || lowerCaseQuery.startsWith("rollback")
								|| lowerCaseQuery.startsWith(";")) {
							beginAndCommitCounter++;
						} else if (lowerCaseQuery.startsWith("create trigger")
								|| lowerCaseQuery.startsWith("create index")
								|| lowerCaseQuery.startsWith("attach database") || lowerCaseQuery.startsWith("analyze")
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

								/**
								 * Log structure
								 * 0 -> DeviceID
								 * 1 -> Unix timestamp (server)
								 * 2 -> Unix timestamp + order
								 * 3 -> bootID unique per device per boot
								 * 4 -> log-line shows if there is a loss
								 * 5 -> timestamp client per CPU
								 * 6 -> human readable time client
								 * 7 -> ProcessID
								 * 8 -> ThreadID (sure?)
								 * 9 -> log level (Verbose (V), Debug (D), Info (I), Warning (W), Error (E)?)
								 * 10 -> LogType (we are looking for SQLite-Query-PhoneLab)
								 * 11 -> JSON Object
								 * 
								 */

								for (String app : appNames) {
									//System.out.println(app);
									//System.out.println(dbName);
									if (dbName.matches(".*" + app +".*")) {
										
										File tempFile = new File(
												targetFolder + File.separator + app + File.separator + fileName + ".gz");
										
										if (!Files.exists(tempFile.getParentFile().toPath())) {
											Files.createDirectory(tempFile.getParentFile().toPath());
										}
										
										
										FileOutputStream fos = new FileOutputStream(tempFile,
												true);
										GZIPOutputStream gos = new GZIPOutputStream(fos);
										
										
										BufferedWriter outputAppData = new BufferedWriter(new OutputStreamWriter(gos));
										
										//System.out.println("Trying to output to: " + targetFolder + File.separator + app + File.separator + fileName);

										//data = columns[0] + "\t" + dbName + "\t" + columns[1] + "\t"
										//		+ "NotUsed" + "\t" + 0 + "\t" + columns[6] + "\t"
										//		+ "NotUsed" + "\t" + query;
										
										data = columns[0] + "\t" + dbName + "\t" + columns[1] + "\t" + columns[6] + "\t" + query;
										
										fileWriteCount++;
										
										if (fileWriteCount % 10000 == 0) {
											System.out.println("Currently, " + fileWriteCount + " lines are written from this file.");
										}

										outputAppData.append(data + System.lineSeparator());
										outputAppData.close();
										//System.out.println("A line is written.");
										break;
									}
								}

								// System.out.println("An example of accepted query: " + query);
							} catch (ParseException e) {
								unparsableQueryCounter++;
								// System.out.println("An example of unparsable query: " + query + " : " +
								// e.getMessage());
								// System.out.println("An example of unparsable query: " + query);
							} catch (Exception e) {
								someOtherProblem++;
								//System.out.println("Some other problem: " + query);
								System.out.println("Some other problem: " + e.getMessage());
								//e.printStackTrace();
							} catch (Error e) {
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

				System.out.println("There are " + noDBCounter + " lines where the JSON is unparsable in "
						+ recordCounter + " records.");
				System.out.println("There are " + queryCounter + " queries in " + (recordCounter - noDBCounter)
						+ " parsable JSON records.");
				System.out.println(
						unparsableQueryCounter + " queries couldn't be parsed in " + queryCounter + " queries.");
				System.out
						.println("There are " + parsedQueryCount + " parsed queries in " + queryCounter + " queries.");
				System.out.println(
						"There are also " + someOtherProblem + " other problems in " + queryCounter + " queries.");
				System.out.println(
						"There are also " + pragmaCounter + " pragma commands in " + queryCounter + " queries.");
				System.out.println("There are also " + beginAndCommitCounter + " transaction commands in "
						+ queryCounter + " queries.");
				System.out.println("There are also " + maintenanceQueryCounter + " maintenance commands in "
						+ queryCounter + " queries.");
				System.out.println("There are also " + schemaAlteration + " schema alteration queries in "
						+ queryCounter + " queries.");

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}

			System.out.println("Read completed.");

		}
	}
	
	private static String convertToLine(String input) {
		//TODO create a new object for each line to keep the lines in an ordered list
		//TODO the new object should have a comparator
		return input;
	}
	

	/**
	 * 
	 * @param deviceID
	 * @param filesToBeProcessed
	 * @param targetFolder
	 * @param appName
	 */
	private static void extractFiles(int deviceID, List<File> filesToBeProcessed, String targetFolder) {
		
		List<String> records = new ArrayList<String>();
		
		//Extract SQLite rows from the file
		
		for(int i = 0; i < filesToBeProcessed.size(); i++) {
			try {
				//BufferedReader inputUserData = new BufferedReader(
				//		new FileReader(filesToBeProcessed.get(i)));
				
				System.out.println(filesToBeProcessed.get(i).getAbsolutePath() + " file is being read.");
				
				FileInputStream fis = new FileInputStream(filesToBeProcessed.get(i));
				GZIPInputStream gzis = new GZIPInputStream(fis);
				InputStreamReader reader = new InputStreamReader(gzis);
				BufferedReader in = new BufferedReader(reader);
				
				records = new ArrayList<String>();
				
				String readLine;
				while ((readLine = in.readLine()) != null) {
					if (readLine.contains("SQLite-Query-PhoneLab")) {
						records.add(convertToLine(readLine));
					}
					//readLine = in.readLine();
				}
				
				in.close();
				reader.close();
				gzis.close();
				fis.close();
				
				//Write the information extracted to new file
				
				BufferedWriter writer = null;
			    try {
			        GZIPOutputStream zip = new GZIPOutputStream(
			            new FileOutputStream(new File(targetFolder + File.separator + "user" + deviceID + ".csv.gz"), true));

			        writer = new BufferedWriter(
			            new OutputStreamWriter(zip));

			        
			        for (int j = 0; j < records.size(); j++) {
			        		writer.append(records.get(j) + System.lineSeparator());
					}
			    } catch (Exception ex) {
					System.out.println(ex.getMessage());
				} finally {         
			        if (writer != null)
			            writer.close();
			    }
				
			    BufferedWriter fileAccountWriter = null;
			    try {
			    	FileOutputStream fos = new FileOutputStream(
			    			new File(targetFolder + File.separator + "processedFiles.txt"), true);
			    	fileAccountWriter = new BufferedWriter(
			    			new OutputStreamWriter(fos, "UTF-8"));
			    	
			    	fileAccountWriter.append(filesToBeProcessed.get(i).getAbsolutePath() + System.lineSeparator());
			    } catch (Exception ex) {
			    	System.out.println(ex.getMessage());
			    } finally {
			    	if (fileAccountWriter != null)
			    		fileAccountWriter.close();
			    }
			    

				System.out.println(filesToBeProcessed.get(i).getAbsolutePath() + " file is closed.");
				System.out.println((filesToBeProcessed.size() - i - 1) + " files still to be read.");
				System.out.println("Currenty there are " + records.size() + " records.");
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		/**
		 * Log structure
		 * 0 -> DeviceID
		 * 1 -> Unix timestamp (server)
		 * 2 -> Unix timestamp + order
		 * 3 -> bootID unique per device per boot
		 * 4 -> log-line shows if there is a loss
		 * 5 -> timestamp client per CPU
		 * 6 -> human readable time client
		 * 7 -> ProcessID 
		 * 8 -> ThreadID (sure?)
		 * 9 -> log level (Verbose (V), Debug (D), Info (I), Warning (W), Error (E)?)
		 * 10 -> LogType (we are looking for SQLite-Query-PhoneLab)
		 * 11 -> JSON Object (look at Quick-JSON parser)
		 * 
		 */
		
		try {
			
						
			/*
			//Delete the .out file when you are done
			 * No need anymore
			
			for (int i = 0; i < filesToBeProcessed.size(); i++) {
				Files.delete(filesToBeProcessed.get(i).toPath());
			}
			*/
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
	}

	
	
	/**
	 * Find out all the files that belongs to the user
	 * @param userFolder: this is the folder that includes all data collected for a specific user
	 * @return 
	 */
	public static List<File> traverseFolder(File userFolder) {

		//System.out.println(userFolder.getAbsolutePath());
		
		List<File> retVal = new ArrayList<File>();

		if(userFolder.isDirectory() && !userFolder.getAbsolutePath().endsWith(".gz")) {
			String[] subNote = userFolder.list();
			for(String filename : subNote){
				retVal.addAll(traverseFolder(new File(userFolder, filename)));
			}
		//} else if (userFolder.getAbsolutePath().endsWith(".gz")){
			File[] filesToBeDecompressed = userFolder.listFiles(new FilenameFilter() { 
                public boolean accept(File dir, String filename)
                     { return filename.endsWith(".gz"); }
			} );
			
			/*
			
			//TODO unzip had been done here
			
			for (int i = 0; i < filesToBeDecompressed.length; i++) {
				unzipFile(filesToBeDecompressed[i]);
			}

	        File[] filesToBeProcessed = userFolder.listFiles(new FilenameFilter() { 
	                 public boolean accept(File dir, String filename)
	                      { return filename.endsWith(".out"); }
	        } );
	        
	        
	        retVal.addAll(Arrays.asList(filesToBeProcessed));
	        */
			
			if (filesToBeDecompressed != null) {
				System.out.println(filesToBeDecompressed.length + " files found in " + userFolder.getPath());
				retVal.addAll(Arrays.asList(filesToBeDecompressed));
			}
		}
		
		return retVal;
	}
	
	public enum FileType {
		zippedFile,
		extractedFile
	}
	
}




