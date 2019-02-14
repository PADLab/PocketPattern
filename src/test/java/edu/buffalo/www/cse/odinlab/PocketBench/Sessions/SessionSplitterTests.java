package edu.buffalo.www.cse.odinlab.PocketBench.Sessions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.experimental.categories.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * 					==== README for running these test ====
 * The methods of this test suite are chosen for running on basis of a reg ex. If you want to 
 * exclude a method from the test runner's scope, just remove the 'test' prefix from the method name
 * Conversely, if you want a method to be included in the run, add / retain the 'test' prefix from 
 * the method name. Right-click the test file in Eclipse and choose "Run As >> JUnit Test". I 
 * understand that this is hacky way of running things. A better way to explore later would be 
 * to use 'Categories' decorations of Maven test runners.
 */

public class SessionSplitterTests extends TestCase {
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SessionSplitterTests( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( SessionSplitterTests.class );
    }
    
    public void FileImportApp()
    {
    	String inFilePath = "src/test/resources/GooglePlusClustering-Test.csv";
        SessionSplitter s = new SessionSplitter();    
		File testFileResource = new File(inFilePath);
		String pathToFile = testFileResource.getAbsoluteFile().toString();
		System.out.println("\nUse test data from file "+pathToFile);
		Boolean fileReadStatus = s.readFromFile(pathToFile);
		Boolean rowsImportStatus = (s._rows.size() > 0);
		assertTrue("Rows imported from "+pathToFile+" successfully.",fileReadStatus && rowsImportStatus);
    }
    
    public void MakeSessionsWithFileImportApp()
    {
    	int idleTimeTolerance = 1000;
    	String inFilePath = "src/test/resources/GooglePlusClustering-Test.csv";
    	SessionSplitter s = new SessionSplitter();    
		File testFileResource = new File(inFilePath);
		String pathToFile = testFileResource.getAbsoluteFile().toString();
		System.out.println("\nUse test data from file "+pathToFile);
		Boolean fileReadStatus = s.readFromFile(pathToFile);
		Boolean rowsImportStatus = (s._rows.size() > 0);		
		
		Map<String, List<DataRow>> sessionsByUser  =s._makeSessionsGroupByUser(idleTimeTolerance);

		Boolean ifSessionsOfFirstUserArePresent = sessionsByUser.get("0").size() == 11 ; // Do not change this unless you are changing the test file as well
		Boolean ifSessionsOfSecondUserArePresent = sessionsByUser.get("1").size() == 5 ;
		Boolean ifSessionIdsHaveBeenExported = s.getSessionIds("1").size() == 16;
		assertTrue(ifSessionIdsHaveBeenExported && ifSessionsOfFirstUserArePresent && ifSessionsOfSecondUserArePresent && rowsImportStatus && fileReadStatus);
    }
    
    public void AverageSimilarityWithFileImportApp()
    {
    	int idleTimeTolerance = 1000;
    	String inFilePath = "src/test/resources/GooglePlusClustering-Test.csv";
    	SessionSplitter s = new SessionSplitter();    
		File testFileResource = new File(inFilePath);
		String pathToFile = testFileResource.getAbsoluteFile().toString();
		System.out.println("\nUse test data from file "+pathToFile);
		Boolean fileReadStatus = s.readFromFile(pathToFile);
		Boolean rowsImportStatus = (s._rows.size() > 0);		
//		Map<String, List<DataRow>> sessionsByUser  =s.makeSessionsGroupByUser(idleTimeTolerance);
//
//		Boolean ifSessionsOfFirstUserArePresent = sessionsByUser.get("0").size() == 11 ; // Do not change this unless you are changing the test file as well
//		Boolean ifSessionsOfSecondUserArePresent = sessionsByUser.get("1").size() == 5 ;
		
		List<OutputDataRow> avgSimWindows = s._calculateAverageSimilarityForWindows(idleTimeTolerance);
		//TODO: Need a more specific condition for this
		Boolean ifAvgSimHasBeenCalculated = avgSimWindows.size() == 5; // This is specific to the test data file
		assertTrue(fileReadStatus && rowsImportStatus && ifAvgSimHasBeenCalculated);
    }
    
    public void SessionsCSVExportWithFileImportApp()
    {
    	int idleTimeTolerance = 1000;
    	String inFilePath = "src/test/resources/GooglePlusClustering-Test.csv";
    	String outFilePath = "src/test/resources/GooglePlusClustering-Test-Result.csv";
    	
    	SessionSplitter s = new SessionSplitter();    
		File testFileResource = new File(inFilePath);
		
		String pathToFile = testFileResource.getAbsoluteFile().toString();
		System.out.println("\nUse test data from file "+pathToFile);
		
		Boolean fileReadStatus = s.readFromFile(pathToFile);
		Boolean rowsImportStatus = (s._rows.size() > 0);		
		
		List<OutputDataRow> avgSimWindows = s._calculateAverageSimilarityForWindows(idleTimeTolerance);		
		Boolean resultsExportStatus = s.exporttoCSV(avgSimWindows, outFilePath);
		
		assertTrue(fileReadStatus && rowsImportStatus && resultsExportStatus);
    }
    
    public void LongTimestampToDateApp()
    {
    	int idleTimeTolerance = 1000;
    	String inFilePath = "src/test/resources/GooglePlusClustering-Test.csv";
    	String outFilePath = "src/test/resources/GooglePlusClustering-Test-Result.csv";
    	SessionSplitter s = new SessionSplitter();
    	File testFileResource = new File(inFilePath);    	
    	String pathToFile = testFileResource.getAbsoluteFile().toString();
		System.out.println("\nUse test data from file "+pathToFile);		
		Boolean fileReadStatus = s.readFromFile(pathToFile);
		Boolean rowsImportStatus = (s._rows.size() > 0);
		
		Map<String, List<DataRow>> sessionsByUser  = s._makeSessionsGroupByUser(idleTimeTolerance);
		LinkedHashMap<String, LinkedHashMap<String, List<Session>>> r = s.splitUserSessionsByDate(sessionsByUser);

		Boolean ifThereAreUsers = r.keySet().size() == 2;
		Boolean ifFirstUserHasDates = r.get("0").keySet().size() == 1;
		Boolean ifSecondUserHasDates = r.get("1").keySet().size() == 1;
		Boolean ifFirstUserFirstDateHasSessions = r.get("0").get("2015-03-22").size() == 4;
		Boolean ifSecondUserFirstDateHasSessions = r.get("1").get("2015-03-02").size() == 1;

		assertTrue(fileReadStatus && rowsImportStatus && ifThereAreUsers && ifFirstUserHasDates && ifSecondUserHasDates && ifFirstUserFirstDateHasSessions && ifSecondUserFirstDateHasSessions);
    }
    
    /*
     * Experiment for Knee Selection for number of sessions versus idle time tolerance curve
     */
    public void testKneeSelectionMethodApp()
    {
//    	Integer idleTimes[] = {10, 100, 1000, 10000, 100000, 10000};
    	List<Integer> idleTimes = new ArrayList<>(Arrays.asList(10, 100, 1000, 10000, 100000, 10000));
    	String inFilePath = "src/test/resources/GooglePlusClustering-Test.csv";
    	SessionSplitter s = new SessionSplitter();    
		File testFileResource = new File(inFilePath);
		String pathToFile = testFileResource.getAbsoluteFile().toString();
		System.out.println("\nUse test data from file "+pathToFile);
		
		Boolean fileReadStatus = s.readFromFile(pathToFile);
		Boolean rowsImportStatus = (s._rows.size() > 0);
		
		List<Integer> idealIdleTime = s.findIdealIdleTimesForAllUsers(idleTimes);
		System.out.println("Ideal idle times are "+idealIdleTime);
		Boolean correctIdealIdleTimeCalculated = idealIdleTime.get(0).equals(100000) && idealIdleTime.get(1).equals(100);
		assertTrue(fileReadStatus && rowsImportStatus && correctIdealIdleTimeCalculated);
    }
    
}
