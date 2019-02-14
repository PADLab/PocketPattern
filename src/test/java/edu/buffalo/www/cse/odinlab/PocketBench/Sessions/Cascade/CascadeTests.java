package edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Cascade;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.DataRow;

public class CascadeTests {
	final double EPSILON = 1E-2;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() { //TODO Write tests for CascadeSessionSimilarity using a sample ESA Corpus	
		String inFilePath = "src/test/resources/GooglePlusClustering-Test.csv";
		String sampleEsaCorpusText = "SELECT name FROM sqlite_master WHERE type='table' SELECT media_type, volume_name, last_media_id FROM media_tracker";

		Set<String> esaCorpus = new HashSet<String>();
    	esaCorpus.addAll(Arrays.asList(sampleEsaCorpusText.split(" ")));
		SessionSplitter s = new SessionSplitter();
    	s.setEsaCorpus(esaCorpus);
    	
		File testFileResource = new File(inFilePath);
		String pathToFile = testFileResource.getAbsoluteFile().toString();
		System.out.println("\nUsing test data from file "+pathToFile);
		
		Boolean fileReadStatus = s.readFromFile(pathToFile);
		Boolean rowsImportStatus = (s._rows.size() > 0);
		Map<String, List<DataRow>> sessionsByUser = s._makeSessionsGroupByUser();
		LinkedHashMap<String, LinkedHashMap<Long, List<DataRow>>> results = s.exportResults(); //Make sure you call _makeSessionsGroupByUser before trying to export the result
		
		results.forEach((user, mapTimeRows) ->{
			System.out.println("User - "+user);
			mapTimeRows.forEach((timestamp, userTimeRows) -> {
				System.out.println("Timestamp -- "+timestamp);
				userTimeRows.forEach(r ->{
					System.out.println(r);
				});
			});
		});
		
		assertTrue(fileReadStatus && rowsImportStatus);
	}
	
//	@Test
	public void testTimeFeature() {
		Long timestamp1 = 1427031203870L;
		Long timestamp2 = 1427042024387L;
		
		DataRow dr1 = new DataRow();
		dr1.setTimestamp(timestamp1);
		DataRow dr2 = new DataRow();
		dr2.setTimestamp(timestamp2);
		SessionSplitter s = new SessionSplitter();
		Double f_time = s._getTimeFeature(dr1, dr2);
//		System.err.println("f_time for "+ dr1.timestamp + " and "+dr2.timestamp+" is "+f_time);
		assertTrue(f_time >= 0.0d && f_time <= 1.0d);
//		assertTrue(fileReadStatus && rowsImportStatus);
	}
	
//	@Test
	public void testNgrams()
	{
		SessionSplitter s = new SessionSplitter();
		DataRow dr1 = new DataRow();
		dr1.query = "ThisIsATest";
		Set<String> expected3Grams = new HashSet<>(Arrays.asList("ATe", "his", "Tes", "Thi", "IsA", "sAT", "est", "sIs", "isI"));		
		Set<String> actual3Grams = s.ngrams(3, dr1.query);
		Boolean Is3GramExpectedSetSameAsActual = this.SetEquals(expected3Grams, actual3Grams);
		
		Set<String> expected4Grams = new HashSet<>(Arrays.asList("sIsA", "isIs", "Test", "hisI", "This", "sATe", "IsAT", "ATes"));		
		
		Set<String> actual4Grams = s.ngrams(4, dr1.query);
		Boolean Is4GramExpectedSetSameAsActual = this.SetEquals(expected3Grams, actual3Grams);
//		System.out.println(actual4Grams);
		Set<String> expected5Grams = new HashSet<>(Arrays.asList("isIsA", "IsATe", "hisIs", "sIsAT", "sATes", "ATest", "ThisI"));		
		Set<String> actual5Grams = s.ngrams(5, dr1.query);
		Boolean Is5GramExpectedSetSameAsActual = this.SetEquals(expected3Grams, actual3Grams);
//		System.out.println(actual5Grams);
		assertTrue(Is3GramExpectedSetSameAsActual && Is4GramExpectedSetSameAsActual && Is5GramExpectedSetSameAsActual);	
	}
	
//	@Test
	public void testCosineSimilarity()
	{
		CosineSimilarity c = new CosineSimilarity();
		DataRow dr1 = new DataRow();
		dr1.query = "SELECT name FROM sqlite_master WHERE type='table'";
		dr1.termList = DataRow.tokenizeQuery(dr1.query);
		DataRow dr2 = new DataRow();
		dr2.query = "SELECT media_type, volume_name, last_media_id FROM media_tracker";
		dr2.termList = DataRow.tokenizeQuery(dr2.query);
		Double sim = c.getCosineSimilarity(dr1.termList, dr2.termList);
		System.out.println("Cosine Similarity -- "+sim);
		assertTrue(Math.abs(sim - 0.33) <= EPSILON);
	}
	/*
	 * Utility method to check set equality
	 */
    private Boolean SetEquals(Set<?> set1, Set<?> set2) {
    	if(set1 == null || set2 ==null){
            return false;
        }

        if(set1.size()!=set2.size()){
            return false;
        }

        return set1.containsAll(set2);
	}

}
