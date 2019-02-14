package edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Cascade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

//import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Cascade.QuerySkeletonTool;
//import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Cascade.TermList;
//import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Cascade.TermType;
import net.sf.jsqlparser.statement.Statement;

public class Generalizer {

	public static ArrayList<TermList> getQueryTermLists(ArrayList<Statement> query_set) {
		
		ArrayList<Statement> skeletonList = QuerySkeletonTool.getQuerySkeleton(query_set);
		ArrayList<TermList> termLists = new ArrayList<TermList>();

		for (int i = 0; i < skeletonList.size(); i++) {
			//System.out.println(skeletonList.get(i));
			termLists.add(getTermList(skeletonList.get(i).toString()));
		} 
		
		return termLists;
	}
	
	public static ArrayList<TermList> getTermLists(ArrayList<String> query_set) {
		
		ArrayList<TermList> termLists = new ArrayList<TermList>();

		for (int i = 0; i < query_set.size(); i++) {
			//System.out.println(query_set.get(i));
			termLists.add(getTermList(query_set.get(i).toString()));
		} 
		
		return termLists;
	}
	
	/*
	 * @param: Array of query strings
	 */
	public static ArrayList<TermList> getTermLists(Object[] query_set) {
		
		ArrayList<TermList> termLists = new ArrayList<TermList>();

		for (int i = 0; i < query_set.length; i++) {
			//System.out.println(query_set[i]);
			termLists.add(getTermList(query_set[i].toString()));
		} 
		
		return termLists;
	}
	
	/*
	 * @param statement : query string
	 */
	public static TermList getTermList(String statement) {
		
		String[] prelimResult = statement.split("\\s|,");
		ArrayList<String> terms = new ArrayList<>();
		TermList termList = new TermList(statement);
		for (int i = 0; i < prelimResult.length; i++) {
			if (!(prelimResult[i].equals(null) || prelimResult[i].equals(""))) {
//				terms.add(prelimResult[i]);
				termList.addTerm(prelimResult[i]);
			}
		} 
		
//	    System.err.println(terms);
	    
	    
//	    
//	    for(int i = 0; i < terms.size(); i++) {
//	    	HashSet<String> aliases = new HashSet<String>();
//	    	inspectTerm(termList, terms.get(i), aliases);
//	    }
		
		return termList;
	}

//	private static void inspectTerm(TermList termList, String term, HashSet<String> aliases) {
//		if (term.equals("?")) {
//			termList.addTerm(TermType.CONSTANT);
//		} else if (tryParseDoubleOrInt(term)) {
//			termList.addTerm(TermType.CONSTANT);
//		} else if (termList.getLast() == TermType.SQL_AS) {
//			aliases.add(term);
//			termList.addTerm(TermType.ALIAS);
//		} else if (term.toUpperCase().equals("INSERT")) {
//			termList.addTerm(TermType.SQL_INSERT);
//		} else if (term.toUpperCase().equals("REPLACE")) {
//			termList.addTerm(TermType.SQL_REPLACE);
//		} else if (term.toUpperCase().equals("UPDATE")) {
//			termList.addTerm(TermType.SQL_UPDATE);
//		} else if (term.toUpperCase().equals("SELECT")) {
//			termList.addTerm(TermType.SQL_SELECT);
//		} else if (term.toUpperCase().equals("DISTINCT")) {
//			termList.addTerm(TermType.SQL_DISTINCT);
//		} else if (term.toUpperCase().equals("WHERE")) {
//			termList.addTerm(TermType.SQL_WHERE);
//		} else if (term.toUpperCase().equals("FROM")) {
//			termList.addTerm(TermType.SQL_FROM);
//		} else if (term.toUpperCase().equals("GROUP")) {
//			termList.addTerm(TermType.SQL_GROUP);
//		} else if (term.toUpperCase().equals("ORDER")) {
//			termList.addTerm(TermType.SQL_ORDER);
//		} else if (term.toUpperCase().equals("BY")) {
//			termList.addTerm(TermType.SQL_BY);
//		} else if (term.toUpperCase().equals("ASC")) {
//			termList.addTerm(TermType.SQL_ASC);
//		} else if (term.toUpperCase().equals("DESC")) {
//			termList.addTerm(TermType.SQL_DESC);
//		} else if (term.toUpperCase().equals("LIMIT")) {
//			termList.addTerm(TermType.SQL_TOP);
//		} else if (term.toUpperCase().equals("AND")) {
//			termList.addTerm(TermType.SQL_AND);
//		} else if (term.toUpperCase().equals("OR")) {
//			termList.addTerm(TermType.SQL_OR);
//		} else if (term.toUpperCase().equals("VALUES")) {
//			termList.addTerm(TermType.SQL_VALUES);
//		} else if (term.toUpperCase().equals("WITH")) {
//			termList.addTerm(TermType.SQL_WITH);
//		} else if (term.toUpperCase().equals("SET")) {
//			termList.addTerm(TermType.SQL_SET);
//		} else if (term.toUpperCase().equals("AS")) {
//			termList.addTerm(TermType.SQL_AS);
//		} else if (term.toUpperCase().equals("IN")) {
//			termList.addTerm(TermType.SQL_IN);
//		} else if (term.toUpperCase().equals("LIKE")) {
//			termList.addTerm(TermType.SQL_LIKE);
//		} else if (term.toUpperCase().equals("=")) {
//			termList.addTerm(TermType.SQL_EQUALTO);
//		} else if (term.toUpperCase().equals(">")) {
//			termList.addTerm(TermType.SQL_GREATERTHAN);
//		} else if (term.toUpperCase().equals("<")) {
//			termList.addTerm(TermType.SQL_LESSTHAN);
//		} else if (term.toUpperCase().equals("<>")) {
//			termList.addTerm(TermType.SQL_NOTEQUAL);
//		} else if (term.toUpperCase().equals("!=")) {
//			termList.addTerm(TermType.SQL_NOTEQUAL);
//		} else if (term.toUpperCase().equals("-")
//				|| term.toUpperCase().equals("+")
//				|| term.toUpperCase().equals("/")) {
//			termList.addTerm(TermType.SQL_OPERATOR);
//		} else if (term.toUpperCase().equals("<=")) {
//			termList.addTerm(TermType.SQL_LESSTHAN);
//			termList.addTerm(TermType.SQL_EQUALTO);
//		} else if (term.toUpperCase().equals(">=")) {
//			termList.addTerm(TermType.SQL_GREATERTHAN);
//			termList.addTerm(TermType.SQL_EQUALTO);
//		} else if (term.toUpperCase().equals("AS")) {
//			termList.addTerm(TermType.SQL_AS);
//		} else if (term.toUpperCase().equals("MIN")) {
//			termList.addTerm(TermType.AGG_MIN);
//		} else if (term.toUpperCase().equals("MAX")) {
//			termList.addTerm(TermType.AGG_MAX);
//		} else if (term.toUpperCase().equals("SUM")) {
//			termList.addTerm(TermType.AGG_SUM);
//		} else if (term.toUpperCase().equals("AVG")) {
//			termList.addTerm(TermType.AGG_AVG);
//		} else if (term.toUpperCase().equals("COUNT")) {
//			termList.addTerm(TermType.AGG_COUNT);
//		} else if (term.equals("*")) {
//			termList.addTerm(TermType.ALL_COLUMNS);
//		} else {
//			if (term.contains("(")){
//				if (!term.startsWith("(")) {
//					String temp = term.substring(0, term.indexOf("("));
//					//System.out.println("temp = " + temp);
//					term = term.substring(term.indexOf("("));
//					//System.out.println("term = " + term);
//					if (temp.toUpperCase().equals("MIN")) {
//						termList.addTerm(TermType.AGG_MIN);
//					} else if (temp.toUpperCase().equals("MAX")) {
//						termList.addTerm(TermType.AGG_MAX);
//					} else if (temp.toUpperCase().equals("SUM")) {
//						termList.addTerm(TermType.AGG_SUM);
//					} else if (temp.toUpperCase().equals("AVG")) {
//						termList.addTerm(TermType.AGG_AVG);
//					} else if (temp.toUpperCase().equals("COUNT")) {
//						termList.addTerm(TermType.AGG_COUNT);
//					} else {
//						termList.addTerm(TermType.SQL_FUNCTION);
//					}
//					inspectTerm(termList, term, aliases);
//				} else if (term.startsWith("(")) {
//					termList.addTerm(TermType.SQL_PARANTHESIS_OPEN);
//					inspectTerm(termList, term.substring(1), aliases);
//				}
//			} else if (term.endsWith(")")) {
//				//System.out.println(term.substring(0, term.length() - 1));
//				inspectTerm(termList, term.substring(0, term.length() - 1), aliases);
//				
//				termList.addTerm(TermType.SQL_PARANTHESIS_CLOSE);
//			} else if (term.matches(".*\\..*")) {
//				//System.out.println("Expecting Column = " + term);
//				termList.addTerm(TermType.COLUMN);
//			} else if (aliases.contains(term)) {
//				termList.addTerm(TermType.ALIAS);
//			} else if (term.matches("\\'.*\\'")) {
//				//System.out.println("Constant expecting = " + term);
//				termList.addTerm(TermType.CONSTANT);
//			} else if (term.matches("\\\".*\\\"")) {
//				//System.out.println("Constant expecting = " + term);
//				termList.addTerm(TermType.CONSTANT);
//			} else {
//				if (termList.getLast() == TermType.SQL_FROM || termList.getLast() == TermType.TABLE) {
//					termList.addTerm(TermType.TABLE);
//				} else if (term.matches(".+\\=.*")) {
//					//System.out.println(term.substring(0, term.indexOf("=")));
//					inspectTerm(termList, term.substring(0, term.indexOf("=")), aliases);
//					termList.addTerm(TermType.SQL_EQUALTO);
//					String temp = term.substring(term.indexOf("=") + 1, term.length());
//					if (temp != "") {
//						System.out.println("Girdiii: " + temp);
//						inspectTerm(termList, temp, aliases);
//					}
//				} else {
//					termList.addTerm(TermType.COLUMN);
//				}
//			}
//		}
//		
//	}

	private static boolean tryParseDoubleOrInt(String term) {
		try {
			double d = Double.parseDouble(term);
		    return true;
		}
		catch (NumberFormatException e) {
		    // Use whatever default you like
		    return false;
		}
	}
	
	
	
}

