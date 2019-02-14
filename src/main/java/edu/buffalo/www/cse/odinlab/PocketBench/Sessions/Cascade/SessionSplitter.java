	package edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Cascade;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.DataRow;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.OutputDataRow;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Session;
import net.sf.jsqlparser.parser.CCJSqlParser;

public class SessionSplitter {
public List<DataRow> _rows;
	
	private String workingDirectory;
	private String appName;
	private Set<String> esaCorpus;
	
	public Set<String> getEsaCorpus() {
		return esaCorpus;
	}

	public void setEsaCorpus(Set<String> esaCorpus) {
		this.esaCorpus = esaCorpus;
	}

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
	 * Splits queries into sessions using the 3-step Cascade method
	 * @return Map<String,List<DataRow>> in the form <user, list of DataRows>
	 */
	public Map<String,List<DataRow>> _makeSessionsGroupByUser(){		
		Map<String,List<DataRow>> sessionsGroupByUser = _rows.stream().collect(Collectors.groupingBy(DataRow::getUser));
		if(this.esaCorpus == null || this.esaCorpus.size() < 1){
			System.err.println("Populate ESA Corpus first. Aborting...");
			return null;
		}
		sessionsGroupByUser.forEach((user, rowsList) -> {
			long currentWindowId = 1;
			int userRowsCount = rowsList.size();
			Set<String> keywordsInCurrentSession = new HashSet<>(); //For use in Step 3 - ESA measure. Reset at session change
			
			if(userRowsCount > 0){ //TODO Incrementing session counter and resetting bag of terms
				for(int i = 0; i < userRowsCount; i++){
					DataRow di = rowsList.get(i);
					di.windowId = currentWindowId;					
					keywordsInCurrentSession.addAll(di.termList);					
					for(int j = i+1 ; j < userRowsCount; j++){
						// Label sessions similar to Q_i here						
						DataRow dj = rowsList.get(j);
						// Step 1
						Boolean stringComparisonResult = this._areQueryTermsSimilar(di, dj);
						if(stringComparisonResult){
							dj.windowId = currentWindowId;
							keywordsInCurrentSession.addAll(dj.termList);
							continue;
						}
						// Step 2
						Boolean featureCalculationResults = this._areGeometricallySimilar(di, dj);
						if(featureCalculationResults!=null){
							if(featureCalculationResults){
								dj.windowId = currentWindowId;
								keywordsInCurrentSession.addAll(dj.termList);
							} else	{ // New Session
								currentWindowId += 1;
								keywordsInCurrentSession = new HashSet<>();
							}
							continue;
						}			
						// Step 3
						Boolean featureEsaResult = this._getEsaFeature(keywordsInCurrentSession, dj.termList);// Implement the ESA measure here
						if(featureEsaResult){
							dj.windowId = currentWindowId;
							keywordsInCurrentSession.addAll(dj.termList);
							continue;
						}
						
						//Now, Steps 1,2 and 3 have been unable to put Q_j in the same session as Q_i
						currentWindowId += 1; //Beginning new session
						keywordsInCurrentSession = new HashSet<>();
					}
				}
			}						
		});			
		return sessionsGroupByUser;		
	}
	
	private Boolean _getEsaFeature(Set<String> keywordsInCurrentSession, Set<String> termList) {
		return this._getEsaFeature(keywordsInCurrentSession, termList, 0.35) ;
	}
	
	/* Refer Section 3 , Step 3
	 * @param termsInCurrentSession 
	 * @param termList terms in query currently being considered
	 * @param dge value greater than equal to
	 */
	private Boolean _getEsaFeature(Set<String> termsInCurrentSession, Set<String> termList, double dge) {
		Boolean result = false;
		Set<String> allTerms = new HashSet<>(termsInCurrentSession);
		allTerms.addAll(termList);
		CosineSimilarity c = new CosineSimilarity();
		Double sim = c.getCosineSimilarity(allTerms, esaCorpus);
		if(sim >= dge){
			result = true;
		}
		return result;
	}

	/* Refer Refer Section 4 "The complete cascade algorithm" of the paper"
	 * @param di
	 * @param djr
	 * @return True if DataRow di and DataRow dj are part of the same session
	 * @return False if DataRow di and DataRow dj are part of different sessions
	 * @return Null if the test is indecisive
	 */
	private Boolean _areGeometricallySimilar(DataRow di, DataRow dj) {
		return this._areGeometricallySimilar(di, dj, 0.4, 0.8);
	}
	
	/* Refer Refer Section 4 "The complete cascade algorithm" of the paper"
	 * @param di
	 * @param dj
	 * @param fTimeGe parameter
	 * @param fLexLe parameter
	 * @return True if DataRow di and DataRow dj are part of the same session
	 * @return False if DataRow di and DataRow dj are part of different sessions
	 * @return Null if the test is indecisive
	 */
	private Boolean _areGeometricallySimilar(DataRow di, DataRow dj, double fTimeGe, double fLexLe) {
		Boolean result = null;
		Double f_time = this._getTimeFeature(di, dj);
		Double f_lex = this._getLexFeature(di, dj);
		if(f_time >= fTimeGe || f_lex <= fLexLe){
			Double score = Math.sqrt((f_time * f_time) + (f_lex * f_lex));
			result = (score >= 1 ? true : false);
		}
		return result;
	}

	public Double _getLexFeature(DataRow curr, DataRow prev) {
		Boolean result = false;
		// Add 3,4,5-grams to a list derived from foo
		Set<String> foo = this.ngrams(3, curr.query);
		foo.addAll(this.ngrams(4, curr.query));
		foo.addAll(this.ngrams(5, curr.query));
		// Add 3,4,5-grams to a list derived from bar
		Set<String> bar = this.ngrams(3, prev.query);
		bar.addAll(this.ngrams(4, prev.query));
		bar.addAll(this.ngrams(5, prev.query));

//		List<Set<String>> listOfSets = new ArrayList<>();	    
//	    listOfSets.add(foo);
//	    listOfSets.add(bar);
	    CosineSimilarity c = new CosineSimilarity();
	    Double sim = c.getCosineSimilarity(foo, bar);
		return sim;
	}
	
	public Set<String> ngrams(int n, String str) {
	    Set<String> ngrams = new HashSet<>();
	    for (int i = 0; i < str.length() - n + 1; i++)
	        ngrams.add(str.substring(i, i + n));
	    return ngrams;
	}

	/*
	 * Method overload for _getTimefeature with a span parameter of 24 hours
	 */
	public Double _getTimeFeature(DataRow curr, DataRow prev) {
		return this._getTimeFeature(curr, prev, 24.0);
	}
	/*
	 * Calculates the time feature of consecutive query timestamps in DataRows curr and prev. For method details, refer Section 4 Step 2 
	 * of "Query Session Detection as a Cascade" by Hagen, Stein and Rub
	 * @param curr
	 * @param prev
	 * @param span parameter is the denominator of the ratio
	 * @return Returns double value of time feature f_time
	 */
	public Double _getTimeFeature(DataRow curr, DataRow prev, Double span) {
		Double t = 0.0d;
		Double denominator = span * 60.0d * 60.0d * 1000.0d; // 24 hours duration in epoch time (by default)
		Long numerator = Math.abs(curr.timestamp - prev.timestamp);
//		System.out.println("Numerator "+numerator);
//		System.out.println("Denominator "+denominator);
//		System.out.println("Ratio "+(numerator/ denominator));
		t = 1.0d - (numerator/ denominator);
//		System.out.println("f_time "+t);
		return t;
	}
	
	/*
	 * Calculates the set intersection of query terms in DataRows curr and prev. For method details, refer Section 4 Step 1 
	 * of "Query Session Detection as a Cascade" by Hagen, Stein and Rub
	 * @param curr
	 * @param prev
	 * @return Returns true iff Query terms of curr and prev are 1. Repetitions 2. Specialization 3. Generalization. Returns false otherwise.
	 */
	public Boolean _areQueryTermsSimilar(DataRow curr, DataRow prev) {
		Boolean result = false;
		Set<String> foo = curr.getTermList();
	    Set<String> bar = prev.getTermList();	    
	    List<Set<String>> listOfSets = new ArrayList<>();
	    
	    listOfSets.add(foo);
	    listOfSets.add(bar);
	    
//	    System.out.println("\nCalculating intersection of "+foo+" & "+bar);
		Set<String> intersection = listOfSets.stream().collect(()->new HashSet<>(listOfSets.get(0)), Set::retainAll, Set::retainAll);
//		System.out.println("Intersection -->"+intersection);		
		if(intersection.size() >= Math.min(foo.size(), bar.size()))
//			System.out.println(intersection.size() + " >= " + Math.min(foo.size(), bar.size()));
			result = true;
		return result;
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
					//TODO Commenting out the next 2 lines. See if there is any effect
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

}
