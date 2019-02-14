package edu.buffalo.www.cse.odinlab.PocketBench.ActivityDetection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import edu.buffalo.www.cse.odinlab.PocketBench.ControlledData.ActivityData;
import edu.buffalo.www.cse.odinlab.PocketBench.ControlledData.Label;
import edu.buffalo.www.cse.odinlab.PocketBench.ControlledData.LabeledActivity;
import edu.buffalo.www.cse.odinlab.PocketBench.DataPreparation.Analyze;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.ParsabilityStatus;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.StringWithStatus;
import edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity.Util;
import edu.buffalo.www.cse.odinlab.PocketBench.SessionSimilarity.LabeledSession;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import querySimilarityMetrics.Makiyama;

public class ActivityMatcher {
	
	public ActivityMatcher() {
		//TODO
	}
	
	public void loadLabeledActivities(String appString, String fileLocation) {
		
		//Assuming the first column is the timestamp
		//The second column is the JSON string
		//The third column is the Label for the query
		Set<StringWithStatus> intermediate = new HashSet<StringWithStatus>();
		
		try {
			FileInputStream fis = new FileInputStream(fileLocation);
			InputStreamReader reader = new InputStreamReader(fis);
			BufferedReader in = new BufferedReader(reader);
			
			String readLine;
			String[] columns = null;
			String timestamp = "";
			String JSONstring = "";
			StringWithStatus queryString = null;
			String[] dbAndQuery = null;
			String label = "";
			// RESTRUCTURE List<UnlabeledActivity> 
			
			
			
			while ((readLine = in.readLine()) != null) {
				
				//TODO if this is a new type of activity, record it accordingly
				
				columns = readLine.split("\t");
				
				timestamp = columns[0];
				JSONstring = columns[1];
				label = columns[2];
					
				dbAndQuery = Analyze.processJSON(JSONstring);
				
				if (dbAndQuery[0].contains(appString)) {
				
					queryString = Util.getTypeAndString(dbAndQuery[1]);
					if (queryString.getStatus() == ParsabilityStatus.PARSABLE_CRUD_QUERY) {
						intermediate.add(queryString);
					}
					
					//We don't know the cluster yet
					// RESTRUCTURE activityLine = new ActivityData(timestamp, queryString.getStatement(), -1);
					

				}
				
			}
			
			// RESTRUCTURE labeledActivity.setActivityData(currentActivity);
			// RESTRUCTURE activitiesRecorded.add(labeledActivity);
			
			System.out.println("There are " + intermediate.size() + " unique query strings for this controlled dataset.");
			in.close();
			reader.close();
			fis.close();
		
		} catch (Exception ex) {
			
		}

	}
	
	
	public static List<Label> appointLabelsThroughFeatureBasedJaccards(LabeledSession session) {
		
		List<ActivityData> queryList = session.getRows();
		
		
		
		//TODO detect activity
		
		return null;
	}
	
	public static List<Label> appointLabelsThroughClusters(LabeledSession session) {
		
		//TODO detect activity
		
		return null;
	}
	
	public static List<Label> appointLabelsThroughFeatureBasedJensenShannon(LabeledSession session) {
		
		//TODO detect activity
		
		
		
		return null;
	}

}
