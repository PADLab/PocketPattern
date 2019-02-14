package edu.buffalo.www.cse.odinlab.PocketBench.Sessions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Point;

public class SessionSplitter {
//	Map<Long,List<DataRow>> sessionsGroupByWindowId;
	public List<DataRow> _rows;
	
	private String workingDirectory;
	private String appName;
		
	public void importClusteringData(List<DataRow> r) {
		this._rows = r;
	}

	public List<Long> getSessionIds(String user){
		return _rows.stream().filter(e -> e.user.equals(user)).map(DataRow::getWindowId).collect(Collectors.toList());
	}
	
	public void setWorkingDirectory(String workingDirectory, String appName) {
		this.workingDirectory = workingDirectory;
		this.appName = appName;
	}
	
	public Boolean readFromFile(String pathToFile){
		Boolean successFlag = false;		
		try(Stream<String> stream = Files.lines(Paths.get(pathToFile))){
			_rows = stream.filter(s -> s != null && s.length() > 0).map(e -> DataRow.separatedStringToDataRow(e, "~")).collect(Collectors.toList());
			if(_rows.size() > 0) successFlag = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return successFlag;
	}
	
	/**
	 * 
	 * @param idleTimeTolerance
	 * @return Map<String,List<DataRow>> in the form <user, list of DataRows>
	 */
	public Map<String,List<DataRow>> _makeSessionsGroupByUser(int idleTimeTolerance){		
		Map<String,List<DataRow>> sessionsGroupByUser = _rows.stream().collect(Collectors.groupingBy(DataRow::getUser));
		sessionsGroupByUser.forEach((user, rowsList) -> {
			long currentWindowId = 1;
			if(rowsList.size() > 0){ // TODO: This needs to done for every user separately
				rowsList.get(0).windowId = currentWindowId;
				int i = 1;
				try{
					for(; i < rowsList.size(); i++){
						DataRow curr = rowsList.get(i);
						DataRow prev = rowsList.get(i-1);
						if(curr.timestamp - prev.timestamp <= idleTimeTolerance){
							curr.windowId = currentWindowId;
							rowsList.set(i, curr); //TODO: Can we modify values in this way in ForEach?
						} else {
							currentWindowId++;
							curr.windowId = currentWindowId;
							rowsList.set(i, curr);
						}
					}
				}catch(Exception e){
					System.out.print(e.getStackTrace());
				}
			}
		});
		return sessionsGroupByUser;		
	}

	public List<OutputDataRow> _calculateAverageSimilarityForWindows(int idleTimeTolerance){ //TODO: Minor details of this function needs to be tweaked for implementing frequent item-sets
//		Boolean successFlag = false;
		Map<String,List<DataRow>> sessionsGroupByUser = _makeSessionsGroupByUser(idleTimeTolerance);
		List<OutputDataRow> outputRows = new ArrayList<>();
		sessionsGroupByUser.forEach((user, userDataRowsList) -> {
			Map<Long,List<DataRow>> sessionsGroupByWindowId = userDataRowsList.stream().collect(Collectors.groupingBy(DataRow::getWindowId));
			sessionsGroupByWindowId.forEach((window, rowsListForWindow)->{
//				double similaritySum = 0;
				List<Double> similarities = new ArrayList<Double>();
//				DoubleAdder da = new DoubleAdder();
				sessionsGroupByWindowId.forEach((window1,rowsListForWindow1) -> {
					if(window!=window1){ //Similarity implementation goes here
						Set<Integer> clustersForWindow = rowsListForWindow.stream().map(DataRow::getCluster).collect(Collectors.toCollection(HashSet::new));
						Set<Integer> clustersForWindow1 = rowsListForWindow1.stream().map(DataRow::getCluster).collect(Collectors.toCollection(HashSet::new));
						Set<Integer> intersection = new HashSet<Integer>(clustersForWindow);
						intersection.retainAll(clustersForWindow1);
						Set<Integer> union = new HashSet<Integer>(clustersForWindow);
						union.addAll(clustersForWindow1);
						Double sim = (double) (intersection.size() / union.size());
						similarities.add(sim);
					}					
				});
//				similarities.parallelStream().forEach(da::add);
//				similaritySum = da.doubleValue();
//				similarities.parallelStream().collect(Collectors.summarizingDouble(mapper))
				Double averageSimilarity = similarities.stream().collect(Collectors.averagingDouble(d -> d));
				int numberOfQueriesInWindow = rowsListForWindow.size();
//				Double averageResponseTime = rowsListForWindow.stream().filter(e -> (e.runningTime > 0)).mapToLong(o -> o.runningTime).average().getAsDouble();
				//TODO: Break down this and debug
				Double averageResponseTime = rowsListForWindow.stream().filter(e -> Objects.nonNull(e.runningTime)).collect(Collectors.averagingLong(dr -> dr.runningTime));
				outputRows.add(new OutputDataRow(user, window, averageSimilarity, averageResponseTime, numberOfQueriesInWindow));
			});			
		});
		return outputRows;
	}

	public Boolean exporttoCSV(List<OutputDataRow> lst, String filePath){
		Boolean status = false;
		String recordAsCsv = lst.stream()
		        .map(OutputDataRow::toCsvRow)
		        .collect(Collectors.joining(System.getProperty("line.separator")));
		try {
			Files.write(Paths.get(filePath), recordAsCsv.getBytes());
			status = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return status;
	}
	
	/**
	 * Returns a summary of results of the session splitting operation
	 * @return
	 */
	public LinkedHashMap<String, LinkedHashMap<Long, List<DataRow>>> exportResults()
	{
		LinkedHashMap<String, LinkedHashMap<Long, List<DataRow>>> m = new LinkedHashMap<>();
		
		_rows.stream().collect(Collectors.groupingBy(DataRow::getUser)).forEach((user, rowsGroupedByUser) -> {
			LinkedHashMap<Long, List<DataRow>> tempValues = new LinkedHashMap<>();			
			
			rowsGroupedByUser.stream().collect(Collectors.groupingBy(DataRow::getWindowId)).forEach((window, rowsGroupedByUserWindow) -> {
				tempValues.put(window, rowsGroupedByUserWindow);
			});
			
			m.put(user, tempValues);
		});
		return m;		
	}
	
	/**
	 * Returns a hash of sessions by date. Note that 'cluster' attribute of every session object will not be populated in the return value.
	 * @param sessionsGroupedByUser corresponds to the output of the function _makeSessionsGroupByUser
	 * @return LinkedHashMap<String, LinkedHashMap<String, List<Session>>> structure is <user, <date, sessions_list > >
	 */
	public LinkedHashMap<String, LinkedHashMap<String, List<Session>>> splitUserSessionsByDate(Map<String,List<DataRow>> sessionsGroupedByUser)
	{
		LinkedHashMap<String, LinkedHashMap<String, List<Session>>> m = new LinkedHashMap<>();		
		sessionsGroupedByUser.forEach((user, dataRowsGroupedByUser) -> {
			LinkedHashMap<String, List<Session>> dateAndSessionsForUser = new LinkedHashMap<>();
			dataRowsGroupedByUser.stream().collect(Collectors.groupingBy(DataRow::getDate)).forEach((date, dataRowsgroupedByDate) -> {
				List<Session> sessionsForUser = new ArrayList<>();
				dataRowsgroupedByDate.stream().collect(Collectors.groupingBy(DataRow::getWindowId)).forEach((windowId, dataRowsGroupedByWindowId) -> {
					Session s = new Session();
					s.setUser(user);
					s.addQueries(dataRowsGroupedByWindowId);
					LongSummaryStatistics summary = dataRowsGroupedByWindowId.stream().collect(Collectors.summarizingLong(DataRow::getTimestamp));
					//TODO commented out the following two lines, see if this changes things
					//s.setStartTime(DataRow.getTimestampAsDate(summary.getMin()));
					//s.setEndTime(DataRow.getTimestampAsDate(summary.getMax()));
					sessionsForUser.add(s);
					System.err.println("On "+date+" user "+user+" Session #"+sessionsForUser.size()+" has "+s.getQueries().size()+" queries");
				});
				dateAndSessionsForUser.put(date, sessionsForUser);
				
			});
			m.put(user, dateAndSessionsForUser);
		});	
		return m;
	}

	
	/**
	 * Returns the ideal idle time for all users
	 * @param res data from session splitting
	 * @return HashMap<String, Integer> containing ideal idle time of corresponding user using automatic knee selection
	 */
	public HashMap<String, Integer> getIdealIdleTimeMap(Map<String, List<Point>> res) {
		HashMap<String, Integer> t = new HashMap<String, Integer>();
		for(String user : res.keySet()){
			List<Point> idleTimeAndNumberOfSessions = res.get(user);
			idleTimeAndNumberOfSessions.sort(Comparator.comparing(Point::getIdleTime));
			Integer idealIdleTimeIndex = findIndexOfKnee(idleTimeAndNumberOfSessions);
			t.put(user, idleTimeAndNumberOfSessions.get(idealIdleTimeIndex).idleTime);
			saveDataForIdealTimeGraph(user, idleTimeAndNumberOfSessions, idealIdleTimeIndex);
		}
		return t;
	}

	/**
	 * Writing ideal time data for each user
	 * @param user
	 * @param idleTimeAndNumberOfSessions
	 * @param idealIdleTimeIndex
	 */
	private void saveDataForIdealTimeGraph(String user, List<Point> idleTimeAndNumberOfSessions,
			Integer idealIdleTimeIndex) {
		try {
			BufferedWriter outputSessionCountIdleTime = new BufferedWriter(
					new FileWriter(new File(workingDirectory
							+ "/sessionCountIdleTimeData.csv"), true)); 
			for (int i = 0; i < idleTimeAndNumberOfSessions.size(); i++) {
				outputSessionCountIdleTime.write(
						user + "~"
						+ idleTimeAndNumberOfSessions.get(i).idleTime + "~"
						+ idleTimeAndNumberOfSessions.get(i).numberofSessions + "~"
						//+ (idleTimeAndNumberOfSessions.get(i).idleTime == idealIdleTimeIndex)
						+ (idealIdleTimeIndex == i)
						+ System.getProperty("line.separator")
				);
			}
			outputSessionCountIdleTime.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
	}

	/**
	 * Returns the ideal idle time for all users
	 * @param res data from session splitting
	 * @return HashMap<String, Integer> containing ideal idle time of corresponding user using automatic knee selection
	 */
	public List<Integer> getIdealIdleTime(Map<String, List<Point>> res) {
		List<Integer> t = new ArrayList<Integer>();
		for(String user : res.keySet()){
			List<Point> idleTimeAndNumberOfSessions = res.get(user);
			idleTimeAndNumberOfSessions.sort(Comparator.comparing(Point::getIdleTime));
			Integer idealIdleTimeIndex = findIndexOfKnee(idleTimeAndNumberOfSessions);
			t.add(idleTimeAndNumberOfSessions.get(idealIdleTimeIndex).idleTime);
			saveDataForIdealTimeGraph(user, idleTimeAndNumberOfSessions, idealIdleTimeIndex);
		}
		return t;
	}
	
	/**
	 * Method returns the index of the point which corresponds to the 'knee' of the function
	 * Using technique discussed in the accepted answer of https://stackoverflow.com/questions/2018178/finding-the-best-trade-off-point-on-a-curve
	 * @param data Collection of points which make the function
	 * @return Index of point
	 */
	private Integer findIndexOfKnee(List<Point> data) {		
		if(data.size() < 2) return null;
		
		Point first = data.get(0);
		Point last = data.get(data.size()-1);
		if(first.idleTime.equals(last.idleTime)) return null; //To avoid Divide by Zero.
		
		Double m = (double) ((first.numberofSessions - last.numberofSessions) / (first.idleTime - last.idleTime)); //Slope
		
		//Line Coefficients. Transforming from 2-point slope form to ax + by +c = 0 form.
		Double a = m;
		Double b = (double) -1;
		Double c = first.numberofSessions - m * first.idleTime;
		
		// Calculating distance of each point from the line
		double denominator = Math.sqrt(a*a + b*b); // sqrt(a*a + b+b)
		Integer indexOfMaxDistance = 0;
		Double maxDistance = 0d;
		for(int i=0; i < data.size(); i++){
			Point p = data.get(i);
			double numerator = Math.abs(a*p.idleTime + b * p.numberofSessions + c); // abs(ax+by+c)
			Double currentDistance = numerator / denominator;
			if(currentDistance > maxDistance) {
//				System.err.println("Revising max dist from "+maxDistance+" to "+currentDistance+" at idx "+indexOfMaxDistance);
				maxDistance = currentDistance;
				indexOfMaxDistance = i;
			}
		}
		
		return indexOfMaxDistance;
	}
	
	/**
	 * Run experiments for ideal idle time calculations. This method can be called directly after importing data. 
	 * @param idleTimes Collection of Idle Times values to be used for making sessions
	 * @return List of ideal idle times for all users
	 */
	public List<Integer> findIdealIdleTimesForAllUsers(List<Integer> idleTimes)
	{
		//Format <User, List of Points>. Point consists of (idle_time, number_of_sessions)
		Map<String,List<Point>> res = new HashMap<String, List<Point>>();
		for(Integer idt : idleTimes){
			Map<String, List<DataRow>> sessionsByUser  = _makeSessionsGroupByUser(idt);
			for(String u : sessionsByUser.keySet()){
				List<DataRow> d = sessionsByUser.get(u);
				Long numberOfSessionsForUser = d.get(d.size()-1).windowId;
				List<Point> pointsForUser = res.get(u);
				if(pointsForUser == null) pointsForUser = new ArrayList<>();
				pointsForUser.add(new Point(idt, numberOfSessionsForUser));
				res.put(u, pointsForUser);
			}
		}
		List<Integer> idealIdleTimes = getIdealIdleTime(res);
		return idealIdleTimes;		
	}

	
	/**
	 * Run experiments for ideal idle time calculations. This method can be called directly after importing data. 
	 * @param idleTimes Collection of Idle Times values to be used for making sessions
	 * @return List of ideal idle times for all users
	 */
	public HashMap<String, Integer> findIdealIdleTimesForAllUsersMap(List<Integer> idleTimes)
	{
		//Format <User, List of Points>. Point consists of (idle_time, number_of_sessions)
		Map<String,List<Point>> res = new HashMap<String, List<Point>>();
		for(Integer idt : idleTimes){
			Map<String, List<DataRow>> sessionsByUser  = _makeSessionsGroupByUser(idt);
			for(String u : sessionsByUser.keySet()){
				List<DataRow> d = sessionsByUser.get(u);
				Long numberOfSessionsForUser = d.get(d.size()-1).windowId;
				List<Point> pointsForUser = res.get(u);
				if(pointsForUser == null) pointsForUser = new ArrayList<>();
				pointsForUser.add(new Point(idt, numberOfSessionsForUser));
				res.put(u, pointsForUser);
			}
		}
		HashMap<String, Integer> idealIdleTimes = getIdealIdleTimeMap(res);
		return idealIdleTimes;		
	}

	public void setWorkingDirectory(String dataFolder) {
		this.setWorkingDirectory(dataFolder,"");
	}

	public List<DataRow> getSessions() {
		return this._rows;
	}
	
	/**
	 * Run experiments for ideal idle time calculations. This method can be called directly after importing data. 
	 * @param idleTimes Collection of Idle Times values to be used for making sessions
	 * @return List of ideal idle times for all users
	 */	
	public HashMap<String, Integer> findIdealIdleTimesForGivenUserMap(List<Integer> idleTimes) {
		//Format <User, List of Points>. Point consists of (idle_time, number_of_sessions)
		Map<String,List<Point>> res = new HashMap<String, List<Point>>();
		for(Integer idt : idleTimes){
			Map<String, List<DataRow>> sessionsByUser  = this.makeSessionsForGivenUser(idt);
			for(String u : sessionsByUser.keySet()){
				List<DataRow> d = sessionsByUser.get(u);
				Long numberOfSessionsForUser = d.get(d.size()-1).windowId;
				List<Point> pointsForUser = res.get(u);
				if(pointsForUser == null) pointsForUser = new ArrayList<>();
				pointsForUser.add(new Point(idt, numberOfSessionsForUser));
				res.put(u, pointsForUser);
			}
		}
		HashMap<String, Integer> idealIdleTimes = getIdealIdleTimeMap(res);
		
		Iterator<String> iterator = idealIdleTimes.keySet().iterator();
		String temp = null;
		while(iterator.hasNext()) {
			temp = iterator.next();
			System.out.println(temp + "," + idealIdleTimes.get(temp));
		}
		
		return idealIdleTimes;			
	}

	private Map<String, List<DataRow>> makeSessionsForGivenUser(Integer idleTimeTolerance) {
		Map<String,List<DataRow>> sessionsGroup = _rows.stream().collect(Collectors.groupingBy(DataRow::getUser));
		sessionsGroup.forEach((user, rowsList) -> {
			long currentWindowId = 1;
			if(rowsList.size() > 0){ 
				rowsList.get(0).windowId = currentWindowId;
				int i = 1;
				try{
					for(; i < rowsList.size(); i++){
						DataRow curr = rowsList.get(i);
						DataRow prev = rowsList.get(i-1);
						if(curr.timestamp - prev.timestamp <= idleTimeTolerance){
							curr.windowId = currentWindowId;
							rowsList.set(i, curr);
						} else {
							currentWindowId++;
							curr.windowId = currentWindowId;
							rowsList.set(i, curr);
						}
					}
				}catch(Exception e){
					System.out.print(e.getStackTrace());
				}
			}
		});
		return sessionsGroup;		
	}
	
}
