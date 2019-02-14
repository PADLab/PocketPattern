package edu.buffalo.www.cse.odinlab.ettu.timeseries;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.statement.Statement;

public interface DataReader {

	String dataFolder = "";

	void initialize(String dataFolder);
	
	List<String> findUserFiles(String dataFolder);
	
	List<String> getUserFiles();
	
	List<Statement> extractUniqueQueries();
}
