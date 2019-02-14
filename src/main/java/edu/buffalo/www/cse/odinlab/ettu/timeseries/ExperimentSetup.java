package edu.buffalo.www.cse.odinlab.ettu.timeseries;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ExperimentSetup {
	private String workingDirectory = "";
	private String appName = "";
	private int userSize = 0;
	
	public ExperimentSetup(String appName, String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	public void runBaseExperiment(String startTime, String endTime) {
		
		DataReader workloadReader = new DataReaderPhoneLabLog(workingDirectory, startTime, endTime);
		
	}

}
