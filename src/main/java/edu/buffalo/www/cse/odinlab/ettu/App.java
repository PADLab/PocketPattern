package edu.buffalo.www.cse.odinlab.ettu;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import edu.buffalo.www.odinlab.statlib.StatLib;

public class App {

	public static void main(String[] args) {
		
		String[] apps = {"Facebook", "GooglePlus", "Hangouts", "MediaStorage", "GooglePlayServices, CompleteWorkload"};
		
		Scanner keyboard = new Scanner(System.in);
    	
    	//Determining which application we are going to work on
    	String input = "";
        if (args == null || args.length == 0) {
        	System.out.println("Enter the app name: (type \"all\" for all apps, or enter specific app name)");
        	input = keyboard.nextLine();
        } else {
        	input = args[0];
        }
        
        String workingDirectory = System.getProperty("user.dir");
        System.out.println("Working Directory = " + workingDirectory);
        
        if (input.equals("cmu")) {
        	
        	//TODO
        	String dataFolder = "/Users/gokhanku/Documents/Research/Data/cert-data";
        	return;
        }
        
        if (!input.equals("all")) {
        	apps = new String[1];
        	apps[0] = input;
        }
        
        ExperimentSetup workingObject = null;
        
        for (int i = 0; i < apps.length; i++) {
	        workingObject = new ExperimentSetup(apps[i], workingDirectory);
	        
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

}
