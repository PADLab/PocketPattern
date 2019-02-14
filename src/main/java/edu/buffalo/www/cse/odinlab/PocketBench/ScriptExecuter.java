package edu.buffalo.www.cse.odinlab.PocketBench;

import java.io.IOException;
import java.util.Scanner;

public class ScriptExecuter {
	
	/**
	 * This method runs the given command line scripts with given parameters
	 * @param command
	 * @param args
	 */
	public static void runCommand(String command, String[] args) {
		StringBuilder builder = new StringBuilder();;
		
		if (args != null && args.length > 0) {
			for(int i = 0; i < args.length; i++) {
				builder.append(args[i] + " ");
			}
		}
				
		try {
			
			//String path = System.getenv("PATH");
			ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", command + " "
					+ builder.toString());
			
        	System.out.println("Running " + command + " "
					+ builder.toString());
			
			processBuilder.start().waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Scanner keyboard = new Scanner(System.in);
	    	
	    	String appPath = "";
	        if (args == null || args.length == 0) {
	        	System.out.println("Enter the path to the exe file: ");
	        	appPath = keyboard.nextLine();
	        } else {
	        	appPath = args[0];
	        }
	        
	        ProcessBuilder processBuilder = new ProcessBuilder(appPath + " "
					+ builder.toString());
			
        	System.out.println("Running " + appPath + " "
					+ builder.toString());
			
			try {
				processBuilder.start().waitFor();
			} catch (InterruptedException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
