package edu.buffalo.www.cse.odinlab.PocketBench.ControlledData;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation.Analyze;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.Util;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

public class ActivityData {
	
	
	private long timestamp = -1;
	private String database = "";
	private String query = "";
	private int queryClusterAppointment = -1;
	private String label = "";
	
	
	public ActivityData(long timestamp, String query, int queryCluster) {
		this.timestamp = timestamp;
		this.query = query;
		this.queryClusterAppointment = queryCluster;
	}
	
	public ActivityData(String timestamp, String query, int queryCluster) {
		this.timestamp = Util.parseFromDateStringToLong(timestamp);
		this.query = query;
		this.queryClusterAppointment = queryCluster;
	}
	
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getDatabase() {
		return database;
	}
	
	public void setDatabase(String database) {
		this.database = database;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public int getQueryCluster() {
		return queryClusterAppointment;
	}
	
	public void setQueryCluster(int queryCluster) {
		this.queryClusterAppointment = queryCluster;
	}
	
	public static ActivityData convertUnlabeledActivityQueryToActivityData(UnlabeledActivityQuery unlabeledActivityQuery, String targetApp) {
		
		ActivityData retVal = null;
		int queryCluster = -1;
		String queryString = null;
		
		if (unlabeledActivityQuery.getDatabase().matches(targetApp)) {
			
			queryString = unlabeledActivityQuery.getQuery();
			
			String lowerCaseQuery = null;
			if (!queryString.equals("")) {
				queryString = Analyze.fixQuery(queryString);
				lowerCaseQuery = queryString.toLowerCase();
			} else {
				return null;
			}
			
			if (lowerCaseQuery.startsWith("pragma")) {
				return null;
			} else if (lowerCaseQuery.startsWith("begin")
					|| lowerCaseQuery.startsWith("commit")
					|| lowerCaseQuery.startsWith("abort")
					|| lowerCaseQuery.startsWith("rollback")
					|| lowerCaseQuery.startsWith(";")) {
				return null;
			} else if (lowerCaseQuery.startsWith("create trigger")
					|| lowerCaseQuery.startsWith("create index")
					|| lowerCaseQuery.startsWith("attach database")
					|| lowerCaseQuery.startsWith("analyze")
					|| lowerCaseQuery.startsWith("reindex")) {
				return null;
			} else if (lowerCaseQuery.startsWith("alter table")) {
				return null;
			} else {
				try {
					ByteArrayInputStream stream = new ByteArrayInputStream(queryString.getBytes(StandardCharsets.UTF_8));
					CCJSqlParser parser = new CCJSqlParser(stream);
					Statement statement = parser.Statement();
					
					queryCluster = Analyze.findQueryCluster(queryString);
					
					//System.out.println("An example of accepted query: " + query);
				} catch (ParseException e) {			    
					return null;
					//System.out.println("An example of unparsable query: " + query + " : " + e.getMessage());
					//System.out.println("An example of unparsable query: " + query);
				} catch(Exception e){
					System.out.println("Some other problem: " + queryString);
					return null;
				} catch(Error e){
					System.out.println("The reason JSqlParser crashed is: " + queryString);
					return null;
				}
			}
			
			retVal = new ActivityData(unlabeledActivityQuery.getTimestamp(),
									unlabeledActivityQuery.getQuery(),
									queryCluster);
		
		}
		
		return retVal;
	}

	
}
