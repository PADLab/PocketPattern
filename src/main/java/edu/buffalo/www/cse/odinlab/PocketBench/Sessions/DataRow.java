package edu.buffalo.www.cse.odinlab.PocketBench.Sessions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.google.common.collect.ImmutableSet;

import edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation.QueryData;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Cascade.TermList;

public class DataRow implements Comparable<DataRow>, Comparator<DataRow> {
	public String query;
	public Long timestamp;
	public Integer cluster;
	public String user;
	public Long runningTime;
	public Long windowId;
	public String date;
//	public ArrayList<String> termList; //Extract of terms in a query
	public Set<String> termList; //Extract of terms in a query. Switching to Set for better intersection performance
	
	public String getDate() {
		return DataRow.getTimestampAsDate(timestamp);
	}
	
//	public ArrayList<String> getTermList() {
	public Set<String> getTermList() {	
		return termList;
	}

//	public void setTermList(ArrayList<String> termList) {
	public void setTermList(Set<String> termList) {
		this.termList = termList;
	}

	public String getQuery() {
		return this.query;
	}

	public DataRow() {
	}
	 
	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	/*
	 * @param query
	 * @param timestamp
	 * @param cluster
	 * @param user
	 * @param runningTime
	 */
	public DataRow(String query, Long timestamp, Integer cluster, String user, Long runningTime) {
		this(query, timestamp, cluster, user, runningTime, null);
	}	
	
	@Override
	public String toString() {
		return "DataRow [" + (user != null ? "user=" + user + ", " : "") + (date != null ? "date=" + date + ", " : "")
				+ (query != null ? "query=" + query + ", " : "") + (cluster != null ? "cluster=" + cluster + ", " : "")
				+ (termList != null ? "termList=" + termList : "NO_LIST")
				+ (windowId != null ? "windowId=" + windowId : "") + "]";
	}

	/**
	 * @param query
	 * @param timestamp
	 * @param cluster
	 * @param user
	 * @param runningTime
	 * @param windowId
	 */
	public DataRow(String query, Long timestamp, Integer cluster, String user, Long runningTime, Long windowId) {
		this.query = query;
		this.timestamp = timestamp;
		this.cluster = cluster;
		this.user = user;
		this.runningTime = runningTime;
		this.windowId = windowId;
	}	
	
	public static DataRow separatedStringToDataRow(String s, String separator){
		if(s == null)	return null;
		String[] items = s.split(separator);
		int l = items.length;
		if(l < 3) return null; //Should at least have query, timestamp, cluster for a meaningful output
		DataRow temp = new DataRow();
		try{
			temp.query = items[0];
			temp.termList = DataRow.tokenizeQuery(items[0]);
			temp.timestamp = Long.parseLong(items[1]);
			temp.cluster = Integer.parseInt(items[2]);
			temp.user = items[3];
			temp.runningTime = Long.parseLong(items[4]);
//			temp.windowId = Long.parseLong(items[5]);
		}catch(ArrayIndexOutOfBoundsException e){
			System.err.println(s+" has no data at index "+e.getLocalizedMessage());
		}
		return temp;		
	}
	
	public static Set<String> tokenizeQuery(String statement) {
		
		String[] prelimResult = statement.split("\\s|,");
//		ArrayList<String> terms = new ArrayList<>();
		Set<String> terms = new HashSet<>();
		for (int i = 0; i < prelimResult.length; i++) {
			if (!(prelimResult[i].equals(null) || prelimResult[i].equals(""))) {
				terms.add(prelimResult[i]);
			}
		} 
		return terms;
	}
	
	public long getWindowId() {
		return windowId;
	}
	
	
	public String getUser() {
		return user;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cluster;
		result = prime * result + ((query == null) ? 0 : query.hashCode());
//		result = prime * result + (int) (runningTime ^ (runningTime >>> 32)); //Running time might not be present in file sometimes
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + (int) (windowId ^ (windowId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataRow other = (DataRow) obj;
		if (cluster != other.cluster)
			return false;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.equals(other.query))
			return false;
		if (runningTime != other.runningTime)
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (windowId != other.windowId)
			return false;
		return true;
	}

	public Integer getCluster() {
		return cluster;
	}

	public void setCluster(Integer cluster) {
		this.cluster = cluster;
	}
	
	public static String getTimestampAsDate(Long t){
	    Calendar cal = Calendar.getInstance();
	    String result = null;
	    try{
		    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		    cal.setTimeInMillis(t);
		    int month = cal.get(Calendar.MONTH) + 1;
		    String monthString = month < 10 ? "0"+month : month+"";
		    int day = cal.get(Calendar.DAY_OF_MONTH);
		    String dayString = day < 10 ? "0"+day : day+"";
		    result = cal.get(Calendar.YEAR) + "-" + monthString + "-" + dayString ;
	    } catch(NullPointerException npe){
	    		System.err.println(npe.getStackTrace());
	    }	    
	    return result;
	}

	@Override
	public int compare(DataRow o1, DataRow o2) {
		if(o1.getTimestamp() > o2.getTimestamp()){
            return 1;
        } else if (o1.getTimestamp() == o2.getTimestamp()) {
        		return 0;
        }else {
            return -1;
        }
	}
	
	@Override
	public int compareTo(DataRow o) {
		if (this.getTimestamp() > o.getTimestamp()) {
			return 1;
		} else if (this.getTimestamp() == o.getTimestamp()) {
			return 0;
		} else {
			return -1;
		}
	}	

	
//	public Session convertToSession()
//	{
//		Session s = new Session();
//		s.setCluster(this.cluster);
////		private int cluster;
//		s.setUser(this.user);
////		private String user;
//		this.
//		private ArrayList<DataRow> queries = null;
//		private String startTime = null;
//		private String endTime = null;
//		
//	}
}
