package edu.buffalo.www.cse.odinlab.PocketBench;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import edu.buffalo.www.cse.odinlab.PocketBench.ExperimentSetup;

public class App 
{
    public static void main( String[] args )
    {
    	String[] apps = {"Facebook", "GooglePlus", "Hangouts", "MediaStorage", "GooglePlayServices, CompleteWorkload"};
		
		Scanner keyboard = new Scanner(System.in);
    	
    		//Determining which application we are going to work on
		String input = "";
        if (args == null || args.length == 0) {
	        	System.out.println("Enter the app name: (type \"all\" for all apps, or enter specific app name)");
	        	//input = keyboard.nextLine();
	        	input = "Facebook";
        } else {
        		input = args[0];
        }
        
        String workingDirectory = System.getProperty("user.dir");
        System.out.println("Working Directory = " + workingDirectory);
        
        if (input.equals("cmu")) {
        	
	        	//TODO
	        	String dataFolder = "/Users/gokhanku/Documents/Research/Data/cert-data"; //TODO Make this relative path and not dependent on the local system
	        	
	        	return;
        }
        
        if (!input.equals("all")) {
	        	apps = new String[1];
	        	apps[0] = input;
        }
        
        ExperimentSetup workingObject = null;
        
        for (int i = 0; i < apps.length; i++) {
        	workingDirectory = "/home/gokhanku/eclipse-workspace/sampleData/com.facebook";
	        //workingDirectory = "/Volumes/TOSHIBA EXT/data/AppData/com.facebook";
        	workingObject = new ExperimentSetup(apps[i], workingDirectory);
	        
	        System.out.println("Experiment2 - Start time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
	        //workingObject.runExperiment1();
	        //workingObject.runQueryCountPerApp("/media/gokhanku/TOSHIBA EXT/data/AppData/com.facebook/", "2017-04-28", "2017-05-24");
	        workingObject.runBaseExperiment("2017-04-29", "2017-05-21");
	        workingObject.runPredictionExperiment("2017-04-29", "2017-05-21");
	        //workingObject.runQueryCountPerApp("/media/gokhanku/TOSHIBA EXT/data/AppData/com.twitter.android/", "2017-04-28", "2017-05-24");
	        //workingObject.runQueryCountPerAppWithoutTime("/media/gokhanku/TOSHIBA EXT/data/AppData/com.facebook/");
	        System.out.println("Experiment2 - End time: " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
	        
        }
		
    }
}
