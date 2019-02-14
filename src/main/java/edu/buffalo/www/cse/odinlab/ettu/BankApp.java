package edu.buffalo.www.cse.odinlab.ettu;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import edu.buffalo.www.odinlab.statlib.StatLib;

public class BankApp {

	public static void main(String[] args) {
		
       String dataFolder = "/Users/gokhanku/Documents/Research/Data/currentBank/data";
        
        
        BankExperimentSetup workingObject = null;
        
	    workingObject = new BankExperimentSetup(dataFolder);
	    
	    workingObject.setupUserInfo();
	        
	    System.out.println("Experiment2 - Start time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
	    workingObject.setupExperiment2();
	    System.out.println("Experiment2 - End time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
	        
	    System.out.println("Experiment1Daily - Start time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
	    workingObject.setupExperiment1ver2();
	    System.out.println("Experiment1Daily - End time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
	        
	        //System.out.println("Experiment1Percent - Start time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
	        //workingObject.setupExperiment1ver1();
	        //System.out.println("Experiment1Percent - End time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));

	}

}
