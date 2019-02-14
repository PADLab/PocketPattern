package edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.Locale;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import edu.buffalo.www.cse.odinlab.PocketBench.ControlledData.ActivityData;
import edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation.Analyze;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.DataRow;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import querySimilarityMetrics.Makiyama;

public class Util {
	
	public static long parseFromDateStringToLong(String dateString) {
		// 2017-08-16 22:06:18.394.394999985
		
		DateTimeFormatter formatter = null;
		LocalDateTime date = null;
		try {
			formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss.SSS.nnnnnnnnn");
			date = LocalDateTime.parse(dateString, formatter);
		
		} catch(Exception ex3) {
			try {
				formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss.SSSSSS");
				date = LocalDateTime.parse(dateString, formatter);
			} catch (Exception ex4){
				//formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
				formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd[ [HH][:mm][:ss][.SSS]]")
	            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
	            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
	            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
	            .toFormatter();
				date = LocalDateTime.parse(dateString, formatter);
			}
			//System.out.println(dateString + " is not in an expected format.");
		}
		ZoneId zoneId = ZoneId.systemDefault();
		long epoch = date.atZone(zoneId).toInstant().toEpochMilli();
		
		return epoch;
	}
	
	public static LocalDateTime parseFromLongToLocalTime(long dateLong) {
		LocalDateTime date = null;
		try {
			date = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateLong), ZoneId.systemDefault());
		} catch (Exception ex) {
			System.out.println(dateLong + " is not convertable to local time.");
		}
		
		return date;
	}
	
	public static LocalDateTime parseFromDateStringToLocalTime(String dateString) {
		// 2017-08-16 22:06:18.394.394999985
		
		DateTimeFormatter formatter = null;
		LocalDateTime date = null;
		try {
			formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss.SSS.nnnnnnnnn");
			date = LocalDateTime.parse(dateString, formatter);
		} catch(Exception ex){
			try {
				formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss.SSSSSS");
				date = LocalDateTime.parse(dateString, formatter);
			} catch (Exception ex2) {
				try {
					//formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
					formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd[ [HH][:mm][:ss][.SSS]]")
		            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
		            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
		            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
		            .toFormatter();
					date = LocalDateTime.parse(dateString, formatter);
				} catch(Exception ex3) {
					System.out.println(dateString + " is not in an expected format.");
				}
			}
		}
		
		return date;
	}
	
	public static List<String> getFileList(String[] listOfFileNames) {
		
		Set<Integer> filesToBeIgnored = new HashSet<Integer>();
		List<String> listOfFiles = new ArrayList<String>();
 		
		for (int i = 0; i < listOfFileNames.length; i++) {
			
			if (listOfFileNames[i].contains("uniqueQueries")) {
				filesToBeIgnored.add(i);
			}
		}
		
		for (int i = 0; i < listOfFileNames.length; i++) {
			
			if (!filesToBeIgnored.contains(i)) {
				listOfFiles.add(listOfFileNames[i]);
			}
		}
		
		return listOfFiles;
		
		
	}
	
	public static LocalDateTime parseFromDateStringToLocalTimeSecond(String dateString) {
		// 2017-08-16 22:06:18.394.394999985
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss");
		LocalDateTime date = LocalDateTime.parse(dateString, formatter);
		return date;
	}
	
	public static ParsabilityStatus checkIfParsable(String query) {
		if (!query.equals("")) {
			query = Analyze.fixQuery(query);

			String lowerCaseQuery = query.toLowerCase();

			if (query.toLowerCase().startsWith("pragma")) {
				return ParsabilityStatus.PRAGMA;
			} else if (lowerCaseQuery.startsWith("begin") || lowerCaseQuery.startsWith("commit")
					|| lowerCaseQuery.startsWith("abort") || lowerCaseQuery.startsWith("rollback")
					|| lowerCaseQuery.startsWith(";")) {
				return ParsabilityStatus.TRANSACTION_COMMAND;
			} else if (lowerCaseQuery.startsWith("create trigger")
					|| lowerCaseQuery.startsWith("create index")
					|| lowerCaseQuery.startsWith("attach database") || lowerCaseQuery.startsWith("analyze")
					|| lowerCaseQuery.startsWith("reindex")) {
				return ParsabilityStatus.MAINTENANCE_QUERY;
			} else if (lowerCaseQuery.startsWith("alter table")) {
				return ParsabilityStatus.SCHEMA_ALTERATION_QUERY;
			} else {
				try {
					ByteArrayInputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
					CCJSqlParser parser = new CCJSqlParser(stream);
					Statement statement = parser.Statement();

					return ParsabilityStatus.PARSABLE_CRUD_QUERY;

					// System.out.println("An example of accepted query: " + query);
				} catch (ParseException e) {
					return ParsabilityStatus.UNPARSABLE_QUERY;
					// System.out.println("An example of unparsable query: " + query + " : " +
					// e.getMessage());
					// System.out.println("An example of unparsable query: " + query);
				} catch (Exception e) {
					System.err.println("Some other problem: " + e.getMessage());
					return ParsabilityStatus.UNPARSABLE_QUERY;
				} catch (Error e) {
					System.out.println("The reason JSqlParser crashed is: " + query);
					return ParsabilityStatus.PARSER_CRASHED;
				}
			}
		} else {
			return ParsabilityStatus.EMPTY_STRING;
		}

	}

	public static StatementWithStatus getTypeAndStatement(String query) {
		if (!query.equals("")) {
			query = Analyze.fixQuery(query);

			String lowerCaseQuery = query.toLowerCase();

			if (query.toLowerCase().startsWith("pragma")) {
				return new StatementWithStatus(ParsabilityStatus.PRAGMA, null);
			} else if (lowerCaseQuery.startsWith("begin") || lowerCaseQuery.startsWith("commit")
					|| lowerCaseQuery.startsWith("abort") || lowerCaseQuery.startsWith("rollback")
					|| lowerCaseQuery.startsWith(";")) {
				return new StatementWithStatus(ParsabilityStatus.TRANSACTION_COMMAND, null);
			} else if (lowerCaseQuery.startsWith("create trigger")
					|| lowerCaseQuery.startsWith("create index")
					|| lowerCaseQuery.startsWith("attach database") || lowerCaseQuery.startsWith("analyze")
					|| lowerCaseQuery.startsWith("reindex")) {
				return new StatementWithStatus(ParsabilityStatus.MAINTENANCE_QUERY, null);
			} else if (lowerCaseQuery.startsWith("alter table")) {
				return new StatementWithStatus(ParsabilityStatus.SCHEMA_ALTERATION_QUERY, null);
			} else {
				try {
					ByteArrayInputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
					CCJSqlParser parser = new CCJSqlParser(stream);
					Statement statement = parser.Statement();

					return new StatementWithStatus(ParsabilityStatus.PARSABLE_CRUD_QUERY, statement);

					// System.out.println("An example of accepted query: " + query);
				} catch (ParseException e) {
					return new StatementWithStatus(ParsabilityStatus.UNPARSABLE_QUERY, null);
					// System.out.println("An example of unparsable query: " + query + " : " +
					// e.getMessage());
					// System.out.println("An example of unparsable query: " + query);
				} catch (Exception e) {
					System.err.println("Some other problem: " + e.getMessage());
					return new StatementWithStatus(ParsabilityStatus.UNPARSABLE_QUERY, null);
				} catch (Error e) {
					System.out.println("The reason JSqlParser crashed is: " + query);
					return new StatementWithStatus(ParsabilityStatus.PARSER_CRASHED, null);
				}
			}
		} else {
			return new StatementWithStatus(ParsabilityStatus.EMPTY_STRING, null);
		}

	}
	
	public static StringWithStatus getTypeAndString(String query) {
		if (!query.equals("")) {
			query = Analyze.fixQuery(query);

			String lowerCaseQuery = query.toLowerCase();

			if (query.toLowerCase().startsWith("pragma")) {
				return new StringWithStatus(ParsabilityStatus.PRAGMA, null);
			} else if (lowerCaseQuery.startsWith("begin") || lowerCaseQuery.startsWith("commit")
					|| lowerCaseQuery.startsWith("abort") || lowerCaseQuery.startsWith("rollback")
					|| lowerCaseQuery.startsWith(";")) {
				return new StringWithStatus(ParsabilityStatus.TRANSACTION_COMMAND, null);
			} else if (lowerCaseQuery.startsWith("create trigger")
					|| lowerCaseQuery.startsWith("create index")
					|| lowerCaseQuery.startsWith("attach database") || lowerCaseQuery.startsWith("analyze")
					|| lowerCaseQuery.startsWith("reindex")) {
				return new StringWithStatus(ParsabilityStatus.MAINTENANCE_QUERY, null);
			} else if (lowerCaseQuery.startsWith("alter table")) {
				return new StringWithStatus(ParsabilityStatus.SCHEMA_ALTERATION_QUERY, null);
			} else {
				try {
					ByteArrayInputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
					CCJSqlParser parser = new CCJSqlParser(stream);
					Statement statement = parser.Statement();

					return new StringWithStatus(ParsabilityStatus.PARSABLE_CRUD_QUERY, query);

					// System.out.println("An example of accepted query: " + query);
				} catch (ParseException e) {
					return new StringWithStatus(ParsabilityStatus.UNPARSABLE_QUERY, null);
					// System.out.println("An example of unparsable query: " + query + " : " +
					// e.getMessage());
					// System.out.println("An example of unparsable query: " + query);
				} catch (Exception e) {
					System.err.println("Some other problem: " + e.getMessage());
					return new StringWithStatus(ParsabilityStatus.UNPARSABLE_QUERY, null);
				} catch (Error e) {
					System.out.println("The reason JSqlParser crashed is: " + query);
					return new StringWithStatus(ParsabilityStatus.PARSER_CRASHED, null);
				}
			}
		} else {
			return new StringWithStatus(ParsabilityStatus.EMPTY_STRING, null);
		}

	}

	public static Statement convertFromStringWithStatus(StringWithStatus stringWithStatus) {
		if (stringWithStatus.getStatus() == ParsabilityStatus.PARSABLE_CRUD_QUERY) {
			try {
				ByteArrayInputStream stream = new ByteArrayInputStream(stringWithStatus.getStatement().getBytes(StandardCharsets.UTF_8));
				CCJSqlParser parser = new CCJSqlParser(stream);
				Statement statement = parser.Statement();
	
				return statement;
	
				// System.out.println("An example of accepted query: " + query);
			} catch (ParseException e) {
				return null;
				// System.out.println("An example of unparsable query: " + query + " : " +
				// e.getMessage());
				// System.out.println("An example of unparsable query: " + query);
			} catch (Exception e) {
				System.err.println("Some other problem: " + e.getMessage());
				return null;
			} catch (Error e) {
				System.out.println("The reason JSqlParser crashed is: " + stringWithStatus.getStatement());
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static Statement convertFromString(String string) {
		StatementWithStatus statementWithStatus = getTypeAndStatement(string);
		if (statementWithStatus.getStatus() == ParsabilityStatus.PARSABLE_CRUD_QUERY) {
			return statementWithStatus.getStatement();
		} else {
			return null;
		}
	}
	
	public static boolean checkIfContainsStatement(List<Statement> statementList, Statement statement) {
		boolean retVal = false;
		for (int i = 0; i < statementList.size(); i++) {
			if (UniqueQueryExtractor.compareStatement(statementList.get(i), statement)) {
				retVal = retVal | true;
				break;
			}
		}
		return retVal;
		
	}
	
	public static TreeMap<String, Integer> getQueryFeatureDistribution(String query) {
		InputStream stream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
		CCJSqlParser parser = new CCJSqlParser(stream);
		Statement statement = null;
		try {
			statement = parser.Statement();
		} catch (ParseException e) {			    
			//System.err.println(e);
		}catch(Error  e){
			//System.err.println(e);
		}
		
		if (statement != null)
			return Makiyama.getQueryVector(statement);
		else
			return null;
	}
	
	public static ActivityData convertDataRowToActivityData(DataRow row) {
		if (row == null) {
			return null;
		}
		ActivityData activity = new ActivityData(row.getDate(), row.getQuery(), row.getCluster());
		return activity;
		
	}

}
